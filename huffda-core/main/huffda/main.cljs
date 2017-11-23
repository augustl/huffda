(ns huffda.main
  (:require [sqlite3]
            [cljs.nodejs :as nodejs]))

(defn main [& args]
      (nodejs/enable-util-print!)

      (prn args)
      (js/console.log "Foo!")
      (let [db (sqlite3/Database. ":memory:")]
           (prn db)))

(set! *main-cli-fn* main)