(defproject huffda "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.3.465"]]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.8"]
            [lein-figwheel "0.5.14"]]

  :cljsbuild {
              :builds [{:id "main-dev"
                        :source-paths ["src" "main"]
                        :figwheel true
                        :compiler {:target :nodejs
                                   :output-dir "out/main-dev"
                                   :output-to "out/main-dev/main-with-figwheel.js"
                                   :optimizations :none
                                   :pretty-print true
                                   :main huffda.main
                                   :parallel-build true
                                   :install-deps true
                                   :npm-deps {:ws "4.0.0"
                                              :source-map-support "0.5.0"
                                              :sqlite3 "3.1.13"
                                              :express "4.16.2"
                                              :mustache "2.3.0"
                                              :mustache-express "1.2.5"}}}

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
