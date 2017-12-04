(ns huffda.expectations
  (:require [sqlite3]
            [cljs.core.async :as async :refer [go chan <! >! put! close! alts! timeout promise-chan]]))

(def db-migrations
  ["CREATE TABLE expectations(
      exp_key TEXT NOT NULL,
      created_at INT NOT NULL,
      reason TEXT,
      source TEXT,
      timeout_at INT NOT NULL)"
   "CREATE UNIQUE INDEX expectations_key_unique_idx ON expectations(exp_key)"
   "CREATE INDEX expectations_timeout_at_idx ON expectations(timeout_at)"

   "CREATE TABLE expectation_contexts(
      exp_key TEXT NOT NULL REFERENCES expectations(exp_key) ON DELETE CASCADE,
      context TEXT NOT NULL)"
   "CREATE INDEX expectation_contexts_key_context_idx ON expectation_contexts(exp_key,context)"
   "CREATE INDEX expectation_contexts_context_idx ON expectation_contexts(context)"

   "CREATE TABLE fulfillments(
      exp_key TEXT NOT NULL,
      created_at INT NOT NULL,
      is_success BOOLEAN NOT NULL,
      reason TEXT,
      metadata TEXT)"
   "CREATE INDEX fulfillments_key_idx ON fulfillments(exp_key)"

   "CREATE TABLE logs(
      exp_key TEXT NOT NULL,
      created_at INT NOT NULL,
      msg TEXT NOT NULL)"
   "CREATE INDEX logs_key_idx ON logs(exp_key)"])

(defn run-db-migration [db stm]
  (let [chan (promise-chan)]
    (.run db stm (clj->js [])
          (fn [err]
            (if err
              (put! chan [nil err])
              (put! chan [true nil]))))
    chan))

(defn create-memory-database []
  (let [db (sqlite3/Database. ":memory:")
        chan (promise-chan)]
    (go
      (doseq [db-migration db-migrations]
        (let [[res err] (<! (run-db-migration db db-migration))]
          (if err
            (put! chan [nil err]))))
      (put! chan [{:db db} nil]))
    chan))

(defn add-millis [now millis]
  (let [date (js/Date.)]
    (.setTime date (+ (.getTime date) millis))
    date))

(defn add-expectation [{:keys [db]} expec-key timeout-ms]
  (let [chan (promise-chan)]
    (.run db "INSERT INTO expectations (exp_key, created_at, timeout_at) VALUES (?, ?, ?)" (clj->js [expec-key (.getTime (js/Date.)) (.getTime (add-millis (js/Date.) timeout-ms))])
          (fn [err]
            (this-as this
              (if err
                (put! chan [nil err])
                (put! chan [{} nil])))))
    chan))

(defn- num-to-bool [num]
  (if (= num 0)
    false
    true))

(defn- get-expectation-from-rows [rows now]
  (case (.-length rows)
    0 nil
    1 (let [row (aget rows 0)
            is-fulfilled (not (nil? (aget row "fulfillment_exp_key")))]
        {:is-fulfilled is-fulfilled
         :is-failed (if is-fulfilled
                      (not (num-to-bool (aget row "fulfillment_is_success")))
                      false)
         :is-timed-out (> now (aget row "expectation_timeout_at"))})
    {:is-fulfilled false
     :is-failed false
     :is-timed-out false}))

(defn get-expectation [{:keys [db]} expec-key]
  (let [chan (promise-chan)]
    (.all db "SELECT
                expectations.exp_key AS exp_key,
                expectations.created_at AS expectation_created_at,
                expectations.reason AS expectation_reason,
                expectations.source AS expectation_source,
                expectations.timeout_at AS expectation_timeout_at,
                fulfillments.exp_key AS fulfillment_exp_key,
                fulfillments.is_success AS fulfillment_is_success,
                fulfillments.created_at AS fulfillment_created_at,
                fulfillments.is_success AS fulfillment_is_success,
                fulfillments.reason AS fulfillment_reason,
                fulfillments.metadata AS fulfillment_metadata
              FROM expectations
              LEFT OUTER JOIN fulfillments ON fulfillments.exp_key = expectations.exp_key
              WHERE expectations.exp_key = ?
              GROUP BY fulfillments.is_success" (clj->js [expec-key])
          (fn [err rows]
            (if err
              (put! chan [nil err])
              (put! chan [(get-expectation-from-rows rows (.getTime (js/Date.)))
                          nil]))))
    chan))

(defn fulfill-expectation [{:keys [db]} expec-key is-success]
  (let [chan (promise-chan)]
    (.run db "INSERT INTO fulfillments (exp_key, created_at, is_success) VALUES (?, ?, ?)" (clj->js [expec-key (.getTime (js/Date.)) is-success])
          (fn [err]
            (if err
              (put! chan [nil err])
              (put! chan [true nil]))))
    chan))