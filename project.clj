(defproject clo-wp "0.1.0-SNAPSHOT"
  :description "An API to interact with WordPress's JSON API."
  :plugins [[lein-codox "0.10.3"]]
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "2.3.0"]
                 [clj-oauth "1.5.5"]
                 [cheshire "5.7.0"]])
