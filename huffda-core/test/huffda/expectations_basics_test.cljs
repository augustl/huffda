(ns huffda.expectations-basics-test
  (:require [cljs.test :refer [deftest testing is async]]
            [huffda.expectations :as expec]
            [cljs.core.async :refer [chan <! >! put! close! alts! timeout promise-chan take!]]
            [huffda.test-helper :refer [test-async]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [huffda.test-helper :refer [async-with-db]]))

(deftest expectation-basics
  (async-with-db
    db
    (go
      (<! (expec/add-expectation db "my-expec-1" 123))
      (let [[res err] (<! (expec/get-expectation db "my-expec-1"))]
        (is (not (:is-fulfilled res)))
        (is (not (:is-failed res)))
        (is (not (:is-timed-out res))))
      (<! (expec/fulfill-expectation db "my-expec-1" true))
      (let [[res err] (<! (expec/get-expectation db "my-expec-1"))]
        (is (:is-fulfilled res))
        (is (not (:is-failed res)))
        (is (not (:is-timed-out res)))))))

(deftest fulfilling-before-expectation-is-added
  (async-with-db
    db
    (go
      (<! (expec/fulfill-expectation db "my-expec-1" true))
      (<! (expec/add-expectation db "my-expec-1" 123))
      (let [[res err] (<! (expec/get-expectation db "my-expec-1"))]
        (is (:is-fulfilled res))
        (is (not (:is-failed res)))
        (is (not (:is-timed-out res)))))))

(deftest checking-status-with-only-fulfillment
  (async-with-db
    db
    (go
      (<! (expec/fulfill-expectation db "my-expec-1" true))
      (let [[res err] (<! (expec/get-expectation db "my-expec-1"))]
        (is (nil? res))
        (is (nil? err))))))

(deftest timing-out
  (async-with-db
    db
    (go
      (<! (expec/add-expectation db "my-expec-1" 100))
      (<! (timeout 500))
      (let [[res err] (<! (expec/get-expectation db "my-expec-1"))]
        (is (not (:is-fulfilled res)))
        (is (not (:is-failed res)))
        (is (:is-timed-out res))))))


(deftest failing-expectation
  (async-with-db
    db
    (go
      (<! (expec/add-expectation db "my-expec-1" 123))
      (<! (expec/fulfill-expectation db "my-expec-1" false))
      (let [[res err] (<! (expec/get-expectation db "my-expec-1"))]
        (is (:is-fulfilled res))
        (is (:is-failed res))
        (is (not (:is-timed-out res)))))))

(deftest multiple-expectations-prioritizes-failure
  (async-with-db
    db
    (go
      (<! (expec/add-expectation db "my-expec-1" 123))
      (<! (expec/fulfill-expectation db "my-expec-1" true))
      (<! (expec/fulfill-expectation db "my-expec-1" false))
      (let [[res err] (<! (expec/get-expectation db "my-expec-1"))]
        (is (:is-fulfilled res))
        (is (:is-failed res))
        (is (not (:is-timed-out res)))))))

(deftest multiple-expectations-prioritizes-failure
  (async-with-db
    db
    (go
      (<! (expec/add-expectation db "my-expec-1" 123))
      (<! (expec/fulfill-expectation db "my-expec-1" true))
      (<! (expec/fulfill-expectation db "my-expec-1" true))
      (<! (expec/fulfill-expectation db "my-expec-1" true))
      (let [[res err] (<! (expec/get-expectation db "my-expec-1"))]
        (is (:is-fulfilled res))
        (is (not (:is-failed res)))
        (is (not (:is-timed-out res)))))))