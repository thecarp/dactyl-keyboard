;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Keyboard Case Model – Central Housing                               ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; The central housing is one of the predefined bodies of the application.
;;; It is distinguished by its shape, uniquely defined in terms of a hollow
;;; polyhedral block extending between two copies of the main body, one of
;;; these copies being reflected.

(ns dactyl-keyboard.cad.body.central
  (:require [clojure.spec.alpha :as spec]
            [thi.ng.math.core :as math]
            [scad-clj.model :as model]
            [scad-tarmi.core :as tarmi :refer [abs]]
            [scad-tarmi.maybe :as maybe]
            [scad-tarmi.util :refer [loft]]
            [scad-klupe.iso :as iso]
            [dactyl-keyboard.cad.misc :refer [merge-bolt wafer]]
            [dactyl-keyboard.cad.poly :as poly]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.misc :refer [soft-merge]]
            [dactyl-keyboard.param.access :refer [compensator]]))


;;;;;;;;;;;;;;;
;; Internals ;;
;;;;;;;;;;;;;;;

;; Geometry.
(defn- get-z-offset
  "Get just a z-axis base offset."
  [interface-item]
  (get-in interface-item [:base :offset 2] 0))
(defn- outline-back-to-3d
  [base-3d outline]
  (map (fn [[x _ _] [y1 z1]] [x y1 z1]) base-3d outline))
