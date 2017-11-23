(ns huffda.expectations
  (:require [sqlite3]
            [cljs.core.async :as async :refer [go chan <! >! put! close! alts! timeout promise-chan]]))

(def db-migrations
  ["CREATE TABLE expectations(
      key TEXT,
      created_at INT)"

   "CREATE TABLE fulfillments(
      key TEXT,
      created_at INT,
      is_success BOOLEAN)"])

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
      (doseq [promise (map #(run-db-migration db %) db-migrations)]
        (let [[res err] (<! promise)]
          (if err
            (put! chan [nil err]))))
      (put! chan [{:db db} nil]))
    chan))

(defn add-expectation [{:keys [db]} expec-key]
  (let [chan (promise-chan)]
    (.run db "INSERT INTO expectations (key, created_at) VALUES (?, ?)" (clj->js [expec-key (.getTime (js/Date.))])
          (fn [err]
            (this-as this
              (if err
                (put! chan [nil err])
                (put! chan [{} nil])))))
    chan))

(defn is-fulfilled [{:keys [db]} expec-key]
  (let [chan (promise-chan)]
    (.get db "SELECT count(*) as c FROM fulfillments WHERE key = ?" (clj->js [expec-key])
          (fn [err row]
            (if err
              (put! chan false)
              (put! chan (not (= 0 (.-c row)))))))
    chan))

(defn fulfill-expectation [{:keys [db]} expec-key is-success]
  (let [chan (promise-chan)]
    (.run db "INSERT INTO fulfillments (key, created_at, is_success) VALUES (?, ?, ?)" (clj->js [expec-key (.getTime (js/Date.)) is-success])
          (fn [err]
            (if err
              (put! chan [nil err])
              (put! chan [true nil]))))
    chan))