(ns clo-wp.type
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clo-wp.core :refer :all]))

(defn get-types
  "Gets all the types from a wordpress-connection.

  Takes an instantiated WordPressConnection object and returns 
  a list of hashmaps cooresponding to the WordPress APIs 
  type schema."

  [wordpress-connection]
  (:body (get-from-wordpress wordpress-connection "/types/")))

(defn get-type-names
  "Gets all the type names that a WordPress site currently has.

  First aarity takes an instantiated WordPressConnection record and returns
  a vector of strings representing raw type names. This is usually what
  you will want to use.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:key, :string, :name) which will determine how to fetch the 
  type name.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper fetch
  type was passed."

  ([wordpress-connection as]
   (cond
     (= as :key) (get-type-names wordpress-connection)
     (= as :string) (->> wordpress-connection
                         get-type-names
                         (map name)
                         (into []))
     (= as :name) (->> wordpress-connection
                       get-type-names
                       (map #(% (get-types wordpress-connection)))
                       (map #(get-in % [:labels :name]))
                       (into []))
     :else (throw (IllegalArgumentException. "get-type-names only takes :key, :string, or :name as its second argument."))))
  ([wordpress-connection]
   (into [] (keys (get-types wordpress-connection)))))

;; TODO: VALIDATE
(defn get-type-mapping
  "Creates a mapping of type identifiers to type links. 

  Takes an instantiated WordPressConnection record and returns
  map of type keywords to type urls.

  I am not sure of the reliability of this function: It needs more tests."

  ([wordpress-connection]
   (->> wordpress-connection
        get-type-names
        (map #(vector % (:href (first (get-in (% (get-types wordpress-connection)) [:_links :wp:items])))))
        (into {}))))

(defn get-type
  "Gets a single type from a wordpress-connection.

  Requires an instantiated WordPressConnection record to be passed 
  as well as a valid type name as a keyword. 

  May throw a clojure.lang.ExceptionInfo in the case
  that an inproper type keyword was passed.

  se the get-type-names function to retrieve all types for any given instantiated WordPressConnection."

  [wordpress-connection type-kw]
  (:body (get-from-wordpress wordpress-connection (str "/types/" (name type-kw)))))

(defn get-type-description
  "Retrieves the description of a type from a wordpress-connection as text.
  
  Takes an instantiated WordPressConnection record as well as a valid type keyword,
  returns a type hash map.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper type keyword
  was passed. 

  Use the get-type-names function to retrieve all type names for any given instantiated WordPressConnection."

  ;; TODO: CLEAN UP THREADING: UNNECCISSAR
  ([wordpress-connection type-keyword]
   (->> (name type-keyword)
        (get-type wordpress-connection)
        :description)))
