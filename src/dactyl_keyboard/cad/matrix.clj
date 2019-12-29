;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Key Matrix Utilities                                                ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.matrix
  (:require [dactyl-keyboard.compass :as compass :refer [sharp-left sharp-right]]))

(defn coordinate-pairs
  ([columns rows] (for [column columns row rows] [column row]))
  ([columns rows selector] (filter selector (coordinate-pairs columns rows))))

(defn next-column
  "Each column runs along the y axis; changing columns changes x."
  [column direction]
  (+ column (compass/delta-x direction)))

(defn next-row
  "Each row runs along the x axis; changing rows changes y."
  [row direction]
  (+ row (compass/delta-y direction)))

(defn walk
  "A tuple describing the position an arbitrary orthogonal walk would lead to."
  [[column row] & directions]
  (if (empty? directions)
    [column row]
    (let [direction (first directions)]
      (apply (partial walk [(next-column column direction)
                            (next-row row direction)])
        (rest directions)))))

(defn- classify-corner
  "Classify the immediate surroundings of passed position looking ahead-left.
  Surveying must happen from an occupied position in the matrix.
  A checkered landscape (clear left, clear ahead, occluded diagonal) is not
  permitted."
  [occlusion-fn {:keys [coordinates direction] :as position}]
  {:pre [(occlusion-fn coordinates)]}
  (let [on-left (walk coordinates (sharp-left direction))
        ahead (walk coordinates direction)
        ahead-left (walk coordinates direction (sharp-left direction))
        landscape (vec (map occlusion-fn [on-left ahead-left ahead]))]
    (case landscape
      [false false false] :outer
      [false false true ] nil
      [false true  true ] :inner
      (throw (Exception.
               (format "Unforeseen landscape at %s: %s" position landscape))))))

(defn- step-clockwise
  "Pick the next position along the edge of a matrix.
  In an outer corner, turn right in place.
  When there is no corner, continue straight ahead.
  In an inner corner, jump diagonally ahead-left while also turning left."
  [occlusion-fn {:keys [coordinates direction] :as position}]
  (case (classify-corner occlusion-fn position)
    :outer (merge position {:direction (sharp-right direction)})
    nil    (merge position {:coordinates (walk coordinates direction)})
    :inner {:coordinates (walk coordinates direction (sharp-left direction))
            :direction (sharp-left direction)}))

(defn trace-edge
  "Walk the edge of a matrix, clockwise. Return a lazy, infinite sequence.
  Annotate each position with a description of how the edge turns."
  [occlusion-fn position]
  (lazy-seq
    (cons (merge position {:corner (classify-corner occlusion-fn position)})
          (trace-edge occlusion-fn (step-clockwise occlusion-fn position)))))

(defn trace-between
  "Walk the edge of a matrix from one position to another. By default, take
  one complete lap starting at [0 0]. As in an exclusive range, the final
  position will not be part of the output."
  ([occlusion-fn]
   (trace-between occlusion-fn {:coordinates [0 0], :direction :N}))
  ([occlusion-fn start-position]
   (trace-between occlusion-fn start-position start-position))
  ([occlusion-fn start-position stop-position]
   (let [[p0 & pn] (trace-edge occlusion-fn start-position)
         salient (fn [{:keys [coordinates direction]}] [coordinates direction])
         stop (salient stop-position)
         pred (fn [p] (not= (salient p) stop))]
     (concat [p0] (take-while pred pn)))))

(defn cube-vertex-offset
  "Compute a 3D offset from the center of a cube to a vertex on it."
  [corner [x y z] {:keys [bottom] :or {bottom true}}]
  {:pre [(compass/noncardinals corner)]}
  (let [directions (compass/keyword-to-tuple corner)]
    [(* (apply compass/delta-x directions) x)
     (* (apply compass/delta-y directions) y)
     ((if bottom - +) z)]))
