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
  (->> wordpress-connection
       get-categories
       (map :id)
       (into [])))

(defn get-category-names
  "Gets all the category names that a WordPress site currently has.

  First aarity takes an instantiated WordPressConnection record and returns
  a vector of strings representing raw category names. This is usually what
  you will want to use.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to render the names.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed."

  ([wordpress-connection]
   (get-category-names wordpress-connection :raw))

  ([wordpress-connection display-type]
   (->> wordpress-connection
        get-categories
        (map :name)
        (into []))))

(defn- extract-category-mapping-item
  "Utility function to generate key value pairs in get-page-mapping"
  [item] [(keyword (str (:id item))) (:name item)])

(defn get-category-mapping
  "Creates a mapping of page identifiers to page names. Useful in contexts
  in which we must explicitly associate the two. It is in general bad to
  flip the key value pairs returned by this function because WordPress allows
  multiple pages with unique identifiers to have the same names. Aka, 
  this map need not be one-to-one.

  *IMPORTANT* This returns a key-value mapping of keywordized integers and 
  strings, not integers and strings!

  First aarity takes an instantiated WordPressConnection record and returns
  map of ids to raw category names. This is usually the aarity that you will
  want to use.

  Second aarity takes an instantiated WordPressConnection record as well as
  a keyword (:rendered, :raw) which will determine how to render the names.

  *Note for the second aarity*
  May throw a clojure.lang.ExceptionInfo in the case that an inproper display
  type was passed. In general, it is best to use the single aarity unless you
  know what your doing!"

  ([wordpress-connection]
   (->> wordpress-connection
        get-categories
        (map extract-category-mapping-item)
        (into {}))))

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
   (->> page-id
        (get-category wordpress-connection)
        :description)))

(defn get-category-name
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
   (->> page-id
        (get-category wordpress-connection)
        :name)))

(defn create-category
  "Uses an authenticated WordPressConnection to generate a new category.

  Second aarity takes an instantiated WordPressConnection, and a hashmap 
  representing data to instantiate a new category with.

  Third aarity represents the usual use case in which the user does not
  care about the slug, parent, and meta-data of a category. it takes an instantiated 
  WordPressConnection, a name, and a description.

  All aarities return the json representation of the new page.

  May throw a clojure.lang.ExceptionInfo in the case that the category name
  already exists."

  ([wordpress-connection attrs]
   (:body (post-to-wordpress wordpress-connection (str "/categories") attrs)))
  ([wordpress-connection name description]
   (create-category wordpress-connection {:name name :description description})))

(defn update-category
  "Uses an authenticated WordPressConnection and page id to update a category generically
  with a map of attributes to be updated.

  Takes an instantiated WordPressConnection, a valid page identifier, and a hashmap
  representing data to be associated onto the category.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper category id was 
  passed.

  Use the get-category-ids function to retrieve all pages for any given instantiated WordPressConnection."

  [wordpress-connection category-id data]
  (:body (post-to-wordpress wordpress-connection (str "/categories/" category-id) data)))

(defn update-category-name
  "Uses an authenticated WordPressConnection and category id to only update a category's name.

  Takes an instantiated WordPressConnection, a valid category identifier, and a string
  representing the title to be applied to a category.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper category-id was 
  passed.

  Use the get-category-ids function to retrieve all categories for any given instantiated WordPressConnection."

  [wordpress-connection category-id name]
  (update-category wordpress-connection category-id {:name name}))

(defn delete-category
  "Uses an authenticated WordPressConnection and category id to delete a category.

  Takes an instantiated WordPressConnection and a valid category identifier.

  May throw a clojure.lang.ExceptionInfo in the case that an inproper category-id was 
  passed.

  Returns the entire category object in order to make use a little bit less risky!
  Still, use this function with caution!  

  Use the get-category-ids function to retrieve all category for any given instantiated WordPressConnection."

  [wordpress-connection category-id]
  (:body (delete-from-wordpress wordpress-connection (str "/categories/" category-id))))

