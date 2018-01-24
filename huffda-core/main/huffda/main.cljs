(ns huffda.main
  (:require [sqlite3]
            [express]
            [mustache-express]
            [cljs.nodejs :as nodejs]))

(defn main [& args]
  (nodejs/enable-util-print!)

  (prn args)
  (js/console.log "Foo!")
  (let [db (sqlite3/Database. ":memory:")
        app (express)]
    (.use app (.static express "resources/public"))
    (.engine app "mustache", (mustache-express))
    (.set app "view engine" "mustache")
    (.set app "views" "resources/views")
    (.get app "/" (fn [req res] (.render res "index" (clj->js {:thing "Love"}))))
    (.listen app 3000 #(js/console.log "Web server started"))
    (prn db)))

(set! *main-cli-fn* main)