;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Switches and Keycaps                                                ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.key.switch
  (:require [clojure.java.io :as io]
            [scad-clj.model :as model]
            [scad-tarmi.core :refer [abs π]]
            [scad-tarmi.maybe :as maybe]
            [scad-tarmi.util :refer [loft]]
            [dmote-keycap.measure :as measure]
            [dmote-keycap.models :refer [keycap]]
            [dactyl-keyboard.misc :as misc]
            [dactyl-keyboard.cad.misc :refer [wafer]]
            [dactyl-keyboard.cots :refer [switch-facts]]
            [dactyl-keyboard.param.access :refer [key-properties most-specific
                                                  compensator]]))

;;;;;;;;;;;;;;;;;;;
;; Keycap Models ;;
;;;;;;;;;;;;;;;;;;;

(defn mount-thickness
  "Key mount thickness."
  [getopt cluster coord]
  (most-specific getopt [:wall :segments 0 :size 2] cluster coord))

(defn switch-for-cap
  "Simplify the choice of key switch type for a keycap.
  This function translates types supported by this application, for the purpose
  of modelling mounting plates, with more basic types relevant to keycaps and
  supported by dmote-keycap."
  [raw] (case raw, :kailh-pg1511 :mx, raw))

(defn resting-clearance
  "Distance between key mount and lower edge of keycap at rest."
  [getopt cluster coord]
  (let [most #(most-specific getopt [:layout :clearance %] cluster coord)
        prop (key-properties getopt cluster coord)
        {:keys [switch-type skirt-length]} prop]
    (if (most :use-key-style)
      (measure/resting-clearance switch-type skirt-length)
      (most :nominal))))

(defn cap-channel-negative
  "The shape of a channel for a keycap to move in."
  [getopt cluster coord {h3 :height wd3 :top-width m :margin}]
  (let [cmp (compensator getopt)
        t 1
        step (fn [x y h]
               (model/translate [0 0 h] (model/cube (cmp x) (cmp y) t)))
        prop (key-properties getopt cluster coord)
        {:keys [switch-type skirt-length]} prop
        ;; The size of the switch with overhangs is not to be confused with
        ;; measure/switch-footprint.
        {sx :x, sy :y} (get-in switch-facts [switch-type :foot])
        [wx wy] (measure/skirt-footprint prop)
        h1 (measure/pressed-clearance switch-type skirt-length)
        h2 (resting-clearance getopt cluster coord)]
    (model/color (:cap-negative misc/colours)
      (model/translate [0 0 (mount-thickness getopt cluster coord)]
        (loft
          [(step (+ sx m) (+ sy m) (/ t 2)) ; A bottom plate for ease of mounting a switch.
           (step (+ sx m) (+ sy m) 1) ; Roughly the height of the foot of the switch.
           (step wx wy h1) ; Space for the keycap’s edges in travel.
           (step wx wy h2)
           (step wd3 wd3 h3)]))))) ; Space for the upper body of a keycap at rest.

