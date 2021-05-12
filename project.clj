(defproject com.7theta/formal-ventus "0.1.2"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"]
                 [com.7theta/utilis "1.12.1"]
                 [com.7theta/formal "0.1.2"]
                 [com.7theta/ventus-ui "0.1.0"]
                 [inflections "0.13.2"]
                 [reagent "1.0.0"]]
  :profiles {:dev {:source-paths ["src"
                                  "example/src"
                                  "../formal/src"]
                   :dependencies [[thheller/shadow-cljs "2.11.17"]]}})
