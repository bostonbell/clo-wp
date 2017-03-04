(ns clo-wp.core
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]))

(defn build-api-endpoint
  ([url ep] (str url "/wp-json/wp/v2" ep))
  ([url] (str url "/wp-json/wp/v2")))

(defn has-wordpress-api
  [url]
  (print (build-api-endpoint url))
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

(defn get-from-wordpress
  ([wordpress-connection endpoint-path context]
   (client/get
    (build-api-endpoint (:url wordpress-connection) (str endpoint-path "?context=" (name context)))
    {:basic-auth
     [(:username wordpress-connection)
      (:password wordpress-connection)]
     :as :json})
    )
   ([wordpress-connection endpoint-path]
    (get-from-wordpress wordpress-connection endpoint-path :edit)))

(defn get-site-information
  [wordpress-connection]
  (:body (client/get (str (:url wordpress-connection) "/wp-json")
                     {:basic-auth [(:username wordpress-connection)
                                   (:password wordpress-connection)]
                      :as :json})))