(defn single-cap
  "The shape of one keycap at rest on its switch. This is intended for use in
  defining an OpenSCAD module that needs no further input.
  The ‘supported’ argument acts as a fallback in case the user has not
  specified the dmote-keycap parameter by that name. Supports are generally
  appropriate for printable STLs but not for cluster previews.
  The ‘importable-filepath-fn’ defined here is not context-sensitive like
  ‘supported’ but does not belong in configuration data, derived or otherwise,
  because it is opaque for the purpose of logging a configuration for
  troubleshooting, and does not affect other uses of a configuration."
  [getopt key-style supported]
  (->> (getopt :keys :derived key-style)
    (merge {:supported supported
            :importable-filepath-fn #(str (io/file misc/output-directory "scad" %))})
    keycap
    (model/color (:cap-body misc/colours))
    (maybe/translate [0 0 (getopt :keys :derived key-style :vertical-offset)])))

(defn cap-positive
  "Recall of the results of single-cap for a particular coordinate."
  [getopt cluster coord]
  (maybe/translate [0 0 (mount-thickness getopt cluster coord)]
    (model/call-module (:module-keycap (key-properties getopt cluster coord)))))


;;;;;;;;;;;;;;;;;;;;;;
;; Keyswitch Models ;;
;;;;;;;;;;;;;;;;;;;;;;

;; These models are intended solely for use as cutouts and therefore lack
;; features that would not interact with key mounts.

(defn- plate-blocks
  "Basic block outlines of a switch for negative space."
  [mount-type]
  (let [facts (mount-type switch-facts)
        {:keys [above-plate into-plate]} (:height facts)
        {hole-x :x, hole-y :y} (:hole facts)
        {foot-x :x, foot-y :y} (:foot facts)]
    (model/union
      ;; Space for the part of a switch above the mounting hole.
      (model/translate [0 0 (/ above-plate 2)]
        (model/cube foot-x foot-y above-plate))
      ;; The hole through the plate.
      (model/translate [0 0 (/ into-plate -2)]
        (model/cube hole-x hole-y (inc into-plate))))))

(defn- alps-wing
  "Negative space for a pair of wings flaring out from the base of an
  ALPS-style switch. This model is designed to hug the real thing quite
  closely, mainly for the purpose of making the mount easy to print at
  odd angles."
  [x-base y-base z-base]
  (let [y-wing 2  ; Length of a wing alongside the base of the switch.
        x-wing (+ x-base 0.85)  ; Flaring 0.4 mm away from the base, each side.
        z-gap 0.6   ; Distance from plate top to upper edge of wing.
        z-upper 1.5 ; Distance from upper edge of wing to point.
        z-lower 2.5 ; Distance from lower edge of wing to point.
        z-wing (+ z-upper z-lower)]  ; Total vertical extent of wing.
    (model/translate [0 (/ y-base 2) (- z-gap)]
      (model/hull
        (model/translate [0 (/ y-wing -2) (/ z-wing -2)]
          (model/cube x-base y-wing z-wing))
        (model/translate [0 0 (- z-upper)]
          (model/cube x-wing wafer wafer))))))

(defn- alps-switch
  "One ALPS-compatible cutout model."
  []
  (let [into-plate (get-in switch-facts [:alps :height :into-plate])
        {hole-x :x, hole-y :y} (get-in switch-facts [:alps :hole])]
    (model/union
      ;; The actual height of the notches is 1 mm and it’s not a full cuboid.
      (plate-blocks :alps)
      ;; ALPS-specific space for wings to flare out inside the plate.
      (model/union
        (alps-wing hole-x hole-y into-plate)
        (model/mirror [0 1 0]
          (alps-wing hole-x hole-y into-plate))))))

(defn- pg1511-switch
  "One cutout for just the tiny anteroposterior teeth common to Cherry MX and
  its clones, as well as a square hole in the plate. The teeth are designed
  to grip a 1.6 mm PCB."
  []
  (let [hole-y (get-in switch-facts [:mx :hole :x])]
    (model/union
      (plate-blocks :mx)
      (model/hull
        (model/translate [0 0 -2.1] (model/cube 4 (inc hole-y) 1))
        (model/translate [0 0 -4]   (model/cube 6 (dec hole-y) 4))))))

(defn- mx-switch
  "One Cherry MX-compatible cutout model with large nubs for lateral recesses."
  []
  (let [hole-xy (get-in switch-facts [:mx :hole :x])
        nub-radius 1
        nub-depth 4
        nub (->> (model/cylinder nub-radius 2.75)
                 (model/with-fn 20)
                 (model/rotate [(/ π 2) 0 0])
                 (model/translate [(/ hole-xy 2) 0 (- nub-radius nub-depth)])
                 (model/hull
                   (model/translate [(+ 3/4 (/ hole-xy 2)) 0 (/ nub-depth -2)]
                     (model/cube 1.5 2.75 nub-depth))))]
    (model/difference
      (pg1511-switch)
      (model/union
        nub
        (model/mirror [0 1 0] (model/mirror [1 0 0] nub))))))

(defn single-switch
  "Negative space for the insertion of a key switch through a mounting plate."
  [mount-type]
  (case mount-type
    :alps (alps-switch)
    :kailh-pg1511 (pg1511-switch)
    :mx (mx-switch)))
