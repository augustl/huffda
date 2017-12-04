(ns huffda.expectations-db
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
