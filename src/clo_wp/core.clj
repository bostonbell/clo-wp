(ns clo-wp.core
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]))

(defn build-api-endpoint
  "Function used generally for internal use to build endpoint paths."
  ([url ep] (str url "/wp-json/wp/v2" ep))
  ([url] (str url "/wp-json/wp/v2")))

(defn has-wordpress-api
  "Takes a url and makes sure that the WP Rest API is on a website."
  [url]
  (print (build-api-endpoint url))
  (try
    (let [response (client/head (build-api-endpoint url) {:accept :json})]
      (client/success? response))
    (catch Exception e false)))

(defrecord WordPressConnection [url username password])

(defn connect
  "Attempts to connect to a site which has WordPress installed.

  Takes a url which must be properly formatted (see below), a username, and a password.
  Returns a WordPressConnection instance that we are pretty sure can be used in all the
  libraries functions.

  Note that the URL must be fully formatted: http:// or https:// must be included, otherwise
  an error will occur.

  In certain cases where the JSON api can not be found, an IllegalArgumentException will be 
  thrown, and likewise for not including an HTTP header."
  [url username password]
  (if (or (clojure.string/includes? url "http://") (clojure.string/includes? url "https://"))
    (if (has-wordpress-api url)
        (->WordPressConnection url username password)
        (throw (IllegalArgumentException. "This URL does not seem to have a cooresponding WordPress API or does not exist.")))
    (throw (IllegalArgumentException. "URL did not include http header: Try using the fully qualified URL."))))

;; TODO: Easier passing of query params.
(defn get-from-wordpress
  "Raw API call for receiving data from a site that has WordPress installed.
  
  Takes an instantiated WordPressConnection, the path of an endpoint (formatted with an initial /), and
  the context one wishes to access the endpoint with. Legitimate contexts include :view :edit and :embed.
  Certain contexts allow access to additional information in the response, for example allowing gets 
  on posts to recieve the raw markup (including shortcodes) when the edit parameter is passed instead of
  view, which will only return 'rendered' markup.

  Primary for internal use or for extending the library."

  ([wordpress-connection endpoint-path context page]
   (let [response (client/get
    (build-api-endpoint (:url wordpress-connection)
                        (str endpoint-path
                             "?context=" (name context)
                             "&page=" page
                             "&per_page=" 100))
    {:basic-auth
     [(:username wordpress-connection)
      (:password wordpress-connection)]
     :content-type :json
     :as :json})]
     (if (empty? (:body response))
       nil
       (if (= (get-in response [:_links :collection]) nil)
         (:body response)
         (lazy-seq (concat
           (:body response)
           (get-from-wordpress wordpress-connection
                            endpoint-path
                            context
                            (+ page 1))))))))
  ([wordpress-connection endpoint-path context]
   (get-from-wordpress wordpress-connection endpoint-path context 1))
   ([wordpress-connection endpoint-path]
    (get-from-wordpress wordpress-connection endpoint-path :edit)))

(defn delete-from-wordpress
  "Raw API call for deleting data from a site that has WordPress installed.

  Takes an instantiated WordPressConnection, the path of an endpoint (formatted with an initial /), and
  the context one wishes to access the endpoint with. Legitimate contexts include :view :edit and :embed.
  Certain contexts allow access to additional information in the response, for example allowing gets 
  on posts to recieve the raw markup (including shortcodes) when the edit parameter is passed instead of
  view, which will only return 'rendered' markup.

  Primary for internal use or for extending the library."

  ([wordpress-connection endpoint-path context]
   (client/delete
    (build-api-endpoint (:url wordpress-connection) (str endpoint-path "?context=" (name context) "&force=true"))
    {:basic-auth
     [(:username wordpress-connection)
      (:password wordpress-connection)]
     :content-type :json
     :as :json})
    )
   ([wordpress-connection endpoint-path]
    (delete-from-wordpress wordpress-connection endpoint-path :edit)))

(defn post-to-wordpress
  "Raw API call for posting data from a site that has WordPress installed.

  Takes an instantiated WordPressConnection, the path of an endpoint (formatted with an initial /), and
  the context one wishes to access the endpoint with. Legitimate contexts include :view :edit and :embed.
  Certain contexts allow access to additional information in the response, for example allowing gets 
  on posts to recieve the raw markup (including shortcodes) when the edit parameter is passed instead of
  view, which will only return 'rendered' markup.

  Primary for internal use or for extending the library."

  ([wordpress-connection endpoint-path context data]
   (client/post
    (build-api-endpoint (:url wordpress-connection) (str endpoint-path "?context=" (name context)))
    {:basic-auth
     [(:username wordpress-connection)
      (:password wordpress-connection)]
     :form-params data
     :content-type :json
     :as :json})
    )
   ([wordpress-connection endpoint-path data]
    (post-to-wordpress wordpress-connection endpoint-path :edit data)))

(defn get-site-information
  "Gets raw information about a site that has WordPress installed.

  Takes an instantiated WordPressConnection and returns the informations from
  the /wp-json endpoint for us.

  Generally not the most useful thing in the world unless checking for extensions
  on the site itself."

  [wordpress-connection]
  (:body (client/get
           (str (:url wordpress-connection) "/wp-json")
           {:basic-auth [(:username wordpress-connection)
                         (:password wordpress-connection)]
                          :as :json})))

(defn generate-id-getter
  "Gets all the ids from a given resource.

  Takes a callback to be used that will get information
  for a specific endpoint."

  [callback]
  (fn [wordpress-connection]
    (->> wordpress-connection
         callback
         (map :id)
         (into []))))


