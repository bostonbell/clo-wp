(ns clo-wp.post
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clo-wp.core :refer :all]))

(defn get-posts
  "Gets all the posts from a wordpress-connection. 

  Takes an instantiated WordPressConnection object and returns 
  a list of hashmaps cooresponding to the WordPress APIs schema."

  [wordpress-connection]
  (:body (get-from-wordpress wordpress-connection "/posts/")))

(defn get-post-ids
  "Gets all the posts ids that a WordPress site currently has.

  Takes an instantiated WordPressConnection record and returns
  a vector of integers representing post IDs."

  [wordpress-connection]
  (into [] (map :id (get-posts wordpress-connection))))

(defn get-post-titles
  "Gets all the posts titles that a WordPress site currently has.

  First aarity takes an instantiated WordPressConnection record and returns
  a vector of strings representing raw post titles.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to give the title, 
  this is neccessary because WordPress renders titles via a macro system and
  in turn returns a vector of strings representing generic post titles.

  In general, the first aarity is what you will want to use unless there
  is some reason not to.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed. In general, it is best to use the single aarity unless you
  know what your doing!"

  ([wordpress-connection]
   (get-post-titles wordpress-connection :raw))

  ; TODO: Refactor using threading macros.
  ([wordpress-connection display-type]
   (into [] (map display-type (map :title (get-posts wordpress-connection))))))

(defn- extract-post-mapping-item
  "Utility function to generate key value pairs in get-post-mapping"
  [x display-type] [(keyword (str (:id x))) (display-type (:title x))])

(defn get-post-mapping
  "Creates a mapping of post identifiers to post titles. Useful in contexts
  in which we must explicitly associate the two. It is in general bad to
  flip the key value pairs returned by this function because WordPress allows
  multiple posts with unique identifiers to have the same titles. Aka, 
  this map need not be one-to-one.

  *IMPORTANT* This returns a key-value mapping of keywordized integers and 
  strings, not integers and strings!!

  First aarity takes an instantiated WordPressConnection record and returns
  map of ids to raw post names.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to output the titles, 
  this is neccessary because WordPress renders titles via a macro system.
  In turn, a map of ids to generic post names will be returned.

  In general, the first aarity is what you will want to use unless there
  is some reason not to.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed. In general, it is best to use the single aarity unless you
  know what your doing!"

  ([wordpress-connection]
   (get-post-mapping wordpress-connection :raw))

  ([wordpress-connection display-type]
   (clojure.walk/keywordize-keys
    (into {}
          (map
           #(extract-post-mapping-item % display-type)
           (get-posts wordpress-connection))))))

(defn get-post
  "Gets a single post from a wordpress-connection.

  Requires an instantiated WordPressConnection record to be passed 
  as well as a valid post-id based on WordPress's ID system. 

  May throw a clojure.lang.ExceptionInfo in the case
  that an inproper identifier was passed.

  Use the get-post-ids function to retrieve all posts for any given instantiated WordPressConnection."

  [wordpress-connection post-id]
  (:body (get-from-wordpress wordpress-connection (str "/posts/" post-id))))

(defn get-post-content
  "Retrieves the content of a simple post from a wordpress-connection as text.
  Note that this retrieves the --rendered-- post content: unfortunately I 
  do not believe that the WordPress JSON API allows one to recieve raw text,
  but I believe for the most part that this is okay.

  Second aarity takes an instantiated WordPressConnection record as well as a 
  valid post-id based on WordPress's ID system and returns the !raw! content of the 
  post if rendered content is desired, the third aarity should be used.
  
  Third aarity takes an instantiated WordPressConnection record, a valid post-id,
  and the content render type one wishes to use: The current types that are returned
  by the WordPress JSON API are :rendered and :raw. :raw is only accessable when
  in the 'edit' context.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper post-id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-post-ids function to retrieve all posts for any given instantiated WordPressConnection."

  ([wordpress-connection post-id]
   (get-post-content wordpress-connection post-id :raw))

  ([wordpress-connection post-id content-type]
   (content-type
    (:content
     (get-post wordpress-connection post-id)))))

