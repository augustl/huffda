(ns ^:figwheel-always huffda.main-server
  (:require [sqlite3]
            [express]
            [mustache-express]
            [cljs.nodejs :as nodejs]))

(defn get-thing []
  (.random js/Math))

(defn main [& args]
  (nodejs/enable-util-print!)

  (let [db (sqlite3/Database. ":memory:")
        app (express)]
    (.use app (.static express "resources/public"))
    (.engine app "mustache", (mustache-express))
    (.set app "view engine" "mustache")
    (.set app "view cache" false)
    (.set app "views" "resources/views")
    (.get app "/" (fn [req res] (.render res "index" (clj->js {:thing (get-thing)}))))
    (.listen app 3000 #(js/console.log "Web server started"))))

(set! *main-cli-fn* main)