(ns huffda.core-test
  (:require [doo.runner :refer-macros [doo-tests]]
            huffda.timed-expectations-test
            "source-map-support/register"))

(doo-tests 'huffda.timed-expectations-test)