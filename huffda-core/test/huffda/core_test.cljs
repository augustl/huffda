(ns huffda.core-test
  (:require [doo.runner :refer-macros [doo-tests]]
            huffda.expectations-basics-test
            "source-map-support/register"))

(doo-tests 'huffda.expectations-basics-test)