(defn get-post-title
  "Retrieves the content of a simple post from a wordpress-connection as text.
  Note that this retrieves the --rendered-- post content: unfortunately I 
  do not believe that the WordPress JSON API allows one to recieve raw text,
  but I believe for the most part that this is okay.

  Second aarity takes an instantiated WordPressConnection record as well as a 
  valid post-id based on WordPress's ID system and returns the !raw! content of the 
  post if rendered content is desired, the third aarity should be used.
  
  Third aarity takes an instantiated WordPressConnection record, a valid post-id,
  and the content render type one wishes to use: The current types that are returned
  by the WordPress JSON API are :rendered and :raw. :raw is only accessable when
  in the 'edit' context.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper post-id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-post-ids function to retrieve all posts for any given instantiated WordPressConnection."

  ([wordpress-connection post-id]
   (get-post-title wordpress-connection post-id :raw))

  ([wordpress-connection post-id content-type]
   (content-type
    (:title
     (get-post wordpress-connection post-id)))))

(defn update-post
  "Uses an authenticated WordPressConnection and post id to update a post generically
  with a map of attributes to be updated.

  Takes an instantiated WordPressConnection, a valid post identifier, and a hashmap
  representing data to be associated onto the post.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper post-id was 
  passed.

  Use the get-post-ids function to retrieve all posts for any given instantiated WordPressConnection."

  [wordpress-connection post-id data]
  (:body (post-to-wordpress wordpress-connection (str "/posts/" post-id) data)))

(defn update-post-content
  "Uses an authenticated WordPressConnection and post id to only update a posts content.

  Takes an instantiated WordPressConnection, a valid post identifier, and a string
  representing raw content to be applied to a post.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper post-id was 
  passed.

  Use the get-post-ids function to retrieve all posts for any given instantiated WordPressConnection."

  [wordpress-connection post-id content]
  (update-post wordpress-connection post-id {:content content}))

(defn create-post
  "Uses an authenticated WordPressConnection to generate a new post.

  Second aarity takes an instantiated WordPressConnection, and a hashmap 
  representing data to instantiate a new post post with.

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

  All aarities return the identifier of the new post."

  ([wordpress-connection attrs]
   (:body (post-to-wordpress wordpress-connection (str "/posts") attrs)))
  ([wordpress-connection title content]
   (create-post wordpress-connection {:title title :content content :status :publish}))
  ([wordpress-connection title content status]
   (create-post wordpress-connection {:title title :content content :status status})))

(defn delete-post
  "Uses an authenticated WordPressConnection and post id to delete a post.

  Takes an instantiated WordPressConnection and a valid post identifier.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper post-id was 
  passed.

  Returns the entire post object in order to make use a little bit less risky!
  Still, use this function with caution!  

  Use the get-post-ids function to retrieve all posts for any given instantiated WordPressConnection."

  [wordpress-connection post-id]
  (:body (delete-from-wordpress wordpress-connection (str "/posts/" post-id))))

(defn get-post-revisions
  "Uses an authenticated WordPressConnection and post id to get all of the post 
  revisions for a specific post.

  Takes an instantiated WordPressConnection and a valid post identifier.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper post-id was
  passed.

  Returns a collection of post revision maps.

  Use the get-post-ids function to retrieve all posts for any given instantiated WordPressConnection."

  [wordpress-connection post-id]
  (:body (get-from-wordpress wordpress-connection (str "/posts/" post-id "/revisions"))))

(defn get-post-revision-ids
  "Gets all the post revision ids that a given post id in a WordPress 
  site currently has.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper post-id was
  passed.

  Takes an instantiated WordPressConnection record and a post id 
  and returns a vector of integers representing post revision IDs."

  [wordpress-connection post-id]
  (into [] (map :id (get-post-revisions wordpress-connection post-id))))

(defn get-post-revision
  "Uses an authenticated WordPressConnection, a post id, and a specific
  revision id to give back information about said specific revision. 

  Takes an instantiated WordPressConnection, a valid post identifier,
  and a valid revision identifier.

  Returns a specific post-revision map.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper post-id 
  OR post-revision-id was passed.

  Use the get-post-revision-ids function to retrieve all post 
  revisions for any given post given back by get-post/get-posts."

  [wordpress-connection post-id post-revision-id]
  (:body (get-from-wordpress wordpress-connection (str "/posts/" post-id "/revisions/" post-revision-id))))

(defn delete-post-revision
  "Uses an authenticated WordPressConnection, a post id, and a 
  specific revision id to delete a post revision.

  Takes an instantiated WordPressConnection, a valid post identifier,
  and a valid revision identifier.

  Returns a map of the now deleted post revision.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper post-id 
  OR post-revision-id was passed.

  Use the get-post-revision-ids function to retrieve all post 
  revisions for any given post given back by get-post/get-posts."

  [wordpress-connection post-id post-revision-id]
  (:body (delete-from-wordpress wordpress-connection (str "/posts/" post-id "/revisions/" post-revision-id) true)))
