(ns huffda.db-helper)

(defmacro db-promise [db method sql params bindings & body]
  (let [res-binding (first bindings)]
    `(let [chan# (cljs.core.async/promise-chan)]
       (. ~db
          (~method
            ~sql
            ~params
            (fn [err# res#]
              (if err#
                (cljs.core.async/put! chan# [nil err#])
                (cljs.core.async/put! chan# [(let [~res-binding res#] (do ~@body)) nil])))))
       chan#)))