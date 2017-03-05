(ns clo-wp.tag
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clo-wp.core :refer :all]))

(defn get-tags
  "Gets all the tags from a wordpress-connection.

  Takes an instantiated WordPressConnection object and returns 
  a list of hashmaps cooresponding to the WordPress APIs 
  tag schema."

  [wordpress-connection]
  (:body (get-from-wordpress wordpress-connection "/tags/")))

(defn get-tag-ids
  "Gets all the tag ids that a WordPress site currently has.

  Takes an instantiated WordPressConnection record and returns
  a vector of integers representing tag tag entries."

  [wordpress-connection]
  (->> wordpress-connection
       get-tags
       (map :id)
       (into [])))

(defn get-tag-names
  "Gets all the tag names that a WordPress site currently has.

  First aarity takes an instantiated WordPressConnection record and returns
  a vector of strings representing raw tag names. This is usually what
  you will want to use.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to render the names.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed."

  ([wordpress-connection]
   (get-tag-names wordpress-connection :raw))

  ([wordpress-connection display-type]
   (->> wordpress-connection
        get-tags
        (map :name)
        (into []))))

(defn- extract-tag-mapping-item
  "Utility function to generate key value pairs in get-tag-mapping"
  [item] [(keyword (str (:id item))) (:name item)])

(defn get-tag-mapping
  "Creates a mapping of tag identifiers to tag names. Useful in contexts
  in which we must explicitly associate the two. It is in general bad to
  flip the key value pairs returned by this function because WordPress allows
  multiple tags with unique identifiers to have the same names. Aka, 
  this map need not be one-to-one.

  *IMPORTANT* This returns a key-value mapping of keywordized integers and 
  strings, not integers and strings!

  First aarity takes an instantiated WordPressConnection record and returns
  map of ids to raw tag names. This is usually the aarity that you will
  want to use.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to render the names.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed. In general, it is best to use the single aarity unless you
  know what your doing!"

  ([wordpress-connection]
   (->> wordpress-connection
        get-tags
        (map extract-tag-mapping-item)
        (into {}))))

;; TODO: ALLOW KEYWORD INDEXING ON GET ITEMS!

(defn get-tag
  "Gets a single tag from a wordpress-connection.

  Requires an instantiated WordPressConnection record to be passed 
  as well as a valid tag-id based on WordPress's ID system. 

  May throw a clojure.lang.ExceptionInfo in the case
  that an inproper identifier was passed.

  Use the get-tag-ids function to retrieve all tags for any given instantiated WordPressConnection."

  [wordpress-connection tag-id]
  (:body (get-from-wordpress wordpress-connection (str "/tags/" tag-id))))

(defn get-tag-description
  "Retrieves the description of a tag from a wordpress-connection as text.
  
  Takes an instantiated WordPressConnection record as well as a valid tag id,
  returns a tag object.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper tag-id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-tag-ids function to retrieve all tag for any given instantiated WordPressConnection."

  ([wordpress-connection tag-id]
   (->> tag-id
        (get-tag wordpress-connection)
        :description)))

(defn get-tag-name
  "Retrieves the content of a simple tag from a wordpress-connection as text.

  Second aarity takes an instantiated WordPressConnection record as well as a 
  valid tag-id based on WordPress's ID system and returns the !raw! content of the 
  tag if rendered content is desired, the third aarity should be used.
  
  Third aarity takes an instantiated WordPressConnection record, a valid tag-id,
  and the content render type one wishes to use: The current types that are returned
  by the WordPress JSON API are :rendered and :raw. :raw is only accessable when
  in the 'edit' context.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper tag-id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-tag-ids function to retrieve all tags for any given instantiated WordPressConnection."

  ([wordpress-connection tag-id]
   (->> tag-id
        (get-tag wordpress-connection)
        :name)))

(defn create-tag
  "Uses an authenticated WordPressConnection to generate a new tag.

  Second aarity takes an instantiated WordPressConnection, and a hashmap 
  representing data to instantiate a new tag with.

  Third aarity represents the usual use case in which the user does not
  care about the slug, parent, and meta-data of a tag. it takes an instantiated 
  WordPressConnection, a name, and a description.

  All aarities return the json representation of the new tag.

  May throw a clojure.lang.ExceptionInfo in the case that the tag name
  already exists."

  ([wordpress-connection attrs]
   (:body (post-to-wordpress wordpress-connection (str "/tags") attrs)))
  ([wordpress-connection name description]
   (create-tag wordpress-connection {:name name :description description})))

(defn update-tag
  "Uses an authenticated WordPressConnection and tag id to update a tag generically
  with a map of attributes to be updated.

  Takes an instantiated WordPressConnection, a valid tag identifier, and a hashmap
  representing data to be associated onto the tag.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper tag id was 
  passed.

  Use the get-tag-ids function to retrieve all tags for any given instantiated WordPressConnection."

  [wordpress-connection tag-id data]
  (:body (post-to-wordpress wordpress-connection (str "/tags/" tag-id) data)))

(defn update-tag-name
  "Uses an authenticated WordPressConnection and tag id to only update a tag's name.

  Takes an instantiated WordPressConnection, a valid tag identifier, and a string
  representing the title to be applied to a tag.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper tag-id was 
  passed.

  Use the get-tag-ids function to retrieve all tags for any given instantiated WordPressConnection."

  [wordpress-connection tag-id name]
  (update-tag wordpress-connection tag-id {:name name}))

(defn delete-tag
  "Uses an authenticated WordPressConnection and tag id to delete a tag.

  Takes an instantiated WordPressConnection and a valid tag identifier.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper tag-id was 
  passed.

  Returns the entire tag object in order to make use a little bit less risky!
  Still, use this function with caution!  

  Use the get-tag-ids function to retrieve all tag for any given instantiated WordPressConnection."

  [wordpress-connection tag-id]
  (:body (delete-from-wordpress wordpress-connection (str "/tags/" tag-id))))

;; TODO: CHECK IF TAGS DO REVISIONS AS WELL
