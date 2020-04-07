;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Tweak Plating                                                       ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module describes models for a general system of tweaks. Tweaks alter
;;; the shape of the keyboard case by connecting named features.

(ns dactyl-keyboard.cad.tweak
  (:require [clojure.spec.alpha :as spec]
            [scad-clj.model :as model]
            [scad-tarmi.core :as tarmi-core]
            [scad-tarmi.maybe :as maybe]
            [dactyl-keyboard.cad.body :as body]
            [dactyl-keyboard.cad.central :as central]
            [dactyl-keyboard.cad.mcu :as mcu]
            [dactyl-keyboard.cad.misc :as misc]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.key :as key]
            [dactyl-keyboard.param.access :as access :refer [main-body-tweak-data
                                                             central-tweak-data]]))


;;;;;;;;;;
;; Main ;;
;;;;;;;;;;


(defn- posts
  "(The hull of) one or more corner posts for a case tweak.
  This function both picks the shape of the post and positions it.
  For a tweak anchored to the central housing, defer to the central module
  for both shape and position, because they are closely related in that case.
  Otherwise use the most specific dimensions available for the post, defaulting
  to a web post."
  [getopt anchor side first-segment last-segment]
  (if (= first-segment last-segment)
    (let [type (:type (access/resolve-anchor getopt anchor))
          post (case type
                 :central-housing (central/tweak-post getopt anchor)
                 :rear-housing (body/rhousing-post getopt)
                 :mcu-grip (apply model/cube (getopt :mcu :support :grip :size))
                 ;; If a side of the MCU plate is specifed, put a nodule there,
                 ;; else use the entire base of the plate.
                 :mcu-lock-plate (if side
                                   misc/nodule
                                   (mcu/lock-plate-base getopt false))
                 (key/web-post getopt))]
      (if (= type :central-housing)
        ;; High-precision anchor; reckon-from-anchor is inadequate.
        post
        ;; Low-precision anchor.
        (place/reckon-from-anchor getopt anchor
          {:subject post, :side side, :segment first-segment})))
    (apply model/hull (map #(posts getopt anchor side %1 %1)
                           (range first-segment (inc last-segment))))))

(declare plating)

(defn- tweak-map
  "Treat a map-type node in the configuration."
  [getopt node]
  (let [parts (get node :chunk-size)
        at-ground (get node :at-ground false)
        prefix (if (get node :highlight) model/-# identity)
        shapes (reduce (partial plating getopt) [] (:hull-around node))
        hull (if at-ground misc/bottom-hull model/hull)]
    (when (get node :above-ground true)
      (prefix
        (apply (if parts model/union hull)
          (if parts
            (map (partial apply hull) (partition parts 1 shapes))
            shapes))))))

(defn- plating
  "A reducer."
  [getopt coll node]
  (conj coll
    (if (map? node)
      (tweak-map getopt node)
      (apply (partial posts getopt) node))))

(defn- union
  [getopt data-fn]
  "User-requested additional shapes from some data."
  [getopt]
  (apply maybe/union
    (reduce (partial plating getopt) [] (data-fn getopt))))

(defn all-main-body [getopt] (union getopt main-body-tweak-data))
(defn all-central-housing [getopt] (union getopt central-tweak-data))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Projections to the Floor ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- floor-vertex
  "A corner vertex on a tweak wall, extending from a key mount."
  [getopt segment-picker bottom [alias side first-segment last-segment]]
  {:post [(spec/valid? ::tarmi-core/point-2d %)]}
  (let [segment (segment-picker (range first-segment (inc last-segment)))]
    (take 2 (place/reckon-from-anchor getopt alias
              {:side side, :segment segment, :bottom bottom}))))

(defn- dig-to-seq [node]
  (if (map? node) (dig-to-seq (:hull-around node)) node))

(defn- floor-pairs
  "Produce coordinate pairs for a polygon. A reducer."
  [getopt [post-picker segment-picker bottom] coll node]
  {:post [(spec/valid? ::tarmi-core/point-coll-2d %)]}
  (let [vertex-fn (partial floor-vertex getopt segment-picker bottom)]
    (conj coll
      (if (map? node)
        ;; Pick just one post in the subordinate node, on the assumption that
        ;; they’re not all ringing the case.
        (vertex-fn (post-picker (dig-to-seq node)))
        ;; Node is one post at the top level. Always use that.
        (vertex-fn node)))))

(defn- plate-polygon
  "A single version of the footprint of a tweak.
  Tweaks so small that they amount to fewer than three vertices are ignored
  because they wouldn’t have any area."
  [getopt pickers node-list]
  (let [points (reduce (partial floor-pairs getopt pickers) [] node-list)]
    (when (> (count points) 2)
      (model/polygon points))))

(defn- plate-shadows
  "Versions of a tweak footprint.
  This is a semi-brute-force-approach to the problem that we cannot easily
  identify which vertices shape the outside of the case at z = 0."
  [getopt node-list]
  (apply maybe/union
    (distinct
      (for
        [post [first last], segment [first last], bottom [false true]]
        (plate-polygon getopt [post segment bottom] node-list)))))

(defn all-shadows
  "The footprint of all user-requested additional shapes that go to the floor."
  [getopt]
  (apply maybe/union (map #(plate-shadows getopt (:hull-around %))
                          (filter :at-ground (main-body-tweak-data getopt)))))

