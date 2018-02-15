(defproject huffda "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.3.465"]]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.8"]
            [lein-figwheel "0.5.14"]]

  :profiles {:server-dev {:figwheel {:server-port 3550}}
             :client-dev {:figwheel {:server-port 3560
                                     :css-dirs ["resources/public/css"]}}}

  :cljsbuild {
              :builds [{:id "main-server-dev"
                        :source-paths ["src" "main/server"]
                        :figwheel true
                        :compiler {:target :nodejs
                                   :output-dir "out/main-server-dev"
                                   :output-to "out/main-server-dev/main-server-dev-with-figwheel.js"
                                   :optimizations :none
                                   :pretty-print true
                                   :main huffda.main-server
                                   :parallel-build true
                                   :install-deps true
                                   :npm-deps {:ws "4.0.0"
                                              :source-map-support "0.5.0"
                                              :sqlite3 "3.1.13"
                                              :express "4.16.2"
                                              :mustache "2.3.0"
                                              :mustache-express "1.2.5"}}}

                       {:id "main-client-dev"
                        :source-paths ["main/client"]
                        :figwheel true
                        :compiler {:output-to "resources/public/js/huffda.js"
                                   :output-dir "resources/public/js/out"
                                   :asset-path "js/out"
                                   :optimizations :none
                                   :pretty-print true
                                   :main huffda.main-client
                                   :parallel-build true}}

                       {:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:target :nodejs
                                   :output-dir "out/test"
                                   :output-to "out/test.js"
                                   :optimizations :none
                                   :pretty-print true
                                   :main huffda.core-test
                                   :parallel-build true
                                   :install-deps true
                                   :source-map true
                                   :npm-deps {:sqlite3 "3.1.13"
                                              :source-map-support "0.5.0"}}}]})
