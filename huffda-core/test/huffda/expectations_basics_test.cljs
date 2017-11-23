(ns huffda.expectations-basics-test
  (:require [clojure.test :refer [deftest testing is async]]
            [huffda.expectations :as expec]
            [cljs.core.async :refer [chan <! >! put! close! alts! timeout promise-chan]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(deftest expectations-basics
  (testing "should work"
    (async done
      (go
        (let [[db err] (<! (expec/create-memory-database))]
          (<! (expec/add-expectation db {:key "my-expec-1"}))
          (is (do (not (<! (expec/is-fulfilled db "my-expec-1")))))
          (<! (expec/fulfill-expectation db "my-expec-1" true))
          (is (do (<! (expec/is-fulfilled db "my-expec-1")))))
        (done)))))