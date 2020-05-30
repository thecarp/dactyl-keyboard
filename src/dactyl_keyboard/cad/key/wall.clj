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
    :outer (outer-corner position)
    nil (straight-join position)
    :inner (inner-corner position)))


;;;;;;;;;;;;;;;;;;
;; Edge Walking ;;
;;;;;;;;;;;;;;;;;;

(defn- edge-post
  "Place an individual wall post."
  [getopt cluster coord side segment]
  (->>
    (key/web-post getopt)
    (flex/translate
      (place/wall-corner-offset getopt cluster coord
        {:side (compass/tuple-to-intermediate side)
         :segment segment}))
    (place/cluster-place getopt cluster coord)))

(defn- hull
  "Pick and apply an appropriate hull function for two edges.
  When both edges go to the bottom, use bottom-hull. However, if only one edge
  goes to the bottom, ignore the pair, returning nil, so that edges without a
  specified full-extent wall do not reach the bottom with their neighbours."
  [edge-and-segment-pairs & shapes]
  (let [segments (set (map last edge-and-segment-pairs))
        function (cond (= segments #{::to-ground}) misc/bottom-hull
                       (::to-ground segments) (constantly nil)
                       :else maybe/hull)]
    (apply function shapes)))

(defn- post-pair
  "Combine two posts, one from each of two edges."
  [getopt cluster edge-and-segment-pairs]
  (apply hull edge-and-segment-pairs
    (map (fn [[[coord side] segment]]
           (edge-post getopt cluster coord side segment))
         edge-and-segment-pairs)))

(defn- post-pair2
  [getopt cluster edges braid segment-index]
  "Make posts at one segment along passed edges."
  (post-pair getopt cluster
    (map (fn [edge-index]
           (let [edge (nth edges edge-index)
                 segments (nth braid edge-index)]
             [edge (nth segments segment-index)]))
         (range (count edges)))))

(defn- shared-posts
  "Make posts of each segment along passed edges."
  [getopt cluster edges braid]
  (map (partial post-pair2 getopt cluster edges braid)
       (range (count (first braid)))))

(defn- segment-sequence
  "Produce a sequence of segment IDs at one corner."
  [getopt cluster [coord [short-direction _]]]
  (let [direction (short-direction compass/short-to-long)
        extent (most-specific getopt [:wall direction :extent] cluster coord)
        full (= extent :full)]
    (concat
      (range (inc (if full 4, extent)))
      (when full [::to-ground]))))

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
  (loft (shared-posts getopt cluster edges
          (braid-segments getopt cluster edges))))

(defn cluster
  "Walk the perimeter of a key cluster, walling it in."
  [getopt cluster]
  (apply maybe/union
    (mapcat
      (fn [position] [(slab getopt cluster (straight-body position))
                      (slab getopt cluster (connecting-wall position))])
      (key/walk-cluster getopt cluster))))

