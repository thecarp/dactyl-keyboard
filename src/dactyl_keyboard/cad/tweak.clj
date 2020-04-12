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
            [dactyl-keyboard.param.schema :as schema]
            [dactyl-keyboard.param.access :refer [resolve-anchor
                                                  main-body-tweak-data
                                                  central-tweak-data]]
            [dactyl-keyboard.param.proc.anch :as anch]))


;;;;;;;;;;;;;
;; General ;;
;;;;;;;;;;;;;

(defn- classify-node [node]
  (cond
    (spec/valid? ::schema/tweak-plate-map node) ::branch
    (spec/valid? ::schema/tweak-plate-leaf node) ::leaf
    :else (throw (ex-info "Unclassifiable tweak node."
                   {:node node}))))

(defn- segment-range
  [{:keys [anchoring sweep]}]
  {:pre [sweep (:segment anchoring)]}
  (range (:segment anchoring) (inc sweep)))

(defn- single-step-node
  "Simplify a leaf node to cover one part of a sweep."
  [node segment]
  (-> node (assoc :sweep nil) (assoc-in [:anchoring :segment] segment)))

(defn- splay
  "Represent one leaf node as a list of one or more non-sweeping nodes."
  [{:keys [sweep] :as node}]
  (if sweep
    (map (partial single-step-node node) (segment-range node))
    [node]))


;;;;;;;;
;; 3D ;;
;;;;;;;;

(defn- pick-3d-shape
  "Pick the model for a tweak. Return a tuple of that model and an indicator
  for whether the model is already in place. Positioning depends both on the
  type of anchor and secondary parameters about the target detail upon it.
  By default, use the most specific dimensions available for the post,
  defaulting to a post for key-cluster webbing."
  [getopt {:keys [anchoring] :as node}]
  {:pre [(spec/valid? ::schema/tweak-plate-leaf node)]}
  (let [{:keys [anchor side segment offset]} anchoring
        {::anch/keys [type primary]} (resolve-anchor getopt anchor)]
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
             (maybe/translate (place/port-hole-offset getopt anchoring)
               (misc/nodule))
             (auxf/port-hole getopt anchor)))]
      :port-holder
        [true
         (place/port-place getopt primary
           (if (or side segment offset)
             (maybe/translate (place/port-holder-offset getopt
                                (assoc anchoring ::anch/primary primary))
               (auxf/port-tweak-post getopt primary))
             (auxf/port-holder getopt primary)))]
      [false (key/web-post getopt)])))

(defn- leaf-blade-3d
  "One model at one vertical segment of one feature."
  [getopt {:keys [anchoring] :as node}]
  {:pre [(spec/valid? ::schema/tweak-plate-leaf node)]}
  (let [[placed item] (pick-3d-shape getopt node)]
    (if placed
      item
      (place/reckon-with-anchor getopt (assoc anchoring :subject item)))))

(defn- model-leaf-3d
  "(The hull of) one or more models of one type on one side, in place, in 3D."
  [getopt node]
  (apply maybe/hull (map (partial leaf-blade-3d getopt) (splay node))))

(declare plating)

(defn- model-branch-3d
  [getopt {:keys [at-ground above-ground hull-around chunk-size highlight]
           :or {above-ground true}}]
  (let [prefix (if highlight model/-# identity)
        shapes (reduce (partial plating getopt) [] hull-around)
        hull (if at-ground misc/bottom-hull model/hull)]
    (when above-ground
      (prefix
        (apply (if chunk-size model/union hull)
          (if chunk-size
            (map (partial apply hull) (partition chunk-size 1 shapes))
            shapes))))))

(defn- plating
  "A reducer."
  [getopt coll node]
  (conj coll
    (case (classify-node node)
      ::branch (model-branch-3d getopt node)
      ::leaf (model-leaf-3d getopt node))))

(defn- union
  [getopt data-fn]
  "User-requested additional shapes from some data."
  [getopt]
  (apply maybe/union
    (reduce (partial plating getopt) [] (data-fn getopt))))

(defn all-main-body [getopt] (union getopt main-body-tweak-data))
(defn all-central-housing [getopt] (union getopt central-tweak-data))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 2D: Projections to the Floor ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- floor-pairs
  "Produce coordinate pairs for a polygon. A reducer.
  Pick just one leaf in a branch node, and just one post in a leaf, on the
  assumption that they’re not all ringing the case."
  [getopt [post-picker segment-picker bottom] coll node]
  {:pre [(spec/valid? ::schema/tweak-plate-leaf node)]
   :post [(spec/valid? ::tarmi-core/point-coll-2d %)]}
  (conj coll
    (as-> (case (classify-node node)
            ::branch (post-picker (:hull-around node))
            ::leaf node)
          point
      (splay point)
      (segment-picker point)  ; A single- or no-segment leaf.
      (:anchoring point)
      (assoc point :bottom bottom)  ; Metadata for placement.
      (place/reckon-with-anchor getopt point)
      (take 2 point))))  ; [x y] coordinates.

(defn- plate-polygon
  "A single version of the footprint of a tweak.
  Tweaks so small that they amount to fewer than three vertices are ignored
  because they wouldn’t have any area."
  [getopt pickers node-list]
  (let [points (reduce (partial floor-pairs getopt pickers) [] node-list)]
    (when (> (count points) 2) (model/polygon points))))

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
  (apply maybe/union
    (map #(plate-shadows getopt (:hull-around %))
         (filter :at-ground (main-body-tweak-data getopt)))))

