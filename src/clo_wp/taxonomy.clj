(ns clo-wp.taxonomy
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clo-wp.core :refer :all]))

(defn get-taxonomies
  "Gets all the taxonomies from a wordpress-connection.

  Takes an instantiated WordPressConnection object and returns 
  a list of hashmaps cooresponding to the WordPress APIs 
  taxonomy schema."

  [wordpress-connection]
  (:body (get-from-wordpress wordpress-connection "/taxonomies/")))

(defn get-taxonomy-names
  "Gets all the taxonomy names that a WordPress site currently has.

  First aarity takes an instantiated WordPressConnection record and returns
  a vector of strings representing raw taxonomy names. This is usually what
  you will want to use.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:key, :string, :name) which will determine how to fetch the 
  taxonomy name.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper fetch
  type was passed."

  ([wordpress-connection as]
   (cond
     (= as :key) (get-taxonomy-names wordpress-connection)
     (= as :string) (->> wordpress-connection
                         get-taxonomy-names
                         (map name)
                         (into []))
     (= as :name) (->> wordpress-connection
                       get-taxonomy-names
                       (map #(% (get-taxonomies wordpress-connection)))
                       (map #(get-in % [:labels :name]))
                       (into []))
     :else (throw (IllegalArgumentException. "get-taxonomy-names only takes :key, :string, or :name as its second argument."))))
  ([wordpress-connection]
   (into [] (keys (get-taxonomies wordpress-connection)))))

;; TODO: VALIDATE
(defn get-taxonomy-mapping
  "Creates a mapping of taxonomy identifiers to taxonomy links. 

  Takes an instantiated WordPressConnection record and returns
  map of taxonomy keywords to taxonomy urls.

  I am not sure of the reliability of this function: It needs more tests."

  ([wordpress-connection]
   (->> wordpress-connection
        get-taxonomy-names
        (map #(vector % (:href (first (get-in (% (get-taxonomies wordpress-connection)) [:_links :wp:items])))))
        (into {}))))

(defn get-taxonomy
  "Gets a single taxonomy from a wordpress-connection.

  Requires an instantiated WordPressConnection record to be passed 
  as well as a valid taxonomy name as a keyword. 

  May throw a clojure.lang.ExceptionInfo in the case
  that an inproper taxonomy keyword was passed.

  Use the get-taxonomy-names function to retrieve all taxonomies for any given instantiated WordPressConnection."

  [wordpress-connection taxonomy-kw]
  (:body (get-from-wordpress wordpress-connection (str "/taxonomies/" (name taxonomy-kw)))))

(defn get-taxonomy-description
  "Retrieves the description of a taxonomy from a wordpress-connection as text.
  
  Takes an instantiated WordPressConnection record as well as a valid taxonomy keyword,
  returns a taxonomy hash map.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper taxonomy keyword
  was passed. 

  Use the get-taxonomy-names function to retrieve all taxonomy names for any given instantiated WordPressConnection."

  ([wordpress-connection taxonomy-keyword]
   (->> (name taxonomy-keyword)
        (get-taxonomy wordpress-connection)
        :description)))
