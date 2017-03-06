(ns clo-wp.comment
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clo-wp.core :refer :all]))

(defn get-comments
  "Gets all the comments from a wordpress-connection.

  Takes an instantiated WordPressConnection object and returns 
  a list of hashmaps cooresponding to the WordPress APIs 
  comment schema."

  [wordpress-connection]
  (:body (get-from-wordpress wordpress-connection "/comments/")))

(defn get-comment-ids
  "Gets all the comment ids that a WordPress site currently has.

  Takes an instantiated WordPressConnection record and returns
  a vector of integers representing comment comment entries."

  [wordpress-connection]
  (->> wordpress-connection
       get-comments
       (map :id)
       (into [])))

(defn get-comment-links
  "Gets all the comment names that a WordPress site currently has.

  First aarity takes an instantiated WordPressConnection record and returns
  a vector of strings representing raw comment names. This is usually what
  you will want to use.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to render the names.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed."

  ([wordpress-connection]
   (->> wordpress-connection
        get-comments
        (map :link)
        (into []))))

(defn- extract-comment-mapping-item
  "Utility function to generate key value pairs in get-comment-mapping"
  [item] [(keyword (str (:id item))) (:link item)])

(defn get-comment-mapping
  "Creates a mapping of comment identifiers to comment names. Useful in contexts
  in which we must explicitly associate the two. It is in general bad to
  flip the key value pairs returned by this function because WordPress allows
  multiple comments identifiers to have the same names. Aka, 
  this map need not be one-to-one.

  *IMPORTANT* This returns a key-value mapping of keywordized integers and 
  strings, not integers and strings!

  First aarity takes an instantiated WordPressConnection record and returns
  map of ids to raw comment names. This is usually the aarity that you will
  want to use.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to render the names.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed. In general, it is best to use the single aarity unless you
  know what your doing!"

  ([wordpress-connection]
   (->> wordpress-connection
        get-comments
        (map extract-comment-mapping-item)
        (into {}))))

(defn get-comment
  "Gets a single comment from a wordpress-connection.

  Requires an instantiated WordPressConnection record to be passed 
  as well as a valid comment-id based on WordPress's ID system. 

  May throw a clojure.lang.ExceptionInfo in the case
  that an inproper identifier was passed.

  Use the get-comment-ids function to retrieve all comments for any given instantiated WordPressConnection."

  [wordpress-connection comment-id]
  (:body (get-from-wordpress wordpress-connection (str "/comments/" comment-id))))

(defn get-comment-content
  "Retrieves the description of a comment from a wordpress-connection as text.
  
  Takes an instantiated WordPressConnection record as well as a valid comment id,
  returns a comment object.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper comment-id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-comment-ids function to retrieve all comment for any given instantiated WordPressConnection."

  ([wordpress-connection comment-id]
   (->> comment-id
        (get-comment wordpress-connection)
        :description)))

(defn get-comment-content
  "Retrieves the content of a content from an instantied WordPressConnection as text.

  Second aarity takes an instantiated WordPressConnection record as well as a 
  valid comment-id based on WordPress's ID system and returns the !raw! content of the 
  comment if rendered content is desired, the third aarity should be used. In turn
  returns a said comment's title.
  
  Third aarity takes an instantiated WordPressConnection record, a valid comment id,
  and the content render type one wishes to use: The current types that are returned
  by the WordPress JSON API are :rendered and :raw. :raw is only accessable when
  in the 'edit' context. In turn returns said comment's title.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper comment id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-comment-ids function to retrieve all comments for any given instantiated WordPressConnection."

  ([wordpress-connection comment-id]
   (get-comment-content wordpress-connection comment-id :raw))

  ([wordpress-connection comment-id content-type]
   (content-type
    (:content
     (get-comment wordpress-connection comment-id)))))

(defn create-comment
  "Uses an authenticated WordPressConnection to generate a new comment.

  Second aarity takes an instantiated WordPressConnection, and a hashmap 
  representing data to instantiate a new comment with.

  Fourth aarity represents the usual use case in which the user simply
  wishes to post a comment to a certain post id. Takes a post which
  is a number, the authors name, and the content one wishes to push
  to the comment. Returns the new comment.

  All aarities return the json representation of the new comment.

  May throw a clojure.lang.ExceptionInfo in the case that the post is not
  allowing any more comments Also may throw in the case that the post 
  simply does not exist."

  ([wordpress-connection attrs]
   (:body (post-to-wordpress wordpress-connection (str "/comments") attrs)))

  ([wordpress-connection post author-name content]
   (create-comment wordpress-connection {:content content :author_name author-name :post post})))

(defn update-comment
  "Uses an authenticated WordPressConnection and comment id to update a comment generically
  with a map of attributes to be updated.

  Takes an instantiated WordPressConnection, a valid comment identifier, and a hashmap
  representing data to be associated onto the comment.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper comment id was 
  passed.

  Use the get-comment-ids function to retrieve all comments for any given instantiated WordPressConnection."

  [wordpress-connection comment-id data]
  (:body (post-to-wordpress wordpress-connection (str "/comments/" comment-id) data)))

(defn update-comment-content
  "Uses an authenticated WordPressConnection and comment id to only update a comment's name.

  Takes an instantiated WordPressConnection, a valid comment identifier, and a string
  representing the title to be applied to a comment.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper comment-id was 
  passed.

  Use the get-comment-ids function to retrieve all comments for any given instantiated WordPressConnection."

  [wordpress-connection comment-id content]
  (update-comment wordpress-connection comment-id {:content content}))

(defn delete-comment
  "Uses an authenticated WordPressConnection and comment id to delete a comment.

  Takes an instantiated WordPressConnection and a valid comment identifier.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper comment-id was 
  passed.

  Returns the entire comment object in order to make use a little bit less risky!
  Still, use this function with caution!  

  Use the get-comment-ids function to retrieve all comment for any given instantiated WordPressConnection."

  [wordpress-connection comment-id]
  (:body (delete-from-wordpress wordpress-connection (str "/comments/" comment-id))))