(defn- horizontal-shifter [x-fn] (fn [[x y z]] [(x-fn x) y z]))
(defn- mirror-shift [points] (map (horizontal-shifter -) points))
(defn- shift-points
  "Manipulate a series of 3D points.
  If there are at least three points or a non-zero inset is passed, the points
  must form a perimeter (a valid polygon) in the yz plane (2D). In that case
  the polygon will be contracted by a positive inset. Each point will also be
  shifted on the x axis (back in 3D) by a non-zero delta-x. Return a vector for
  indexability."
  ([base]  ; Presumably called for vector conversion.
   (shift-points base 0))
  ([base inset]
   (shift-points base inset 0))
  ([base inset delta-x]
   (shift-points base inset + delta-x))
  ([base inset x-operator delta-x]
   {:pre [(spec/valid? ::tarmi/point-coll-3d base)
          (number? inset)
          (number? delta-x)]}
   (mapv (horizontal-shifter #(x-operator % delta-x))
     (if (zero? inset)
       ;; Support sequences that are not valid polygons.
       base
       ;; Else apply the inset before the horizontal shifter.
       (as-> base subject
         (mapv rest subject)
         (poly/from-outline subject inset)
         (outline-back-to-3d base subject))))))

;; Predicates for sorting fasteners by the object they penetrate.
(defn- adapter-side [{:keys [axial-offset]}] (neg? axial-offset))
(defn- housing-side [{:keys [axial-offset]}] (pos? axial-offset))
(defn- any-side [_] true)

;; Predicates for filtering items in the interface for drawing different
;; bodies.
(defn- above-ground?  ; If true, to be included in body.
  [{:keys [above-ground] :as point}]
  (if (nil? above-ground) (not (neg? (get-z-offset point))) above-ground))
(defn- at-ground?  ; If true, to be included in bottom plate.
  [{:keys [at-ground] :as point}]
  (if (nil? at-ground) (not (pos? (get-z-offset point))) at-ground))

(defn- fastener-feature
  "The union of all features produced by a given model function at the sites of
  all adapter fasteners matching a predicate function, on the right-hand side."
  [getopt pred model-fn]
  (let [positions (getopt :central-housing :adapter :fasteners :positions)
        subject-fn #(place/chousing-fastener getopt % (model-fn getopt %))]
    (apply maybe/union (map subject-fn (filter pred positions)))))

(defn- single-right-side-fastener
  "A fastener for attaching the central housing to the rest of the case.
  Because threaded fasteners are chiral, the model is generated elsewhere
  and invoked here as a module, so scad-app can mirror it."
  [_ _]
  (model/call-module "housing_adapter_fastener"))

(defn- single-left-side-fastener
  "The same model, pre-emptively mirrored to get the right threading."
  [_ _]
  (model/mirror [-1 0 0] (model/call-module "housing_adapter_fastener")))

(defn- single-receiver
  "An extension through the central-housing interface array to receive a single
  fastener. This design is a bit rough; more parameters would be needed to
  account for the possibility of wall surfaces angled on the x or y axes.
  Key-cluster wall thickness is not taken into account."
  [getopt {:keys [axial-offset]}]
  (let [rprop (partial getopt :central-housing :adapter :receivers)
        fprop (partial getopt :central-housing :adapter :fasteners)
        diameter (fprop :bolt-properties :m-diameter)
        z-wall (getopt :central-housing :shape :thickness)
        width (+ diameter (* 2 (rprop :thickness :rim)))
        z-hole (- (iso/bolt-length (fprop :bolt-properties)) z-wall)
        z-bridge (min z-hole (rprop :thickness :bridge))
        x-gabel (abs axial-offset)
        x-inner (+ x-gabel (rprop :width :inner))
        x-taper (+ x-inner (rprop :width :taper))
        signed (fn [x] (* (- x) (math/sign axial-offset)))]
    (loft
      ;; The furthermost taper sinks into a straight wall.
      [(model/translate [(signed x-taper) 0 (/ z-wall -2)]
         (model/cube wafer (dec diameter) wafer))
       ;; The thicker base of the anchor.
       (model/translate [(signed x-inner) 0 (- (+ z-wall (/ z-bridge 2)))]
         (model/union
           (model/cube wafer (inc diameter) z-bridge)
           (model/cube wafer diameter (inc z-bridge))))
       (model/translate [(signed x-gabel) 0 (- (+ z-wall (/ z-bridge 2)))]
         (model/union
           (model/cube wafer (inc diameter) z-bridge)
           (model/translate [0 0 -1]
             (model/cube wafer diameter z-bridge))))
       ;; Finally the bridge extending past the base.
       (model/translate [0 0 (- (+ z-wall (/ z-hole 2)))]
         (model/hull  ; Soft edges, more material in the middle.
           (model/cylinder (/ (inc diameter) 2) z-hole)
           (model/translate [0 0 (/ z-hole 8)]
             (model/cylinder (/ width 2) (/ z-hole 3)))))])))

(defn- get-offsets
  "Get raw offsets for each point on the interface."
  [interface]
  [(map #(get-in % [:base :offset] [0 0 0]) interface)
   (map #(get-in % [:adapter :offset] [0 0 0]) interface)])

(defn- get-widths
  "Get half the width of the central housing and the full width of its
  adapter."
  [getopt]
  [(/ (getopt :central-housing :shape :width) 2)
   (getopt :central-housing :adapter :width)])

(defn- resolve-offsets
  "Find the 3D coordinates of points on the outer shell of passed interface."
  [getopt interface]
  (let [[half-width adapter-width] (get-widths getopt)
        [base adapter] (get-offsets interface)
        gabel (shift-points base 0 half-width)]
    [gabel
     (mapv (partial mapv + [adapter-width 0 0]) gabel (shift-points adapter))]))

(defn- filter-indexed
  "Filter an interface list while annotating it with its source indices."
  [interface pred]
  (keep-indexed (fn [index item] (when (pred item) [index item])) interface))

(defn- index-map
  "Filter an interface list. Return both a map of indices in the original list
  to indices in the filtered version, and the filtered version itself.
  This is intended to allow tracing items in the filtered version back to
  the original, as is required to fully annotate the interface."
  [interface pred]
  (let [indexed (filter-indexed interface pred)]
    [(into {} (map-indexed (fn [local [global _]] [global local]) indexed))
     (map second indexed)]))

(defn- annotate-interface
  "Annotate relevant points in the central housing interface with additional
  information relevant only to a shorter, hnce differently indexed, list."
  [interface index-map basepath & subpath-data-pairs]
  (map-indexed
    (fn [global-index item]
      (if-let [local-index (get index-map global-index)]
        ;; Data should exist for the interface item.
        (reduce
          (fn [coll [subpath data]]
            ;; Add one datum to the item without overriding neighbours.
            (soft-merge coll
              (assoc-in {} (concat basepath subpath) (get data local-index))))
          item
          (partition 2 subpath-data-pairs))
        ;; Else pass the item through unchanged.
        item))
    interface))

(defn- locate-above-ground-points
  "Derive 3D coordinates on the body of the central housing."
  [getopt interface]
  (let [[half-width _] (get-widths getopt)
        thickness (getopt :central-housing :shape :thickness)
        [cross-indexed items] (index-map interface :above-ground)
        [base-offsets _] (get-offsets items)
        [right-gabel-outer adapter-outer] (resolve-offsets getopt items)
        shift-left (partial shift-points (mirror-shift base-offsets))]
    (annotate-interface interface cross-indexed [:points :above-ground]
      [:gabel :right :outer] right-gabel-outer
      [:gabel :right :inner] (shift-points base-offsets thickness half-width)
      [:gabel :left :outer] (shift-left 0 - half-width)
      [:gabel :left :inner] (shift-left thickness - half-width)
      [:adapter :outer] adapter-outer
      [:adapter :inner] (shift-points adapter-outer thickness))))

(defn- locate-lip
  "Derive 3D coordinates on the adapter lip of the central housing."
  [getopt interface]
  (let [[cross-indexed items] (index-map interface :above-ground)
        thickness (getopt :central-housing :adapter :lip :thickness)
        width (partial getopt :central-housing :adapter :lip :width)
        base (map #(get-in % [:points :above-ground :gabel :right :inner]) items)
        shift-in (partial shift-points base)]
    (annotate-interface interface cross-indexed [:points :above-ground]
      [:lip :outside :outer] (shift-in 0 (width :outer))
      [:lip :outside :inner] (shift-in thickness (width :outer))
      [:lip :inside :outer] (shift-in 0 - (+ (width :taper) (width :inner)))
      [:lip :inside :inner] (shift-in thickness - (width :inner)))))

(defn- locate-at-ground-points
  "Derive some useful 2D coordinates for drawing bottom plates."
  [getopt interface]
  (let [[cross-indexed items] (index-map interface :at-ground)
        [gabel adapter] (map (partial mapv #(vec (take 2 %)))
                             (resolve-offsets getopt items))]
    (annotate-interface interface cross-indexed [:points :at-ground]
      [:gabel] gabel
      [:adapter] adapter)))

(defn- locate-non-above-ground-points
  "Derive 3D coordinates for points on the interface that do not directly
  shape the central housing or its adapter. This includes points that only
  shape the floor, and “ethereal” points that shape no part of the central
  housing or its floor, except as anchors."
  [getopt interface]
  (let [[cross-indexed items] (index-map interface (complement :above-ground))
        [gabel adapter] (resolve-offsets getopt items)]
    (annotate-interface interface cross-indexed [:points :ethereal]
      [:gabel] gabel
      [:adapter] adapter)))

(defn- prepare-criteria
  "Derive quick-access flags for component inclusion."
  [getopt]
  (let [main (and (getopt :main-body :reflect)
                  (getopt :central-housing :include))
        adapter (and main
                     (getopt :central-housing :adapter :include))]
    {:include-main main
     :include-adapter adapter
     :include-lip (and adapter
                       (getopt :central-housing :adapter :lip :include))}))

(defn categorize-explicitly
  "Annotate an interface item with explicit category tags.
  Some of these may replace sparser tagging from the user configuration."
  [item]
  (-> item
    (assoc :above-ground (above-ground? item))
    (assoc :at-ground (at-ground? item))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Configuration Interface ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn derive-properties
  "Derive certain properties from the base configuration."
  [getopt]
  (merge
    (prepare-criteria getopt)
    {:interface  ; An annotated version of the interface list.
      (->> (getopt :central-housing :shape :interface)
        (map categorize-explicitly)
        (locate-above-ground-points getopt)
        (locate-lip getopt)  ; Uses results from locate-above-ground-points.
        (locate-at-ground-points getopt)
        (locate-non-above-ground-points getopt)
        (vec))}))  ; Because literal lists are not indexable.

(defn interface
  "Access some precomputed set of coordinates from derive-properties."
  [getopt pred item-path]
  (mapv #(get-in % item-path)
        (filter pred (getopt :central-housing :derived :interface))))

(defn vertices
  "Access a coordinate sequence."
  ([getopt item-path]
   (vertices getopt :above-ground item-path))
  ([getopt pred item-path]
   (interface getopt pred (concat [:points pred] item-path))))


;;;;;;;;;;;;;;;;;;;
;; Model Interop ;;
;;;;;;;;;;;;;;;;;;;

(defn build-fastener
  "A threaded fastener for attaching a central housing to its adapter.
  This needs to be mirrored for the left-hand-side adapter, being chiral
  by default. Hence it is written for use as an OpenSCAD module."
  [getopt]
  (merge-bolt
    {:compensator (compensator getopt), :negative true}
    (getopt :central-housing :adapter :fasteners :bolt-properties)))

(defn adapter-right-fasteners
  "All of the screws (negative space) for one side of the housing and adapter."
  [getopt]
  (fastener-feature getopt any-side single-right-side-fastener))

(defn adapter-left-fasteners
  "All of the screws for the other side. Due to a curiosity of the way bilateral
  symmetry is currently implemented for the central housing, this function does
  not mirror the positions of adapter-right-fasteners."
  [getopt]
  (fastener-feature getopt any-side single-left-side-fastener))

(defn adapter-fastener-receivers
  "Receivers for screws, extending from the central housing into the adapter."
  [getopt]
  (fastener-feature getopt housing-side single-receiver))

(defn tweak-post
  "Place an adapter between the housing polyhedron and a case wall."
  [getopt alias]
  {:pre [(keyword? alias)]}
  (apply model/hull
    (for [depth [:outer :inner]]
      (place/at-named getopt {:anchor alias, :depth depth}
        (model/cube wafer wafer wafer)))))

(defn lip-body-right
  "A lip for an adapter."
  [getopt]
  (poly/tuboid
    (vertices getopt [:lip :outside :outer])
    (vertices getopt [:lip :outside :inner])
    (vertices getopt [:lip :inside :outer])
    (vertices getopt [:lip :inside :inner])))

(defn adapter-shell
  "An OpenSCAD polyhedron describing an adapter for the central housing.
  This is just the positive shape, excluding secondary features like fasteners,
  because those may affect other parts of the adapted case."
  [getopt]
  (maybe/union
    (poly/tuboid
      (vertices getopt [:gabel :right :outer])
      (vertices getopt [:gabel :right :inner])
      (vertices getopt [:adapter :outer])
      (vertices getopt [:adapter :inner]))
    (fastener-feature getopt adapter-side single-receiver)))

(defn main-shell
  "An OpenSCAD polyhedron describing the body of the central housing.
  For use in building both the central housing itself as a program output
  and a bottom plate at floor level."
  [getopt]
  (poly/tuboid
    (vertices getopt [:gabel :left :outer])
    (vertices getopt [:gabel :left :inner])
    (vertices getopt [:gabel :right :outer])
    (vertices getopt [:gabel :right :inner])))

(defn negatives
  "Collected negative space for the keyboard case model beyond the adapter."
  [getopt]
  (maybe/union
    (adapter-fastener-receivers getopt)
    (adapter-right-fasteners getopt)))
