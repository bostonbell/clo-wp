(ns clo-wp.page
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clo-wp.core :refer :all]))

(defn get-pages
  "Takes an WordPressConnection and 
  gets all the page resources as JSON."

  [wordpress-connection]
  (api-getter wordpress-connection [:pages]))

(defn get-page-ids
  "Takes a WordPressConnection and gets all the page ids."

  [wordpress-connection]
  (api-getter wordpress-connection [:pages] [:id]))

(defn get-page-titles
  "Takes a wordpress connection and an optional display type (which may
  be ':rendered' or ':raw') and returns properly formatted title information."

  ([wordpress-connection]
   (get-page-titles wordpress-connection :raw))

  ([wordpress-connection display-type]
   (api-getter wordpress-connection [:pages] [:title display-type])))

(defn get-page-mapping
  "Takes a WordPressConnection and creates a mapping of page identifiers to page titles."

  ([wordpress-connection]
   (get-page-mapping wordpress-connection :raw))

  ([wordpress-connection display-type]
   (api-mapper wordpress-connection [:pages] [:id] [:title display-type])))


(defn get-page
  "Takes a WordPressConnection and a page-id and gets a single page's
  JSON."

  [wordpress-connection page-id]
  (api-getter wordpress-connection [:pages page-id]))

(defn get-page-content
  "Takes a WordPressConnection and a page retrieves that content of said
  page. Also allows optional parameter of content-type in the case that
  one wants to have :rendered or :raw content."

  ([wordpress-connection page-id]
   (get-page-content wordpress-connection page-id :raw))

  ([wordpress-connection page-id content-type]
   (api-getter wordpress-connection [:pages page-id] [:content content-type])))

(defn get-page-title
  "Takes a WordPressConnection and a page and retrieves the title of said 
  page as text."

  ([wordpress-connection page-id]
   (get-page-title wordpress-connection page-id :raw))

  ([wordpress-connection page-id content-type]
   (api-getter wordpress-connection [:pages page-id] [:title content-type])))

(defn update-page
  "Takes an authenticated WordPressConnection and page id to update a page 
  with a map of attributes to be updated. Read the WordPress API documentation
  to see information on the schema."

  [wordpress-connection page-id data]
  (api-updater wordpress-connection [:pages page-id] data))

(defn update-page-content
  "Takes an authenticated WordPressConnection and a page id to update a pages content."

  [wordpress-connection page-id content]
  (api-updater wordpress-connection [:pages page-id] :content content))

(defn update-page-title
  "Takes an authenticated WordPressConnection and a page id to update a pages title."

  [wordpress-connection page-id title]
  (api-updater wordpress-connection [:pages page-id] :title title))

(defn create-page
  "Takes an authenticated WordPressConnection, and either an attribute map,
  , a title and a content, or a title and a content and a status 
  to generate a new page, returning the new page as JSON. See the WordPress
  API documentation for the page schema."

  ([wordpress-connection attrs]
   (api-updater wordpress-connection [:pages] attrs))

  ([wordpress-connection title content]
   (create-page
    wordpress-connection
    {:title title :content content :status :publish}))

  ([wordpress-connection title content status]
   (create-page
    wordpress-connection
    {:title title :content content :status status})))

(defn delete-page
  "Takes an authenticated WordPressConnection and a page id and deletes a page.
  returns the JSON object of the now deleted page."

  [wordpress-connection page-id]
  (api-deleter wordpress-connection [:pages page-id]))

(defn get-page-revisions
  "Takes an authenticated WordPressConnection and page id and gets all of the page 
  revisions for a specific page as JSON."

  [wordpress-connection page-id]
  (api-getter wordpress-connection [:pages page-id :revisions]))

(defn get-page-revision-ids
  "Takes an authenticated WordPressConnection and a page id to gets all the page revision ids 
  that a given page id in a WordPress site currently has."

  [wordpress-connection page-id]
  (api-getter wordpress-connection [:pages page-id :revisions] [:id]))

(defn get-page-revision
  "Takes an authenticated WordPressConnection, a page id, and a
  revision id and returns information about that specific revision."

  [wordpress-connection page-id revision-id]
  (api-getter wordpress-connection [:pages page-id :revisions revision-id]))

(defn delete-page-revision
  "Takes an authenticated WordPressConnection, a page id, and a 
  revision id, deletes the page, and returns the now deleted page."

  [wordpress-connection page-id revision-id]
  (api-deleter wordpress-connection [:pages page-id :revisions revision-id]))
