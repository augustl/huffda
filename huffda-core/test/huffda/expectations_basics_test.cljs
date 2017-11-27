(ns huffda.expectations-basics-test
  (:require [clojure.test :refer [deftest testing is async]]
            [huffda.expectations :as expec]
            [cljs.core.async :refer [chan <! >! put! close! alts! timeout promise-chan take!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn test-async [name ch]
  (testing name
    (async done
      (take! ch (fn [_] (done))))))

(deftest expectations-basics
  (test-async
    "should-work"
    (go
      (let [[db err] (<! (expec/create-memory-database))]
        (<! (expec/add-expectation db {:key "my-expec-1"}))
        (is (do (not (<! (expec/is-fulfilled db "my-expec-1")))))
        (<! (expec/fulfill-expectation db "my-expec-1" true))
        (is (do (<! (expec/is-fulfilled db "my-expec-1"))))))))