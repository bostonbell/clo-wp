(ns clo-wp.media
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clo-wp.core :refer :all]))

(defn get-media-items
  "Gets all the media-items from a wordpress-connection.

  Takes an instantiated WordPressConnection object and returns 
  a list of hashmaps cooresponding to the WordPress APIs media schema."

  [wordpress-connection]
  (:body (get-from-wordpress wordpress-connection "/media/")))

(defn get-media-ids
  "Gets all the media-items ids that a WordPress site currently has.

  Takes an instantiated WordPressConnection record and returns
  a vector of integers representing media IDs."

  [wordpress-connection]
  (->> wordpress-connection
       get-media-items
       (map :id)
       (into [])))

(defn get-media-titles
  "Gets all the media-items titles that a WordPress site currently has.

  First aarity takes an instantiated WordPressConnection record and returns
  a vector of strings representing raw media titles. This is usually what
  you will want to use.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to render the title.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed."

  ([wordpress-connection]
   (get-media-titles wordpress-connection :raw))

  ([wordpress-connection display-type]
   (->> wordpress-connection
        get-media-items
        (map :title)
        (map display-type)
        (into []))))

(defn- extract-media-mapping-item
  "Utility function to generate key value pairs in get-media-mapping"
  [x display-type] [(keyword (str (:id x))) (display-type (:title x))])

(defn get-media-mapping
  "Creates a mapping of media identifiers to media titles. Useful in contexts
  in which we must explicitly associate the two. It is in general bad to
  flip the key value pairs returned by this function because WordPress allows
  multiple media-items with unique identifiers to have the same titles. Aka, 
  this map need not be one-to-one.

  *IMPORTANT* This returns a key-value mapping of keywordized integers and 
  strings, not integers and strings!!

  First aarity takes an instantiated WordPressConnection record and returns
  map of ids to raw media titles.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to output the titles, 
  this is neccessary because WordPress renders titles via a macro system.
  In turn, a map of ids to generic media titles will be returned.

  In general, the first aarity is what you will want to use unless there
  is some reason not to.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed. In general, it is best to use the single aarity unless you
  know what your doing!"

  ([wordpress-connection]
   (get-media-mapping wordpress-connection :raw))

  ([wordpress-connection display-type]
   (clojure.walk/keywordize-keys
    (into {}
          (map
           #(extract-media-mapping-item % display-type)
           (get-media-items wordpress-connection))))))

(defn get-media
  "Gets a single media from a wordpress-connection.

  Requires an instantiated WordPressConnection record to be passed 
  as well as a valid media-id based on WordPress's ID system. 

  May throw a clojure.lang.ExceptionInfo in the case
  that an inproper identifier was passed.

  Use the get-media-ids function to retrieve all media-items for any given instantiated WordPressConnection."

  [wordpress-connection media-id]
  (:body (get-from-wordpress wordpress-connection (str "/media/" media-id))))

(defn get-media-link
  "Retrieves the content of a simple media from a wordpress-connection as text.

  Second aarity takes an instantiated WordPressConnection record as well as a 
  valid media-id based on WordPress's ID system and returns the !raw! content of the 
  media if rendered content is desired, the third aarity should be used.
  
  Third aarity takes an instantiated WordPressConnection record, a valid media-id,
  and the content render type one wishes to use: The current types that are returned
  by the WordPress JSON API are :rendered and :raw. :raw is only accessable when
  in the 'edit' context.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper media-id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-media-ids function to retrieve all media-items for any given instantiated WordPressConnection."

  ([wordpress-connection media-id]
    (:source_url
     (get-media wordpress-connection media-id))))

(defn get-media-title
  "Retrieves the content of a simple media from a wordpress-connection as text.

  Second aarity takes an instantiated WordPressConnection record as well as a 
  valid media-id based on WordPress's ID system and returns the !raw! content of the 
  media if rendered content is desired, the third aarity should be used. In turn
  returns a said media-items title.
  
  Third aarity takes an instantiated WordPressConnection record, a valid media-id,
  and the content render type one wishes to use: The current types that are returned
  by the WordPress JSON API are :rendered and :raw. :raw is only accessable when
  in the 'edit' context. In turn returns said media-items title.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper media-id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-media-ids function to retrieve all media-items for any given instantiated WordPressConnection."

  ([wordpress-connection media-id]
   (get-media-title wordpress-connection media-id :raw))

  ([wordpress-connection media-id content-type]
   (content-type
    (:title
     (get-media wordpress-connection media-id)))))

