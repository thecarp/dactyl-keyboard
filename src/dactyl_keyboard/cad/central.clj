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

(defn collect-point-aliases
  "A map of aliases to corresponding indices in the edge array."
  [getopt]
  (into {} (map-indexed
             (fn [idx {:keys [alias]}]
               (when alias
                 [alias {:type :central-housing, :index idx}]))
             (getopt :case :central-housing :shape :edge))))

(defn derive-properties
  "Derive certain properties from the base configuration."
  [getopt]
  (let [thickness (getopt :case :web-thickness)
        width (getopt :case :central-housing :shape :width)
        edge (getopt :case :central-housing :shape :edge)
        points-3d (map :offset edge)
        base-polygon (mapv rest points-3d)
        inner-polygon (poly/from-outline base-polygon thickness)
        gabel-base (outline-back-to-3d points-3d base-polygon)
        gabel-inner (outline-back-to-3d points-3d inner-polygon)
        shift-right (horizontal-shifter #(+ (/ width 2) %))
        shift-left (horizontal-shifter #(- (/ width -2) %))
        right-gabel-outer (mapv shift-right gabel-base)
        left-gabel-outer (mapv shift-left gabel-base)
        right-gabel-inner (mapv shift-right gabel-inner)
        left-gabel-inner (mapv shift-left gabel-inner)]
    {:points {:gabel {:right {:outer right-gabel-outer
                              :inner right-gabel-inner}
                      :left {:outer left-gabel-outer
                             :inner left-gabel-inner}}}}))


;;;;;;;;;;;;;;;;;;;
;; Model Interop ;;
;;;;;;;;;;;;;;;;;;;

(defn tweak-post
  "Place an adapter between the housing polyhedron and a case wall."
  [getopt alias]
  {:pre [(keyword? alias)]}
  (let [index (:index (access/resolve-anchor
                        getopt alias #(= (:type %) :central-housing)))
        prop (partial getopt :case :central-housing :derived :points :gabel :right)]
    (model/hull
      (model/translate (prop :outer index) (model/cube wafer wafer wafer))
      (model/translate (prop :inner index) (model/cube wafer wafer wafer)))))


;;;;;;;;;;;;;
;; Outputs ;;
;;;;;;;;;;;;;

(defn main-body
  "An OpenSCAD polyhedron describing the body of the central housing."
  [getopt]
  (poly/tuboid
    (getopt :case :central-housing :derived :points :gabel :left :outer)
    (getopt :case :central-housing :derived :points :gabel :left :inner)
    (getopt :case :central-housing :derived :points :gabel :right :outer)
    (getopt :case :central-housing :derived :points :gabel :right :inner)))
