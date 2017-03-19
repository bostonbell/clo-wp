(ns clo-wp.restful
  (:require [clo-wp.core :refer :all]))

(defwp category-items [:categories])
(defwp category-item [:categories category-id])

(defwp comments [:comments])
(defwp comment [:comments comment-id])
(defwp comment-revisions [:comments comment-id :revisions])
(defwp comment-revision [:comments comment-id :revisions revision-id])

(defwp media-items [:media])
(defwp media-item [:media media-id])
(defwp media-item-revisions [:media media-id :revisions])
(defwp media-item-revision [:media media-id :revisions revision-id])

(defwp pages [:pages])
(defwp page [:pages page-id])
(defwp page-revisions [:pages page-id :revisions])
(defwp page-revision [:pages page-id :revisions revision-id])

(defwp posts [:posts])
(defwp post [:posts page-id])
(defwp post-revisions [:posts page-id :revisions])
(defwp post-revision [:posts page-id :revisions revision-id])

(defwp settings [:settings])

(defwp statuses [:statuses])
(defwp status [:statuses item])

(defwp tags [:tags])
(defwp tag [:tags tag-id])

(defwp taxonomy [:taxonomies])
(defwp taxonomies [:taxonomies taxonomy-id])

(defwp types [:types])
(defwp type [:types type-id])

(defwp users [:users])
(defwp user [:users user-id])
