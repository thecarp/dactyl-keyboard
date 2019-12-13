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
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.param.access :as access]))


;;;;;;;;;;;;;;;
;; Internals ;;
;;;;;;;;;;;;;;;

(defn- outline-back-to-3d
  [base-3d outline]
  (map (fn [[x _ _] [y1 z1]] [x y1 z1]) base-3d outline))

(defn- horizontal-shifter [x-fn] (fn [[x y z]] [(x-fn x) y z]))


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

(defn- inset-interface
  "Inset a 3D sequence via 2D. Return a vector for indexability."
  [base inset]
  (as-> base subject
    (mapv rest subject)
    (poly/from-outline subject inset)
    (outline-back-to-3d base subject)
    (vec subject)))

(defn derive-properties
  "Derive certain properties from the base configuration."
  [getopt]
  (let [thickness (getopt :case :web-thickness)
        body-width (getopt :case :central-housing :shape :width)
        adapter-width (getopt :case :central-housing :adapter :width)
        interface (getopt :case :central-housing :shape :interface)
        gabel-points-3d (map #(get-in % [:base :offset]) interface)
        gabel-base (inset-interface gabel-points-3d 0)
        gabel-inner (inset-interface gabel-points-3d thickness)
        shift-right (horizontal-shifter #(+ (/ body-width 2) %))
        shift-left (horizontal-shifter #(- (/ body-width -2) %))
        right-gabel-outer (mapv shift-right gabel-base)
        left-gabel-outer (mapv shift-left gabel-base)
        right-gabel-inner (mapv shift-right gabel-inner)
        left-gabel-inner (mapv shift-left gabel-inner)
        adapter-points-3d (map #(get-in % [:adapter :offset] [0 0 0]) interface)
        adapter-intrinsic (inset-interface adapter-points-3d 0)
        adapter-outer (mapv (partial map + [adapter-width 0 0])
                            right-gabel-outer adapter-intrinsic)
        adapter-inner (inset-interface adapter-outer thickness)]
    {:points {:gabel {:right {:outer right-gabel-outer
                              :inner right-gabel-inner}
                      :left {:outer left-gabel-outer
                             :inner left-gabel-inner}}
              :adapter {:outer adapter-outer
                        :inner adapter-inner}}}))


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

(defn adapter
  "An OpenSCAD polyhedron describing an adapter for the central housing."
  [getopt]
  (poly/tuboid
    (getopt :case :central-housing :derived :points :gabel :right :outer)
    (getopt :case :central-housing :derived :points :gabel :right :inner)
    (getopt :case :central-housing :derived :points :adapter :outer)
    (getopt :case :central-housing :derived :points :adapter :inner)))

(defn main-body
  "An OpenSCAD polyhedron describing the body of the central housing."
  [getopt]
  (poly/tuboid
    (getopt :case :central-housing :derived :points :gabel :left :outer)
    (getopt :case :central-housing :derived :points :gabel :left :inner)
    (getopt :case :central-housing :derived :points :gabel :right :outer)
    (getopt :case :central-housing :derived :points :gabel :right :inner)))
