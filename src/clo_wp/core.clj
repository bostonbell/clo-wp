(ns clo-wp.core
  (:require [clj-http.client :as client]
             [cheshire.core :refer :all]))

(defn build-api-endpoint
  ([url ep] (str url "/wp-json/wp/v2/" ep))
  ([url] (str url "/wp-json/v2/")))

(defn has-wordpress-api
  [url]
  (try
    (let [response (client/head (build-api-endpoint url) {:accept :json})]
      (client/success? response))
    (catch Exception e false)))

(defrecord WordPressConnection [url username password])

(defn connect
  [url username password]
  (if (has-wordpress-api url)
    (->WordPressConnection url username password)
    (throw (IllegalArgumentException. "This URL does not have a cooresponding WordPress API."))))

(defn get-pages
  [wordpress-connection]
  (parse-string (:body (client/get (build-api-endpoint (:url wordpress-connection) "pages"))) true))

(defn get-page
  [wordpress-connection page-id]
  (parse-string (:body (client/get (build-api-endpoint (:url wordpress-connection) (str "pages/" page-id)))) true))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
