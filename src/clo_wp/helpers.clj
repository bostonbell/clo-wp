(ns clo-wp.helpers)

(defmulti primary-content
  (fn [item]
    (:type item (:taxonomy item))))

(defn primary-content-helper [x]
  (if (= (count (keys x)) 1)
    (name (first (keys x)))
    (clojure.pprint/pprint x)))

(defmethod primary-content :default [item]
  (cond
    (get-in item [:content :raw]) (get-in item [:content :raw])
    (:name item) (:name item)
    (:description item) (:description item)
    :else (primary-content-helper item)))

(defmulti primary-name
  (fn [item]
    (:name item (:taxonomy item))))

(defmethod primary-name :default [item]
  (cond
    (get-in item [:title :raw]) (get-in item [:title :raw])
    (:name item) (:name item)
    :else nil))

(defn primary-id [item]
  (cond
    (:id item) (:id item)
    :else nil))

(defmulti map-collection
  (fn [collection]
    (:type (first collection) (:taxonomy (first collection)))))

(defmethod map-collection  :default [collection]
  (into {} (map (fn [item] [(:id item) (get-in item [:title :raw])]) collection)))

