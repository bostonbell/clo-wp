(ns clo-wp.category
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clo-wp.core :refer :all]))

(defn get-categories
  "Gets all the categories from a wordpress-connection.

  Takes an instantiated WordPressConnection object and returns 
  a list of hashmaps cooresponding to the WordPress APIs 
  category schema."

  [wordpress-connection]
  (:body (get-from-wordpress wordpress-connection "/categories/")))

(defn get-category-ids
  "Gets all the category ids that a WordPress site currently has.

  Takes an instantiated WordPressConnection record and returns
  a vector of integers representing page category entries."

  [wordpress-connection]
  (into [] (map :id (get-categories wordpress-connection))))

(defn get-category-names
  "Gets all the category titles that a WordPress site currently has.

  First aarity takes an instantiated WordPressConnection record and returns
  a vector of strings representing raw category titles. This is usually what
  you will want to use.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to render the title.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed."

  ([wordpress-connection]
   (get-category-names wordpress-connection :raw))

  ([wordpress-connection display-type]
   (into [] (map :name (get-categories wordpress-connection)))))

(defn- extract-category-mapping-item
  "Utility function to generate key value pairs in get-page-mapping"
  [x] [(keyword (str (:id x))) (:name x)])

(defn get-category-mapping
  "Creates a mapping of page identifiers to page titles. Useful in contexts
  in which we must explicitly associate the two. It is in general bad to
  flip the key value pairs returned by this function because WordPress allows
  multiple pages with unique identifiers to have the same titles. Aka, 
  this map need not be one-to-one.

  *IMPORTANT* This returns a key-value mapping of keywordized integers and 
  strings, not integers and strings!

  First aarity takes an instantiated WordPressConnection record and returns
  map of ids to raw category names. This is usually the aarity that you will
  want to use.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to render the titles.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed. In general, it is best to use the single aarity unless you
  know what your doing!"

  ([wordpress-connection]
   (clojure.walk/keywordize-keys
    (into {}
          (map
           extract-category-mapping-item (get-categories wordpress-connection))))))

(defn get-category
  "Gets a single category from a wordpress-connection.

  Requires an instantiated WordPressConnection record to be passed 
  as well as a valid category-id based on WordPress's ID system. 

  May throw a clojure.lang.ExceptionInfo in the case
  that an inproper identifier was passed.

  Use the get-category-ids function to retrieve all categories for any given instantiated WordPressConnection."

  [wordpress-connection category-id]
  (:body (get-from-wordpress wordpress-connection (str "/categories/" category-id))))

(defn get-category-description
  "Retrieves the description of a category from a wordpress-connection as text.
  
  Takes an instantiated WordPressConnection record as well as a valid category id,
  returns a category object.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper category-id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-category-ids function to retrieve all category for any given instantiated WordPressConnection."

  ([wordpress-connection page-id]
     (:description (get-category wordpress-connection page-id))))

(defn get-page-title
  "Retrieves the content of a simple page from a wordpress-connection as text.

  Second aarity takes an instantiated WordPressConnection record as well as a 
  valid page-id based on WordPress's ID system and returns the !raw! content of the 
  page if rendered content is desired, the third aarity should be used.
  
  Third aarity takes an instantiated WordPressConnection record, a valid page-id,
  and the content render type one wishes to use: The current types that are returned
  by the WordPress JSON API are :rendered and :raw. :raw is only accessable when
  in the 'edit' context.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper page-id
  was passed. May return nil if a non-existant content-type was passed.

  Use the get-page-ids function to retrieve all pages for any given instantiated WordPressConnection."

  ([wordpress-connection page-id]
   (get-page-title wordpress-connection page-id :raw))

  ([wordpress-connection page-id content-type]
   (content-type
    (:title
     (get-page wordpress-connection page-id)))))

