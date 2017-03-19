(ns clo-wp.core
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]))

(defrecord WordPressConnection [url username password])

;; Utilities for building queries to WordPress API

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

(defn connect
  "Attempts to connect to a site which has WordPress installed. Takes a 
  url, username, and password and either throws in the case some issue
  with said credentials is encountered, or gives back a new WordPressConnection."

  [url username password]
  (if (or (clojure.string/includes? url "http://") (clojure.string/includes? url "https://"))
    (if (has-wordpress-api url)
        (->WordPressConnection url username password)
        (throw (IllegalArgumentException. "This URL does not seem to have a cooresponding WordPress API or does not exist.")))
    (throw (IllegalArgumentException. "URL did not include http header: Try using the fully qualified URL."))))

;; Functions for sending http methods to WordPress endpoints.

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
       ()
       (if (= (get-in response [:_links :collection]) nil)
         [(into {} (:body response))]
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
   (:body (client/delete
    (build-api-endpoint (:url wordpress-connection) (str endpoint-path "?context=" (name context) "&force=true"))
    {:basic-auth
     [(:username wordpress-connection)
      (:password wordpress-connection)]
     :content-type :json
     :as :json})))
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
   (:body (client/post
    (build-api-endpoint (:url wordpress-connection) (str endpoint-path "?context=" (name context)))
    {:basic-auth
     [(:username wordpress-connection)
      (:password wordpress-connection)]
     :form-params data
     :content-type :json
     :as :json})))
   ([wordpress-connection endpoint-path data]
    (post-to-wordpress wordpress-connection endpoint-path :edit data)))

;; Functions for helping with path deductions from Clojure keywords.

(defmulti str-item
  (fn [item]))

(defmethod str-item String [s]
  s)

(defmethod str-item :default [s]
  (name s))

(defn special-stringize
  [x]
  (if (number? x)
    (str x)
    x))

(defn endpathize
  [items] (str "/" (clojure.string/join
           "/"
           (map
            (comp
             str-item
             special-stringize)
            items))))

(defn url-extraction-scheme
  [item]
  (:href (first (get-in item [:_links :self]))))

;; Functions for abstracting API calls into a single method (Used in defwp macro.)
;; Work as lazy iterators in conjunction with http calls.

(defn api-getter
  ([wordpress-connection resource]
    (into [] (doall (get-from-wordpress wordpress-connection (endpathize resource)))))
  ([wordpress-connection endpoint-path extraction-items]
   (let [response (api-getter wordpress-connection endpoint-path)]
     (if (vector? response)
       (into [] (map #(get-in % extraction-items) response))
       (if (map? response)
         (get-in response extraction-items))))))

(defn api-mapper
  ([wordpress-connection resource key-extraction-item value-extraction-item]
   (if (vector? resource)
     (clojure.walk/keywordize-keys
       (into {}
         (map #(identity [(get-in % key-extraction-item) (get-in % value-extraction-item)])
              (api-getter wordpress-connection resource))))
     (if (map? resource)
       (into {}
         (map #(identity [(get-in % key-extraction-item) (get-in % value-extraction-item)])
              resource))))))

(defn api-deleter
  ([wordpress-connection resource]
   (if (map? resource)
     (delete-from-wordpress wordpress-connection (url-extraction-scheme resource))
     (if (vector? resource)
       (delete-from-wordpress wordpress-connection (endpathize resource))
       (throw (IllegalArgumentException. "Resource passed to api-deleter must be path or resource item."))))))

(defn api-updater
  ([wordpress-connection resource]
   (api-updater wordpress-connection (url-extraction-scheme resource) resource))
  ([wordpress-connection endpoint-path data]
   (post-to-wordpress wordpress-connection (endpathize endpoint-path) data))
  ([wordpress-connection endpoint-path item-key item-value]
   (api-updater wordpress-connection endpoint-path {item-key item-value})))

;; Generic API Information getters.

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

;; Methods for generating specific http calls to specific endpoints.

(defn get-resource
  "Takes an WordPressConnection and 
  gets all the content-item resources as JSON."

  ([resource-names wordpress-connection]
   (api-getter wordpress-connection resource-names)))

(defn post-resource
  "Takes an authenticated WordPressConnection and content-item id to update a content-item 
  with a map of attributes to be updated. Read the WordPress API documentation
  to see information on the schema."

  ([resource-names wordpress-connection arg-map]
   (api-updater wordpress-connection resource-names arg-map)))

(defn delete-resource
  "Takes an authenticated WordPressConnection and a content-item id and deletes a content-item.
  returns the JSON object of the now deleted content-item."

  ([resource-names wordpress-connection]
    (api-deleter wordpress-connection resource-names)))

(defn dispatch-resource
  ([resource-names wordpress-connection]
      (get-resource resource-names wordpress-connection))
  ([resource-names wordpress-connection arg-map]
      (post-resource resource-names wordpress-connection arg-map))
  ([resource-names wordpress-connection arg-map method]
    (case method
      :get (get-resource resource-names wordpress-connection)
      :post (post-resource resource-names wordpress-connection arg-map)
      :put (post-resource resource-names wordpress-connection arg-map)
      :delete (delete-resource resource-names wordpress-connection)))
  ([content-items wordpress-connection arg-map method callback]
    (callback (dispatch-resource content-items wordpress-connection arg-map method))))

;; The bread and butter: Allows us to define an endpoint and extract information from it.

(defmacro defwp
  ([endpoint-name path]
  (let [param-list (into [] (concat ['method 'wordpress-connection] (into [] (filter symbol? path)) ['& 'args]))
       param-list-default (into [] (concat ['wordpress-connection] (into [] (filter symbol? path))))]
  `(do
    (defn ~endpoint-name
      (~param-list
         (dispatch-resource ~path ~'wordpress-connection (into {} ~'args) ~'method))
      (~param-list-default
       (dispatch-resource ~path ~'wordpress-connection {} :get))))))
  ([endpoint-name path callback]
  (let [param-list (into [] (concat ['method 'wordpress-connection] (into [] (filter symbol? path)) ['& 'args]))
       param-list-default (into [] (concat ['wordpress-connection] (into [] (filter symbol? path))))]
  `(do
    (defn ~endpoint-name
      (~param-list
         (~callback (dispatch-resource ~path ~'wordpress-connection (into {} ~'args) ~'method)))
      (~param-list-default
         (~callback (dispatch-resource ~path ~'wordpress-connection {} :get))))))))

