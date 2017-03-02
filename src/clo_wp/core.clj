(ns clo-wp.core
  (:require [clj-http.client :as client]
             [cheshire.core :refer :all]))

(defrecord WordPressConnection [url username password])

(defn has-wordpress-api
  [url]
  (let [response (client/head "http://site.com/resource" {:accept :json})]
    if

(defn connect
  [url username password]
  (if (has-wordpress-api url)
    (->WordPressConnection url username password)
    (throw (IllegalArgumentException. "This URL does not have a cooresponding WordPress API."))))

(defn get-posts
  [wordpress-connection]
  )

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
