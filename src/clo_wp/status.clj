(ns clo-wp.status
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clo-wp.core :refer :all]))

(defn get-statuses
  "Gets all the statuses from a wordpress-connection.

  Takes an instantiated WordPressConnection object and returns 
  a list of hashmaps cooresponding to the WordPress APIs 
  status schema."

  [wordpress-connection]
  (:body (get-from-wordpress wordpress-connection "/statuses/")))

(defn get-status-names
  "Gets all the status names that a WordPress site currently has.

  First aarity takes an instantiated WordPressConnection record and returns
  a vector of strings representing raw status names. This is usually what
  you will want to use.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:key, :string, :name) which will determine how to fetch the 
  status name.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper fetch
  status was passed."

  ([wordpress-connection as]
   (cond
     (= as :key) (get-status-names wordpress-connection)
     (= as :string) (->> wordpress-connection
                         get-status-names
                         (map name)
                         (into []))
     :else (throw (IllegalArgumentException. "get-status-names only takes :key, :string, or :name as its second argument."))))
  ([wordpress-connection]
   (into [] (keys (get-statuses wordpress-connection)))))

(defn get-status-mapping
  "Creates a mapping of type identifiers to type links. 

  Takes an instantiated WordPressConnection record and returns
  map of type keywords to type urls.

  I am not sure of the reliability of this function: It needs more tests."

  ([wordpress-connection]
   (->> wordpress-connection
        get-status-names
        (map #(vector % (:href (first (get-in (% (get-statuses wordpress-connection)) [:_links :archives])))))
        (into {}))))

((defn get-status
  "Gets a single status from a wordpress-connection.

  Requires an instantiated WordPressConnection record to be passed 
  as well as a valid status name as a keyword. 

  May throw a clojure.lang.ExceptionInfo in the case
  that an inproper status keyword was passed.

  se the get-status-names function to retrieve all statuses for any given instantiated WordPressConnection."

  [wordpress-connection status-kw]
  (:body (get-from-wordpress wordpress-connection (str "/statuses/" (name status-kw)))))
