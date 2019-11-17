;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Central Housing                                                     ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.central
  (:require [scad-clj.model :as model]
            [scad-tarmi.maybe :as maybe]
            [scad-tarmi.threaded :as threaded]
            [dactyl-keyboard.cad.poly :as poly]))


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

(defn derive-properties
  "Derive certain properties from the base configuration."
  [getopt]
  (let [thickness (getopt :case :web-thickness)
        width 100
        points-3d [[0 -20 -5] [0 -20 30] [0 -15 35] [0 20 35] [0 20 -5]]
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
