(ns huffda.expectations-basics-test
  (:require [cljs.test :refer [deftest testing is async]]
            [huffda.expectations :as expec]
            [cljs.core.async :refer [chan <! >! put! close! alts! timeout promise-chan take!]]
            [huffda.test-helper :refer [test-async]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [huffda.test-helper :refer [test-async-with-db]]))

(deftest expectations-basics
  (test-async-with-db
    "should-work"
    db
    (go
      (<! (expec/add-expectation db "my-expec-1" 123))
      (let [[res err] (<! (expec/get-expectation db "my-expec-1"))]
        (is (not (:is-fulfilled res))))
      (<! (expec/fulfill-expectation db "my-expec-1" true))
      (let [[res err] (<! (expec/get-expectation db "my-expec-1"))]
        (is (:is-fulfilled res))))))