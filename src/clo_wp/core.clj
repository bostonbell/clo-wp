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

(defn get-information
  [wordpress-connection]
  (:body (client/get (str (:url wordpress-connection) "/wp-json")
                     {:basic-auth [(:username wordpress-connection)
                                   (:password wordpress-connection)]
                      :as :json})))

(defn get-page-ids
  "Gets all the pages ids that a WordPress site currently has.

  Takes an instantiated WordPressConnection record and returns
  a vector of integers representing page IDs."

  [wordpress-connection]
  (into []
        (map :id
             (:body (client/get
                     (build-api-endpoint (:url wordpress-connection) "/pages")
                     {:basic-auth [(:username wordpress-connection)
                                   (:password wordpress-connection)]
                      :as :json})))))

(defn get-pages
  "Gets all the pages from a wordpress-connection. 

  Takes an instantiated WordPressConnection object and returns 
  a list of hashmaps cooresponding to the WordPress APIs schema."

  [wordpress-connection]
  (:body (client/get
          (build-api-endpoint (:url wordpress-connection) "/pages")
          {:basic-auth [(:username wordpress-connection)
                        (:password wordpress-connection)]
           :as :json})))

(defn get-page
  "Gets a single page from a wordpress-connection.

  Requires an instantiated WordPressConnection record to be passed 
  as well as a valid page-id based on WordPress's ID system. 

  May throw a clojure.lang.ExceptionInfo in the case
  that an inproper identifier was passed.

  Use the get-page-ids function to retrieve all pages on a given site."

  [wordpress-connection page-id]
  (:body (client/get
          (build-api-endpoint (:url wordpress-connection) (str "/pages/" page-id))
          {:basic-auth [(:username wordpress-connection)
                        (:password wordpress-connection)]
           :as :json})))

(defn get-page-content
  "Retrieves the content of a simple page from a wordpress-connection as text.
  Note that this retrieves the --rendered-- page content: unfortunately I 
  do not believe that the WordPress JSON API allows one to recieve raw text,
  but I believe for the most part that this is okay.

  Second aarity takes an instantiated WordPressConnection record as well as a 
  valid page-id based on WordPress's ID system and returns the !raw! content of the 
  page if rendered content is desired, the third aarity should be used.
  
  Third aarity takes an instantiated WordPressConnection record, a valid page-id,
  and the content render type one wishes to use: The current types that are returned
  by the WordPress JSON API are :rendered and :raw. :raw is only accessable when
  in the 'edit' context.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper page-id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-page-ids function to retrieve all pages on a given site."

  ([wordpress-connection page-id]
   (get-page-content wordpress-connection page-id :raw))

  ([wordpress-connection page-id content-type]
   (content-type
    (:content
     (:body (client/get
             (build-api-endpoint (:url wordpress-connection) (str "/pages/" page-id "?context=edit"))
             {:basic-auth [(:username wordpress-connection)
                           (:password wordpress-connection)]
              :as :json}))))))

(defn update-page
  "Uses an authenticated WordPressConnection and page id to update a page generically
  with a map of attributes to be updated.

  Takes an instantiated WordPressConnection, a valid page identifier, and a hashmap
  representing data to be associated onto the page.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper page-id was 
  passed.

  Use the get-page-ids function to retrieve all pages on a given site."
  [wordpress-connection page-id msg]
  (:body (client/post
          (build-api-endpoint (:url wordpress-connection) (str "/pages/" page-id))
          {:basic-auth [(:username wordpress-connection)
                        (:password wordpress-connection)]
           :form-params msg
           :content-type :json})))

