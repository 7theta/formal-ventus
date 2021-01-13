(defproject com.7theta/formal-ventus "0.1.0"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"]
                 [com.7theta/utilis "1.10.0"]
                 [com.7theta/formal "0.1.0"]
                 [com.7theta/ventus "0.1.12"]
                 [reagent "1.0.0"]]
  :profiles {:dev {:source-paths ["src" "example/src" "checkouts/formal/src" "checkouts/ventus/src"]
                   :dependencies [[thheller/shadow-cljs "2.11.13"]]}})
