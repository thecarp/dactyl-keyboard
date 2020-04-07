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
            [dactyl-keyboard.cad.auxf :as auxf]
            [dactyl-keyboard.cad.body :as body]
            [dactyl-keyboard.cad.central :as central]
            [dactyl-keyboard.cad.mcu :as mcu]
            [dactyl-keyboard.cad.misc :as misc]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.key :as key]
            [dactyl-keyboard.param.access :refer [resolve-anchor
                                                  main-body-tweak-data
                                                  central-tweak-data]]))


;;;;;;;;;;
;; Main ;;
;;;;;;;;;;


(defn- select-post
  "Pick the model for a tweak. Return a tuple of that model and an indicator
  for whether the model is already in place. Positioning depends both on the
  type of anchor and secondary parameters about the target detail upon it.
  By default, use the most specific dimensions available for the post,
  defaulting to a post for key-cluster webbing."
  [getopt {:keys [anchor side segment offset] :as opts}]
  (let [{:keys [type parent]} (resolve-anchor getopt anchor)]
    (case type
      :central-housing
        [true
         (central/tweak-post getopt anchor)]
      :rear-housing
        [false  ; TODO: Refactor based on central-housing-like logic.
         (body/rhousing-post getopt)]
      :mcu-grip
        [false
         (apply model/cube (getopt :mcu :support :grip :size))]
      ;; If a side of the MCU plate is specifed, put a nodule there,
      ;; else use the entire base of the plate.
      :mcu-lock-plate
        [false
         (if side
           misc/nodule
           (mcu/lock-plate-base getopt false))]
      :port-hole
        [true
         (place/port-place getopt anchor
           (if (or side segment offset)
             (maybe/translate (place/port-hole-offset getopt opts)
               (misc/nodule))
             (auxf/port-hole getopt anchor)))]
      :port-holder
        [true
         (place/port-place getopt parent
           (if (or side segment offset)
             (maybe/translate (place/port-holder-offset getopt
                                (assoc opts :parent parent))
               (auxf/port-tweak-post getopt parent))
             (auxf/port-holder getopt anchor)))]
      [false (key/web-post getopt)])))

(defn- single-post
  "One model at one vertical segment of one feature."
  [getopt {:keys [anchor] :as opts}]
  (let [[placed post] (select-post getopt opts)]
    (if placed
      post
      (place/reckon-from-anchor getopt anchor (assoc opts :subject post)))))

(defn- posts
  "(The hull of) one or more models of one type on one side."
  [getopt anchor side first-segment last-segment]
  (if (= first-segment last-segment)
    (single-post getopt {:anchor anchor, :side side, :segment first-segment})
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

