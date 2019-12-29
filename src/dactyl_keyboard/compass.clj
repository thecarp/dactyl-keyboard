;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; The Compass Metaphor                                                ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module defines the metaphor of a compass as a user-friendly way
;;; to refer to parts of and directions away from features of the keyboard,
;;; in the plane of each feature.

(ns dactyl-keyboard.compass
  (:refer-clojure :exclude [reverse])
  (:require [clojure.set :refer [map-invert union]]
            [scad-tarmi.core :refer [π]]))

(def directions
  "Clockwise around the compass using upper-case keywords."
  [:N :NNE :NE :ENE :E :ESE :SE :SSE :S :SSW :SW :WSW :W :WNW :NW :NNW])
(def n-divisions (count directions))

(def long-to-short
  "Longer names are used for cardinal directions in the configuration layer."
  {:north :N
   :east  :E
   :south :S
   :west  :W})
(def short-to-long (map-invert long-to-short))

(defn- select-length [n] (set (filter #(= (count (name %)) n) directions)))

(def cardinals (select-length 1))       ; North, south, etc. 4 directions.
(def intercardinals (select-length 2))  ; Northeast, southeast, etc. Also 4.
(def intermediates (select-length 3))   ; North-by-northeast etc. 8 directions.
(def noncardinals (union intercardinals intermediates))  ; 12 directions.

;; The twelve directions intermediate between the cardinals and intercardinals
;; can be represented as 2-tuples of cardinals. In this representation, the
;; order of the tuple corresponds to the primacy of each constituent cardinal.
(def intercardinal-to-tuple
  {:NE [:N :E]
   :SE [:S :E]
   :SW [:S :W]
   :NW [:N :W]})
(def pair-to-intercardinal
  (into {} (map (fn [[k v]] [(set v) k]) intercardinal-to-tuple)))

;; This scheme is particularly useful for describing extensions that start in
;; one roughly intercardinal corner of a squarish shape, such as a key mount,
;; and extend in a cardinal direction from that corner, in such a way that the
;; final position is intermediate between a cardinal and a diagonal
;; intercardinal direction.
(def intermediate-to-tuple
  {:NNE [:N :E]
   :ENE [:E :N]
   :SSE [:S :E]
   :ESE [:E :S]
   :SSW [:S :W]
   :WSW [:W :S]
   :NNW [:N :W]
   :WNW [:W :N]})
(def tuple-to-intermediate (map-invert intermediate-to-tuple))
;; Intermediates can also be resolved to their nearest intercardinal.
(let [f (fn [i] [i (get pair-to-intercardinal (set (i intermediate-to-tuple)))])]
  (def intermediate-to-intercardinal (into {} (map f intermediates))))

(defn convert-to-intercardinal
  "Take a corner keyword. Return the nearest intercardinal direction."
  [corner]
  {:pre [(noncardinals corner)] :post [#(intercardinals %)]}
  (get intermediate-to-intercardinal corner corner))

(def keyword-to-tuple (merge intercardinal-to-tuple intermediate-to-tuple))

(def tuples (set (vals intermediate-to-tuple))) ; All 2-tuples of cardinals.

(defn- turn
  "Retrieve a direction keyword for turning clockwise."
  [steps-clockwise starting-direction]
  (let [i (.indexOf directions starting-direction)]
    (get directions (mod (+ i steps-clockwise) n-divisions))))

(def sharp-right (partial turn (/ n-divisions 4)))
(def sharp-left (partial turn (/ n-divisions -4)))
(def reverse (partial turn (/ n-divisions 2)))

(def radians
  {:N 0
   :E (/ π 2)
   :S π
   :W (/ π -2)})

(def to-grid
  "Translation particles for each cardinal direction."
  (array-map
   :N {:dx  0, :dy  1},
   :E {:dx  1, :dy  0},
   :S {:dx  0, :dy -1},
   :W {:dx -1, :dy  0}))

(defn- axis-delta
  "Find a coordinate axis delta for movement in any of the stated directions."
  [axis direction & directions]
  {:pre [(cardinals direction)]}
  (let [value (get-in to-grid [direction axis])]
    (if (or (not (zero? value)) (empty? directions))
      value
      (apply axis-delta axis directions))))

(defn delta-x [& directions] (apply axis-delta :dx directions))
(defn delta-y [& directions] (apply axis-delta :dy directions))