(defn get-media-description
  "Retrieves the content of a simple media from a wordpress-connection as text.

  Second aarity takes an instantiated WordPressConnection record as well as a 
  valid media-id based on WordPress's ID system and returns the !raw! content of the 
  media if rendered content is desired, the third aarity should be used. In turn
  returns a said media-items title.
  
  Third aarity takes an instantiated WordPressConnection record, a valid media-id,
  and the content render type one wishes to use: The current types that are returned
  by the WordPress JSON API are :rendered and :raw. :raw is only accessable when
  in the 'edit' context. In turn returns said media-items title.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper media-id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-media-ids function to retrieve all media-items for any given instantiated WordPressConnection."

  ([wordpress-connection media-id]
   (get-media-description wordpress-connection media-id :raw))

  ([wordpress-connection media-id content-type]
   (content-type
    (:description
     (get-media wordpress-connection media-id)))))

(defn update-media
  "Uses an authenticated WordPressConnection and media id to update a media generically
  with a map of attributes to be updated.

  Takes an instantiated WordPressConnection, a valid media identifier, and a hashmap
  representing data to be associated onto the media.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper media-id was 
  passed.

  Use the get-media-ids function to retrieve all media-items for any given instantiated WordPressConnection."

  [wordpress-connection media-id data]
  (:body (post-to-wordpress wordpress-connection (str "/media/" media-id) data)))

(defn update-media-title
  "Uses an authenticated WordPressConnection and media id to only update a media-items content.

  Takes an instantiated WordPressConnection, a valid media identifier, and a string
  representing raw content to be applied to a media.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper media-id was 
  passed.

  Use the get-media-ids function to retrieve all media-items for any given instantiated WordPressConnection."

  [wordpress-connection media-id new-title]
  (update-media wordpress-connection media-id {:title new-title}))

(defn update-media-description
  "Uses an authenticated WordPressConnection and media id to only update a media-items content.

  Takes an instantiated WordPressConnection, a valid media identifier, and a string
  representing raw content to be applied to a media.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper media-id was 
  passed.

  Use the get-media-ids function to retrieve all media-items for any given instantiated WordPressConnection."

  [wordpress-connection media-id new-description]
  (update-media wordpress-connection media-id {:description new-description}))


(defn create-media
  "Uses an authenticated WordPressConnection to generate a new media.

  Second aarity takes an instantiated WordPressConnection, and a hashmap 
  representing data to instantiate a new media media with.

  Third aarity represents the usual use case in which the user does not
  care about the status of a media (and as a result will default to publish)
  it takes an instantiated WordPressConnection, a title, and a content.

  Fourth aarity represents the usual use case that takes an instantiated 
  WordPressConnection, a title, a content, and the status which can be either 
  :publish, :future, :draft, :pending, or :private.
  
  *Note on fourth aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper status was 
  passed. The other two aarities are quite safe, but make sure you are using the
  only the status types which your WordPress version supports if this aarity is used!

  All aarities return the the json representation of the new media.

  You probably don't want to use this: Use a different library for creation of
  media on a WordPress site. Only use this to modify current media. Still, 
  use at your own risk!"

  ([wordpress-connection attrs]
   (:body (post-to-wordpress wordpress-connection (str "/media/") attrs)))
  ([wordpress-connection title description file]
   (create-media wordpress-connection {:title title :description description :status :publish :file file}))
  ([wordpress-connection title description file status]
   (create-media wordpress-connection {:title title :description description :status status :file file})))

(defn delete-media
  "Uses an authenticated WordPressConnection and media id to delete a media.

  Takes an instantiated WordPressConnection and a valid media identifier.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper media-id was 
  passed.

  Returns the entire media object in order to make use a little bit less risky!
  Still, use this function with caution!  

  Use the get-media-ids function to retrieve all media-items for any given instantiated WordPressConnection."

  [wordpress-connection media-id]
  (:body (delete-from-wordpress wordpress-connection (str "/media/" media-id))))
