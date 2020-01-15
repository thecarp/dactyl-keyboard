;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Central Housing                                                     ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.central
  (:require [scad-clj.model :as model]
            [scad-tarmi.maybe :as maybe]
            [scad-tarmi.threaded :as threaded]
            [dactyl-keyboard.cad.misc :refer [wafer]]
            [dactyl-keyboard.cad.poly :as poly]
            [dactyl-keyboard.cad.place :as place]))


;;;;;;;;;;;;;;;
;; Internals ;;
;;;;;;;;;;;;;;;

(defn- outline-back-to-3d
  [base-3d outline]
  (map (fn [[x _ _] [y1 z1]] [x y1 z1]) base-3d outline))

(defn- horizontal-shifter [x-fn] (fn [[x y z]] [(x-fn x) y z]))

;; Predicates for sorting fasteners by the object they penetrate.
(defn- adapter-side [{:keys [lateral-offset]}] (pos? lateral-offset))
(defn- housing-side [{:keys [lateral-offset]}] (neg? lateral-offset))

(defn- bilateral
  ([achiral-subject]
   (bilateral achiral-subject achiral-subject))
  ([right-handed left-handed]
   (model/union right-handed (model/mirror [-1 0 0] left-handed))))

(defn- fastener-feature
  "The union of all features produced by a given model function at the sites of
  all adapter fasteners matching a predicate function, on the right-hand side."
  [getopt pred subject]
  (let [positions (getopt :case :central-housing :adapter :fasteners :positions)
        subject-fn #(place/chousing-fastener getopt % subject)]
    (apply model/union (map subject-fn (filter pred positions)))))

(defn- single-fastener
  "A fastener for attaching the central housing to the rest of the case.
  In place."
  [getopt]
  (let [prop (partial getopt :case :central-housing :adapter :fasteners)]
    (threaded/bolt
      :iso-size (prop :diameter),
      :head-type :countersunk,
      :point-type :cone,
      :total-length (prop :length),
      :compensator (getopt :dfm :derived :compensator)
      :negative true)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Configuration Interface ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- collect-point-pair
  [idx {:keys [base adapter]}]
  (let [props {:type :central-housing, :index idx}
        pluck (fn [alias part extra]
                (when alias [alias (merge props {:part part} extra)]))]
    [(pluck (:alias base) :gabel {:side :right})
     (pluck (:alias adapter) :adapter {})]))

(defn collect-point-aliases
  "A map of aliases to corresponding indices in the interface array."
  [getopt]
  (->> (getopt :case :central-housing :shape :interface)
    (map-indexed collect-point-pair)
    (apply concat)
    (into {})))

(defn- shift-points
  "Manipulate a series of 3D points forming a perimeter.
  Inset (contract) the points in the yz plane (in 2D) and/or shift each point
  on the x axis (back in 3D). Return a vector for indexability."
  ([base]  ; Presumably called for vector conversion.
   (shift-points base 0))
  ([base inset]
   (shift-points base inset 0))
  ([base inset delta-x]
   (shift-points base inset + delta-x))
  ([base inset x-operator delta-x]
   (as-> base subject
     (mapv rest subject)
     (poly/from-outline subject inset)
     (outline-back-to-3d base subject)
     (mapv (horizontal-shifter #(x-operator % delta-x)) subject))))

(defn derive-properties
  "Derive certain properties from the base configuration."
  [getopt]
  (let [thickness (getopt :case :web-thickness)
        half-width (/ (getopt :case :central-housing :shape :width) 2)
        adapter-width (getopt :case :central-housing :adapter :width)
        interface (getopt :case :central-housing :shape :interface)
        base-points (map #(get-in % [:base :offset]) interface)
        gabel-out (shift-points base-points 0 half-width)
        gabel-in (shift-points base-points thickness half-width)
        adapter-points-3d (map #(get-in % [:adapter :offset] [0 0 0]) interface)
        adapter-intrinsic (shift-points adapter-points-3d)
        adapter-outer (mapv (partial map + [adapter-width 0 0])
                            gabel-out adapter-intrinsic)
        lip-t (getopt :case :central-housing :adapter :lip :thickness)
        lip-w (partial getopt :case :central-housing :adapter :lip :width)
        include-adapter (and (getopt :reflect)
                             (getopt :case :central-housing :include)
                             (getopt :case :central-housing :adapter :include))]
    {:include-adapter include-adapter
     :include-lip (and include-adapter
                       (getopt :case :central-housing :adapter :lip :include))
     :points
      {:gabel {:right {:outer gabel-out
                       :inner gabel-in}
               :left {:outer (shift-points base-points 0 - half-width)
                      :inner (shift-points base-points thickness - half-width)}}
       :adapter {:outer adapter-outer
                 :inner (shift-points adapter-outer thickness)}
       :lip {:outside {:outer (shift-points gabel-in 0 (lip-w :outer))
                       :inner (shift-points gabel-in lip-t (lip-w :outer))}
             :inside {:outer (shift-points gabel-in 0 - (+ (lip-w :taper)
                                                           (lip-w :inner)))
                      :inner (shift-points gabel-in lip-t - (lip-w :inner))}}}}))


;;;;;;;;;;;;;;;;;;;
;; Model Interop ;;
;;;;;;;;;;;;;;;;;;;

(defn tweak-post
  "Place an adapter between the housing polyhedron and a case wall."
  [getopt alias]
  {:pre [(keyword? alias)]}
  (let [shape (model/cube wafer wafer wafer)]
    (model/hull
      (place/reckon-from-anchor getopt alias {:depth :outer, :subject shape})
      (place/reckon-from-anchor getopt alias {:depth :inner, :subject shape}))))


;;;;;;;;;;;;;
;; Outputs ;;
;;;;;;;;;;;;;

(defn lip-body-right
  [getopt]
  (let [vertices (partial getopt :case :central-housing :derived :points :lip)]
    (poly/tuboid
      (vertices :outside :outer)
      (vertices :outside :inner)
      (vertices :inside :outer)
      (vertices :inside :inner))))

(defn adapter-shell
  "An OpenSCAD polyhedron describing an adapter for the central housing.
  This is just the basic shape, excluding secondary features like fasteners,
  because those may affect other parts of the adapted case."
  [getopt]
  (let [vertices (partial getopt :case :central-housing :derived :points)]
    (poly/tuboid
      (vertices :gabel :right :outer)
      (vertices :gabel :right :inner)
      (vertices :adapter :outer)
      (vertices :adapter :inner))))

(defn adapter-fasteners
  [getopt]
  (fastener-feature getopt adapter-side (single-fastener getopt)))

(defn main-body
  "An OpenSCAD polyhedron describing the body of the central housing."
  [getopt]
  (let [vertices (partial getopt :case :central-housing :derived :points)]
    (maybe/difference
      (maybe/union
        (poly/tuboid
          (vertices :gabel :left :outer)
          (vertices :gabel :left :inner)
          (vertices :gabel :right :outer)
          (vertices :gabel :right :inner))
        (when (getopt :case :central-housing :derived :include-lip)
          (bilateral (lip-body-right getopt))))
      (when (getopt :case :central-housing :derived :include-adapter)
        (bilateral
          (fastener-feature getopt housing-side (single-fastener getopt)))))))
