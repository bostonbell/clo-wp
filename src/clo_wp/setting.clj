(ns clo-wp.setting
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clo-wp.core :refer :all]))

(defn get-settings
  "Gets all the settings from a wordpress-connection.

  Takes an instantiated WordPressConnection object and returns 
  a list of hashmaps cooresponding to the WordPress APIs setting schema."

  [wordpress-connection]
  (:body (get-from-wordpress wordpress-connection "/settings/")))

(defn get-setting-items
  "Gets all the settings ids that a WordPress site currently has.

  Takes an instantiated WordPressConnection record and returns
  a vector of integers representing setting IDs."

  [wordpress-connection]
  (set  (keys (get-settings wordpress-connection))))

(defn get-setting
  "Gets a single setting from a wordpress-connection.

  Requires an instantiated WordPressConnection record to be passed 
  as well as a valid setting-id based on WordPress's ID system. 

  May throw a clojure.lang.ExceptionInfo in the case
  that an inproper identifier was passed.

  Use the get-setting-ids function to retrieve all settings for any given instantiated WordPressConnection."

  [wordpress-connection setting-kw]
  (setting-kw (get-settings wordpress-connection)))

;; TODO: ALWAYS CLOJURIZE KEYS (UNDERSCORE TO DASH)

(defn update-setting
  "Uses an authenticated WordPressConnection and setting id to update a setting generically
  with a map of attributes to be updated.

  Takes an instantiated WordPressConnection, a valid setting identifier, and a hashmap
  representing data to be associated onto the setting.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper setting-id was 
  passed.

  Use the get-setting-ids function to retrieve all settings for any given instantiated WordPressConnection."

  [wordpress-connection data]
  (:body (post-to-wordpress wordpress-connection "/settings/" data)))

