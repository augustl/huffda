(ns huffda.timed-expectations
  (:require [cljs.core.async :as async :refer [go chan <! >! put! close! alts! timeout promise-chan]]))

(defn add-millis [now millis]
  (let [date (js/Date.)]
    (.setTime date (+ (.getTime date) millis))
    date))

(defn add-expectation [{:keys [db]} {:keys [key timeout-ms reason source]}]
  (let [chan (promise-chan)]
    (.run db "INSERT INTO expectations (exp_key, created_at, timeout_at, reason, source) VALUES (?, ?, ?, ?, ?)" (clj->js [key (.getTime (js/Date.)) (.getTime (add-millis (js/Date.) timeout-ms)) reason source])
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

(defn- get-is-fulfilled [row]
  (not (nil? (aget row "fulfillment_exp_key"))))

(defn- get-is-timed-out [is-fulfilled row now]
  (if is-fulfilled
    false
    (> now (aget row "expectation_timeout_at"))))

(defn- get-expectation-from-rows [rows now]
  (case (.-length rows)
    0 nil
    1 (let [row (aget rows 0)
            is-fulfilled (get-is-fulfilled row)]
        {:is-fulfilled is-fulfilled
         :is-failed (if is-fulfilled
                      (not (num-to-bool (aget row "fulfillment_is_success")))
                      false)
         :is-timed-out (get-is-timed-out is-fulfilled row now)})
    (let [first-row (aget rows 0)
          is-fulfilled (get-is-fulfilled first-row)]
      {:is-fulfilled is-fulfilled
       :is-failed (if (some (fn [row] (not (num-to-bool (aget row "fulfillment_is_success")))) rows)
                    true
                    false)
       :is-timed-out (get-is-timed-out is-fulfilled first-row now)})))

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

(defn fulfill-expectation [{:keys [db]} {:keys [key success reason metadata]}]
  (let [chan (promise-chan)]
    (.run db "INSERT INTO fulfillments (exp_key, created_at, is_success, reason, metadata) VALUES (?, ?, ?, ?, ?)" (clj->js [key (.getTime (js/Date.)) success reason metadata])
          (fn [err]
            (if err
              (put! chan [nil err])
              (put! chan [true nil]))))
    chan))