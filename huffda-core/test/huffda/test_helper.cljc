(ns huffda.test-helper
  (:require [cljs.test :refer [deftest testing is async]]))

(defmacro test-async [name ch]
  `(testing ~name
     (async done#
       (cljs.core.async/take! ~ch (fn [_#] (done#))))))

(defmacro test-async-with-db [name db-sym & body]
  `(testing ~name
     (async done#
       (cljs.core.async/take!
         (huffda.expectations/create-memory-database)
         (fn [[db# db-err#]]
           (cljs.core.async/take!
             (let [~db-sym db#]
               (do ~@body))
             (fn [_#]
               (.close (:db db#) (fn [err#] (done#))))))))))
