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
            [scad-tarmi.core :refer [τ]]))

(def directions
  "Clockwise around the compass."
  [:N :NNE :NE :ENE :E :ESE :SE :SSE :S :SSW :SW :WSW :W :WNW :NW :NNW])
(def n-divisions (count directions))

(def long-to-short
  "Longer names are used for cardinal directions in the configuration layer."
  {:north :N
   :east  :E
   :south :S
   :west  :W})

(let [short (into {} (map-indexed
                       (fn [i d] [d (- (/ (* i τ) n-divisions))])
                       directions))]
  (def radians
    "A map of compass points, including long and short names, to angles in
    radians, for counterclockwise rotation, as seen from above.
    This represents a somewhat literal interpretation of the compass metaphor
    and is not the only interpretation used in the application."
    (merge short
           (into {} (map (fn [[k v]] [k (v short)]) long-to-short)))))

(def matrices
  "2x2 matrices for rotating cleanly in the cardinal directions.
  This is for use where the radians computed above are too lossy as a result of
  floating-point arithmetic, for e.g. unit testing purposes."
  {:N [[1 0] [0 1]]
   :E [[0 1] [-1 0]]
   :S [[-1 0] [0 -1]]
   :W [[0 -1] [1 0]]})

(defn- select-length [n] (set (filter #(= (count (name %)) n) directions)))

(def cardinals (select-length 1))       ; North, south, etc. 4 directions.
(def intercardinals (select-length 2))  ; Northeast, southeast, etc. Also 4.
(def intermediates (select-length 3))   ; North-by-northeast etc. 8 directions.
(def all-short (union cardinals intercardinals intermediates))  ; 16 directions.
(def nonintermediates (union cardinals intercardinals))  ; 8 directions.
(def noncardinals (union intercardinals intermediates))  ; 12 directions.

(defn classify
  "Classify a specific direction. Return a namespaced keyword."
  [direction]
  {:pre [(direction all-short)]}
  (cond
    (direction cardinals) ::cardinal
    (direction intercardinals) ::intercardinal
    :else ::intermediate))

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

(def noncardinal-to-tuple
  (merge intercardinal-to-tuple intermediate-to-tuple))

(let [base (map-invert long-to-short)]
  (def short-to-long
    (merge base (into {} (map (fn [[k [v0 _]]] [k (v0 base)])
                              noncardinal-to-tuple)))))

(defn convert-to-cardinal
  "Take a compass-point keyword. Return the nearest cardinal direction."
  [direction]
  {:pre [(all-short direction)] :post [(% cardinals)]}
  (if-let [tuple (get noncardinal-to-tuple direction)]
    (first tuple)
    direction))

(defn convert-to-intercardinal
  "Take a compass-point keyword. Return the nearest intercardinal direction."
  [direction]
  {:pre [(noncardinals direction)] :post [(% intercardinals)]}
  (get intermediate-to-intercardinal direction direction))

(defn convert-to-nonintermediate
  "Take any short compass keyword. Return the nearest nonintermediate direction."
  [direction]
  {:pre [(all-short direction)] :post [(% nonintermediates)]}
  (if (intermediates direction)
    (convert-to-intercardinal direction)
    direction))

(defn convert-to-any-short
  "Accept a long or short keyword for any compass point. Return a short form."
  [direction]
  {:post [(% all-short)]}
  (get long-to-short direction direction))

(def keyword-to-tuple (merge intercardinal-to-tuple intermediate-to-tuple))

(def tuples (set (vals intermediate-to-tuple))) ; All 2-tuples of cardinals.

(let [n-quadrants 4  ; These are τ/4-radian fields, not coordinate quadrants.
      overflow (dec n-quadrants)
      quadrant-size (/ n-divisions n-quadrants)
      left-bound (dec n-divisions)]
  (defn northern-modulus
    "Shift any direction into its equivalent for the first cardinal.
    Any cardinal direction becomes north; any other direction becomes something
    closer to north than to other cardinals."
    [starting-direction]
    (let [m (mod (.indexOf directions starting-direction) quadrant-size)]
      ;; Treat e.g. ENE (index 3) as NNW (index 15) on a 16-wind compass.
      ;; Otherwise use the modulus as is.
      (get directions (if (= m overflow) left-bound m)))))

(defn- turn
  "Retrieve a direction keyword for turning clockwise."
  [steps-clockwise starting-direction]
  (let [i (.indexOf directions starting-direction)]
    (get directions (mod (+ i steps-clockwise) n-divisions))))

(def gentle-right (partial turn 1))
(def gentle-left (partial turn -1))
(def sharp-right (partial turn (/ n-divisions 4)))
(def sharp-left (partial turn (/ n-divisions -4)))
(def reverse (partial turn (/ n-divisions 2)))

(let [base {:N [0 1], :E [1 0], :S [0 -1], :W [-1 0]}
      store (reduce-kv
              (fn [coll direction constituents]
                (assoc coll direction (apply mapv + (map base constituents))))
              base
              noncardinal-to-tuple)]
  (defn to-grid
    "Find unit-scale translation particles for a compass point.
    The cardinal directions have hardcoded [x y] vectors and the rest combine
    two of these by addition.
    The function supports drawing boxes with bevelled edges, where the intermediate
    directions produce the same grid offsets as their nearest cardinals. Applied
    to key mounts, this is a Dactyl convention, older than the compass metaphor
    introduced by the DMOTE application."
    ([direction]
     (to-grid direction false))
    ([direction box]
     {:pre [(direction all-short)] :post [(vector? %) (= (count %) 2)]}
     (if (and box (direction intermediates))
       (to-grid (convert-to-cardinal direction) false)
       (direction store)))
    ([direction box axis]
     (get (to-grid direction box) axis))))

(defn to-index
  "Return a vector index for the axis of movement in a cardinal direction."
  [direction]
  {:pre [(cardinals direction)] :post [(#{0 1} %)]}
  (if (zero? (first (to-grid direction))) 1 0))

(defn- axis-delta
  "Find a coordinate axis delta for movement in any of the stated directions."
  ([axis]
   0)
  ([axis direction & directions]
   {:pre [(cardinals direction)]}
   (let [value (to-grid direction false axis)]
     (if (or (not (zero? value)) (empty? directions))
       value
       (apply axis-delta axis directions)))))

(defn delta-x [& directions] (apply axis-delta 0 directions))
(defn delta-y [& directions] (apply axis-delta 1 directions))
