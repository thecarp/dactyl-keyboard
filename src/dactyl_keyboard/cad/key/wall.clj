;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Key Cluster Perimeter Wall                                          ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; Functions for specifying parts of a perimeter wall. These all take the
;;; edge-walking algorithm’s map output with position and direction, upon
;;; seeing the need for each part.

(ns dactyl-keyboard.cad.key.wall
  (:require [scad-tarmi.util :refer [loft]]
            [scad-tarmi.maybe :as maybe]
            [scad-tarmi.flex :as flex]
            [dactyl-keyboard.cad.misc :as misc]
            [dactyl-keyboard.cad.matrix :as matrix]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.key :as key]
            [dactyl-keyboard.cad.key.web :refer [web-post]]
            [dactyl-keyboard.compass :as compass :refer [sharp-left sharp-right]]
            [dactyl-keyboard.param.access :refer [most-specific]]))


;;;;;;;;;;;;
;; Models ;;
;;;;;;;;;;;;

(defn- straight-body
  "The part of a case wall that runs along the side of a key mount on the
  edge of the board."
  [{:keys [coordinates direction]}]
  {:pre [(compass/cardinals direction)]}
  [[coordinates [(sharp-left direction) direction]]
   [coordinates [(sharp-left direction) (compass/reverse direction)]]])

(defn- straight-join
  "The part of a case wall that runs between two key mounts in a straight line.
  This differs from straight-body in that the second coordinate pair belongs to
  the next key."
  [{:keys [coordinates direction] :as position}]
  {:pre [(compass/cardinals direction)]}
  (assoc-in (straight-body position) [1 0] (matrix/walk coordinates direction)))

(defn- outer-corner
  "The part of a case wall that smooths out an outer, sharp corner."
  [{:keys [coordinates direction]}]
  {:pre [(compass/cardinals direction)]}
  [[coordinates [(sharp-left direction) direction]]
   [coordinates [direction (sharp-left direction)]]])

(defn- inner-corner
  "The part of a case wall that covers any gap in an inner corner.
  In this case, it is important to pick not only the right corner but the
  right direction moving out from that corner."
  [{:keys [coordinates direction]}]
  {:pre [(compass/cardinals direction)]}
  (let [opposite (matrix/walk coordinates (sharp-left direction) direction)]
    [[coordinates [(sharp-left direction) direction]]
     [opposite [(compass/reverse direction) (sharp-right direction)]]]))

(defn connecting-wall
  [{:keys [corner] :as position}]
  (case corner
    ::matrix/outer (outer-corner position)
    nil (straight-join position)
    ::matrix/inner (inner-corner position)))


;;;;;;;;;;;;;;;;;;
;; Edge Walking ;;
;;;;;;;;;;;;;;;;;;

(defn- property
  [getopt parameter cluster [coord side-tuple]]
  (let [side (compass/tuple-to-intermediate side-tuple)]
    (most-specific getopt [:wall parameter] cluster coord side)))

(defn- edge-post
  "Place an individual wall post."
  [getopt cluster coord side segment]
  (->>
    (web-post getopt cluster coord (first side) segment)
    (flex/translate
      (place/wall-corner-offset getopt cluster coord
        {:side (compass/tuple-to-intermediate side)
         :segment segment}))
    (place/cluster-place getopt cluster coord)))

(defn- combine-pair
  "Combine two posts, one from each of two edges."
  [getopt cluster edge-and-segment-pairs]
  (apply maybe/hull
    (map (fn [[[coord side] segment]]
           (edge-post getopt cluster coord side segment))
         edge-and-segment-pairs)))

(defn- segment-pair
  [getopt cluster edges braid segment-index]
  "Make posts at one segment along passed edges."
  (combine-pair getopt cluster
    (map (fn [edge-index]
           (let [edge (nth edges edge-index)
                 segments (nth braid edge-index)]
             [edge (nth segments segment-index)]))
         (range (count edges)))))

(defn- asymmetric-segment-post-pairs
  "Make posts of each segment along passed edges."
  [getopt cluster edges braid]
  (map (partial segment-pair getopt cluster edges braid)
       (range (count (first braid)))))

(defn- pair-to-ground
  [getopt cluster edges]
  (when (every? (partial property getopt :to-ground cluster) edges)
    (apply misc/bottom-hull
      (map (fn [[coord side :as edge]]
             (edge-post getopt cluster coord side
               (property getopt :extent cluster edge)))
           edges))))

(defn- segment-sequence
  "Produce a sequence of segment IDs at one corner."
  [getopt cluster edge]
  (range (inc (property getopt :extent cluster edge))))

(defn- segment-sequence-pair
  "Produce a sequence of segment IDs per edge."
  [getopt cluster edges]
  (map (fn [position] (segment-sequence getopt cluster position)) edges))

(defn- braid-segments
  "Produce one sequence of segment IDs per edge.
  Short sequences are padded here to match long ones, by repeating their last
  entry."
  [getopt cluster edges]
  (let [pair (segment-sequence-pair getopt cluster edges)
        [shorter longer] (sort-by count pair)
        n (count longer)
        pad (last shorter)]
    (map (fn [segments] (take n (concat segments (repeat pad)))) pair)))

(defn- slab
  "Produce a single shape joining two edges."
  [getopt cluster edges]
  (maybe/union
    (loft (asymmetric-segment-post-pairs getopt cluster edges
            (braid-segments getopt cluster edges)))
    (pair-to-ground getopt cluster edges)))

(defn cluster
  "Walk the perimeter of a key cluster, walling it in."
  [getopt cluster]
  (apply maybe/union
    (mapcat
      (fn [position] [(slab getopt cluster (straight-body position))
                      (slab getopt cluster (connecting-wall position))])
      (key/walk-cluster getopt cluster))))

