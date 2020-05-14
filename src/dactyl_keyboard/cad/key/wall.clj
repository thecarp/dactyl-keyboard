;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Key Cluster Perimeter Wall                                          ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; Functions for specifying parts of a perimeter wall. These all take the
;;; edge-walking algorithm’s map output with position and direction, upon
;;; seeing the need for each part.

(ns dactyl-keyboard.cad.key.wall
  (:require [scad-clj.model :as model]
            [dactyl-keyboard.cad.misc :as misc]
            [dactyl-keyboard.cad.matrix :as matrix]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.key :as key]
            [dactyl-keyboard.compass :as compass :refer [sharp-left sharp-right]]))


;;;;;;;;;;;;
;; Models ;;
;;;;;;;;;;;;

(defn- straight-body
  "The part of a case wall that runs along the side of a key mount on the
  edge of the board."
  [{:keys [coordinates direction]}]
  {:pre [(compass/cardinals direction)]}
  (let [facing (sharp-left direction)]
    [[coordinates facing sharp-right]
     [coordinates facing sharp-left]]))

(defn- straight-join
  "The part of a case wall that runs between two key mounts in a straight line."
  [{:keys [coordinates direction]}]
  {:pre [(compass/cardinals direction)]}
  (let [next-coord (matrix/walk coordinates direction)
        facing (sharp-left direction)]
    [[coordinates facing sharp-right]
     [next-coord facing sharp-left]]))

(defn- outer-corner
  "The part of a case wall that smooths out an outer, sharp corner."
  [{:keys [coordinates direction]}]
  {:pre [(compass/cardinals direction)]}
  (let [original-facing (sharp-left direction)]
    [[coordinates original-facing sharp-right]
     [coordinates direction sharp-left]]))

(defn- inner-corner
  "The part of a case wall that covers any gap in an inner corner.
  In this case, it is import to pick not only the right corner but the right
  direction moving out from that corner."
  [{:keys [coordinates direction]}]
  {:pre [(compass/cardinals direction)]}
  (let [opposite (matrix/walk coordinates (sharp-left direction) direction)]
    [[coordinates (sharp-left direction) (constantly direction)]
     [opposite (compass/reverse direction) sharp-left]]))

(defn connecting-wall
  [{:keys [corner] :as position}]
  (case corner
    :outer (outer-corner position)
    nil (straight-join position)
    :inner (inner-corner position)))


;;;;;;;;;;;;;;;;;;
;; Edge Walking ;;
;;;;;;;;;;;;;;;;;;

(defn- edge-post
  "Run wall-edge-sequence with a web post as its subject."
  [getopt cluster upper edge]
  (place/wall-edge-sequence getopt cluster upper edge (key/web-post getopt)))

(defn- slab
  "Produce a single shape joining some (two) edges."
  [getopt cluster edges]
  (let [upper (map (partial edge-post getopt cluster true) edges)
        lower (map (partial edge-post getopt cluster false) edges)]
   (model/union
     (apply model/hull upper)
     (apply misc/bottom-hull lower))))

(defn cluster
  "Walk the edge of a key cluster, walling it in."
  [getopt cluster]
  (apply model/union
    (mapcat
      (fn [position] [(slab getopt cluster (straight-body position))
                      (slab getopt cluster (connecting-wall position))])
      (key/walk-cluster getopt cluster))))

