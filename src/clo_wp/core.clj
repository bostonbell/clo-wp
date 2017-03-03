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

(defn get-page-titles
  "Gets all the pages titles that a WordPress site currently has.

  First aarity takes an instantiated WordPressConnection record and returns
  a vector of strings representing rendered page titles.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to give the title, 
  this is neccessary because WordPress renders titles via a macro system.

  In general, the first aarity is what you will want to use unless there
  is some reason not to.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed. In general, it is best to use the single aarity unless you
  know what your doing!"

  ([wordpress-connection]
   (get-page-titles wordpress-connection :rendered))

  ([wordpress-connection display-type]
   (into []
         (map display-type
          (map :title
               (:body (client/get
                       (build-api-endpoint (:url wordpress-connection) "/pages?context=edit")
                       {:basic-auth [(:username wordpress-connection)
                                     (:password wordpress-connection)]
                        :as :json})))))))


(defn get-pages
  "Gets all the pages from a wordpress-connection. 

  Takes an instantiated WordPressConnection object and returns 
  a list of hashmaps cooresponding to the WordPress APIs schema."

  [wordpress-connection]
  (:body (client/get
          (build-api-endpoint (:url wordpress-connection) "/pages")
          {:basic-auth [(:username wordpress-connection)
                        (:password wordpress-connection)]
           :content-type :json
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
           :content-type :json
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
              :content-type :json
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
          (build-api-endpoint (:url wordpress-connection) (str "/pages/" page-id "?context=edit"))
          {:basic-auth [(:username wordpress-connection)
                        (:password wordpress-connection)]
           :form-params msg
           :as :json
           :content-type :json})))

(defn update-page-content
  "Uses an authenticated WordPressConnection and page id to only update a pages content.

  Takes an instantiated WordPressConnection, a valid page identifier, and a string
  representing raw content to be applied to a page.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper page-id was 
  passed.

  Use the get-page-ids function to retrieve all pages on a given site."

  [wordpress-connection page-id content]
  (:body (client/post
          (build-api-endpoint (:url wordpress-connection) (str "/pages/" page-id "?context=edit"))
          {:basic-auth [(:username wordpress-connection)
                        (:password wordpress-connection)]
           :form-params {:content content}
           :as :json
           :content-type :json})))

(defn create-page
  "Uses an authenticated WordPressConnection to generate a new page.

  Second aarity takes an instantiated WordPressConnection, and a hashmap 
  representing data to instantiate a new page page with.

  Third aarity represents the usual use case in which the user does not
  care about the status of a post (and as a result will default to publish)
  it takes an instantiated WordPressConnection, a title, and a content.

  Fourth aarity represents the usual use case that takes an instantiated 
  WordPressConnection, a title, a content, and the status which can be either 
  :publish, :future, :draft, :pending, or :private.
  
  *Note on fourth aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper status was 
  passed. The other two aarities are quite safe, but make sure you are using the
  only the status types which your WordPress version supports if this aarity is used!

  All aarities return the identifier of the new page."

  ([wordpress-connection attrs]
   (:id
    (:body (client/post
            (build-api-endpoint (:url wordpress-connection) (str "/pages?context=edit"))
            {:basic-auth [(:username wordpress-connection)
                          (:password wordpress-connection)]
             :form-params attrs
             :as :json
             :content-type :json}))))
  ([wordpress-connection title content]
   (create-page wordpress-connection {:title title :content content :status :publish}))
  ([wordpress-connection title content status]
   (create-page wordpress-connection {:title title :content content :status status})))

(defn delete-page
  "Uses an authenticated WordPressConnection and page id to delete a page.

  Takes an instantiated WordPressConnection and a valid page identifier.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper page-id was 
  passed.

  Returns the entire page object in order to make use a little bit less risky!
  Still, use this function with caution!  

  Use the get-page-ids function to retrieve all pages on a given site."

  [wordpress-connection page-id]
  (:body (client/delete
          (build-api-endpoint (:url wordpress-connection) (str "/pages/" page-id "?context=edit"))
          {:basic-auth [(:username wordpress-connection)
                        (:password wordpress-connection)]
           :as :json
           :content-type :json})))

