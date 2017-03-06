(ns clo-wp.user
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clo-wp.core :refer :all]))

(defn get-users
  "Gets all the users from a wordpress-connection.

  Takes an instantiated WordPressConnection object and returns 
  a list of hashmaps cooresponding to the WordPress APIs user schema."

  [wordpress-connection]
  (:body (get-from-wordpress wordpress-connection "/users/")))

(defn get-user-ids
  "Gets all the users ids that a WordPress site currently has.

  Takes an instantiated WordPressConnection record and returns
  a vector of integers representing user IDs."

  [wordpress-connection]
  (->> wordpress-connection
       get-users
       (map :id)
       (into [])))

(defn get-user-names
  "Gets all the users titles that a WordPress site currently has.

  First aarity takes an instantiated WordPressConnection record and returns
  a vector of strings representing raw user titles. This is usually what
  you will want to use.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to render the title.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed."

  ([wordpress-connection]
   (->> wordpress-connection
        get-users
        (map :name)
        (into []))))

(defn- extract-user-mapping-item
  "Utility function to generate key value pairs in get-user-mapping"
  [x] [(keyword (str (:id x))) (:name x)])

(defn get-user-mapping
  "Creates a mapping of user identifiers to usernames. Useful in contexts
  in which we must explicitly associate the two. 

  *IMPORTANT* This returns a key-value mapping of keywordized integers and 
  strings, not integers and strings!!

  Takes an instantiated WordPressConnection record, returns a mapping 
  of ids to usernames.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed."

  ([wordpress-connection]
   (->> wordpress-connection
        get-users
        (map extract-user-mapping-item)
        (into {})
        clojure.walk/keywordize-keys)))

(defn get-user
  "Gets a single user item from a wordpress-connection.

  Requires an instantiated WordPressConnection record to be passed 
  as well as a valid user id based on WordPress's ID system. 

  May throw a clojure.lang.ExceptionInfo in the case
  that an inproper memdia identifier was passed.

  Use the get-user-ids function to retrieve all users for any given instantiated WordPressConnection."

  [wordpress-connection user-id]
  (:body (get-from-wordpress wordpress-connection (str "/users/" user-id))))

(defn get-user-email
  "Retrieves the url of a simple user from a WordpressConnection instance as text.

  Second aarity takes an instantiated WordPressConnection record as well as a 
  valid user id based on WordPress's ID system and returns the raw content of the 
  user if rendered content is desired, the third aarity should be used.
  
  Third aarity takes an instantiated WordPressConnection record, a valid user-id,
  and the content render type one wishes to use: The current types that are returned
  by the WordPress JSON API are :rendered and :raw. :raw is only accessable when
  in the 'edit' context.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper user-id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-user-ids function to retrieve all users for any given instantiated WordPressConnection."

  ([wordpress-connection user-id]
   (:email (get-user wordpress-connection user-id))))

(defn get-user-real-names
  "Retrieves the title of a simple user from a wordpress-connection as text.

  Second aarity takes an instantiated WordPressConnection record as well as a 
  valid user id based on WordPress's ID system and returns the raw content of the 
  user if rendered content is desired, the third aarity should be used. In turn
  returns a said users title.
  
  Third aarity takes an instantiated WordPressConnection record, a valid user-id,
  and the content render type one wishes to use: The current types that are returned
  by the WordPress JSON API are :rendered and :raw. :raw is only accessable when
  in the 'edit' context. In turn returns said users title.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper user-id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-user-ids function to retrieve all users for any given instantiated WordPressConnection."

  ([wordpress-connection user-id]
   (let [user-data (get-user wordpress-connection user-id)]
    [(:first_name user-data)
     (:first_name user-data)])))

(defn update-user
  "Uses an authenticated WordPressConnection and user id to update a user generically
  with a map of attributes to be updated.

  Takes an instantiated WordPressConnection, a valid user identifier, and a hashmap
  representing data to be associated onto the user.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper user-id was 
  passed.

  Use the get-user-ids function to retrieve all users for any given instantiated WordPressConnection."

  [wordpress-connection user-id data]
  (:body (post-to-wordpress wordpress-connection (str "/users/" user-id) data)))

(defn create-user
  "Uses an authenticated WordPressConnection to generate a new user.

  Second aarity takes an instantiated WordPressConnection, and a hashmap 
  representing data to instantiate a new user user with.

  Third aarity represents the usual use case in which the user does not
  care about the status of a user (and as a result will default to publish)
  it takes an instantiated WordPressConnection, a title, and a content.

  Fourth aarity represents the usual use case that takes an instantiated 
  WordPressConnection, a title, a content, and the status which can be either 
  :publish, :future, :draft, :pending, or :private.
  
  *Note on fourth aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper status was 
  passed. The other two aarities are quite safe, but make sure you are using the
  only the status types which your WordPress version supports if this aarity is used!

  All aarities return the the json representation of the new user.

  You probably don't want to use this: Use a different library for creation of
  user on a WordPress site. Only use this to modify current user. Still, 
  use at your own risk!"

  ([wordpress-connection attrs]
   (:body (post-to-wordpress wordpress-connection (str "/users/") attrs)))

  ([wordpress-connection email username password]
   (create-user wordpress-connection {:username username :password password :email email})))


(defn delete-user
  "Uses an authenticated WordPressConnection and user id to delete a user.

  Takes an instantiated WordPressConnection and a valid user identifier.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper user-id was 
  passed.

  Returns the entire user object in order to make use a little bit less risky!
  Still, use this function with caution!  

  Use the get-user-ids function to retrieve all users for any given instantiated WordPressConnection."

  [wordpress-connection user-id reassign]
  (:body (delete-from-wordpress wordpress-connection (str "/users/" user-id "") (str "&reassign=" reassign))))
