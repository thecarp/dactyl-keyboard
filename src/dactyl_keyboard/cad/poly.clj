;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Polygon/Polyhedron Functions                                        ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; These are general functions for placing vertices rather than building up
;;; complex shapes from primitive solids.

(ns dactyl-keyboard.cad.poly
  (:require [clojure.spec.alpha :as spec]
            [scad-clj.model :as model]
            [scad-tarmi.core :as tarmi :refer [π]]
            [thi.ng.geom.basicmesh :refer [basic-mesh]]
            [thi.ng.geom.bezier :refer [auto-spline2]]
            [thi.ng.geom.core :as geom]
            [thi.ng.geom.polygon :refer [inset-polygon polygon2]]
            [thi.ng.geom.vector :refer [vec2 vec3]]))


;;;;;;;;;;;;;;;
;; Internals ;;
;;;;;;;;;;;;;;;

(defn- same [a b]
  "True if a and b are the same point, regardless of numeric type."
  {:pre [(= (count a) (count b))]}
  (every? #(apply == %) (partition 2 (interleave a b))))

(defn- index-of [coll point]
  "A more expensive version of .indexOf for type-independent equality
  between vectors of numbers. This is intended to provide compatibility with
  thi.ng’s typing."
  (first (keep-indexed #(when (same %2 point) %1) coll)))

(defn tessellate
  "Tessellate an arbitary 3D surface via thi.ng.
  This is all about a round trip through the thi.ng API.
  Return a vector of triangles."
  ;; Exposed for unit testing only.
  [points]
  {:pre [(spec/valid? ::tarmi/point-coll-3d points)]
   :post [(spec/valid? (spec/coll-of ::tarmi/point-coll-3d) %)]}
  (->> [[(mapv vec3 points) nil]]
    (geom/into (basic-mesh))
    geom/tessellate
    :faces
    (map geom/vertices)
    sort
    vec))

(defn- tesselate-quadriteral
  "Naïve 3D tesselation: Return 2 triangles for 4 3D points."
  [points]
  {:pre [(spec/valid? ::tarmi/point-coll-3d points)
         (= (count points) 4)]
   :post [(spec/valid? (spec/coll-of ::tarmi/point-coll-3d) %)
          (= (count %) 2)]}
  [(butlast points) (reverse (rest points))])

(defn- bite-tail [coll] {:pre [(vector? coll)]} (conj coll (first coll)))
(defn- last-first [coll] (rest (bite-tail coll)))

(defn- pave-space
  "Describe a surface between two sequences of points of the same length.
  Return triangles described with point coordinates."
  [[a b]]
  {:pre [(spec/valid? ::tarmi/point-coll-3d a)
         (spec/valid? ::tarmi/point-coll-3d b)
         (= (count a) (count b))]
   :post [(spec/valid? (spec/coll-of ::tarmi/point-coll-3d) %)]}
  (mapcat tesselate-quadriteral
          (partition 4 2 (interleave (bite-tail a) (bite-tail b)))))

(defn- bevel-triangles
  [sides]
  (let [top-vertices (map first sides)  ; 1 coordinate sequence.
        corner-seqs (map second sides)  ; 4 pairs of coordinate sequences.
        corner-pairs (mapv (fn [[top lower]] (mapv (partial into [top]) lower))
                           sides)
        bevel-edges (vec (apply concat corner-pairs))  ; 8 coordinate sequences.
        side-pairs (partition 2 (last-first bevel-edges))]
    (concat
      ;; The top rectangle, as two triangles.
      (tessellate top-vertices)
      ;; The upper corners, one triangle on each side.
      (mapv (fn [[top [left right]]] [top (first right) (first left)]) sides)
      ;; The rectangular lower corners.
      (mapcat pave-space corner-seqs)
      ;; The upper bevels along the sides.
      (mapcat pave-space (take 4 side-pairs))
      ;; The sides proper, between each of the corners.
      (mapcat pave-space (drop 4 side-pairs))
      ;; The bottom octagon.
      (tessellate (map last bevel-edges)))))

(defn- tuboid-triangles
  "thi.ng triangles for a hollow tube of sorts."
  [outer-left inner-left outer-right inner-right]
  (mapcat pave-space
    [[outer-left inner-left]      ; Left-hand-side aperture.
     [inner-right outer-right]    ; Right-hand side aperture, reverse order.
     [inner-left inner-right]     ; Interior.
     [outer-right outer-left]]))  ; Exterior. Notice reverse order.


;;;;;;;;;;;;;;;;;;;;;;;;;
;; Interface Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(defn subdivide-arc
  "Produce intermediate angles inside an arc:
  Divite it equally among a given amount (n) of sectors.
  Return a vector of n + 1 angles from 0 to the full arc."
  [arc n]
  {:pre [(number? arc) (nat-int? n)]}
  (mapv #(* (/ % n) arc) (range (inc n))))

(def subdivide-right-angle (partial subdivide-arc (/ π 2)))

(defn from-outline
  "Draw a thi.ng polygon with some inset.
  Where this is not possible, throw an exception that shows the data, so
  that the fault can be traced to some part of a user’s configuration."
  [base-outline inset]
  {:pre [(spec/valid? ::tarmi/point-coll-2d base-outline)
         (number? inset)]
   :post [(spec/valid? ::tarmi/point-coll-2d %)]}
  (try
    (:points (polygon2 (inset-polygon (mapv vec2 base-outline) inset)))
    (catch IllegalArgumentException e
      (throw (ex-info
               (str "Unable to inset polygon. "
                    "A common cause of this problem is that the base polygon "
                    "is not wide enough at its thinnest point.")
               {:base-polygon base-outline
                :inset inset
                :error e})))))

(defn spline
  "2D coordinates along a closed spline through passed points.
  The stopping point is omitted from the output because, in a closed spline,
  it is necessarily identical to the starting point and scad-clj polygons
  don’t need that."
  [points resolution]
  {:pre [(spec/valid? ::tarmi/point-coll-2d points)
         (nat-int? resolution)]
   :post [(spec/valid? ::tarmi/point-coll-2d %)]}
  (butlast (geom/vertices (auto-spline2 (mapv vec2 points) true) resolution)))

(defn coords-to-indices
  "Take point coordinates and triangles referring to the same
  points by their coordinates. Return point-index triangles.
  This is intended to prepare a list of faces for an OpenSCAD polyhedron."
  ;; Notice that negative indices, as returned by .indexOf for unrecognized
  ;; inputs, are checked as illegal here, meaning that the inputs must match.
  ;; However, all numbers are treated as floats, so that 0 = 0.0 etc.
  [points triangles]
  {:pre [(spec/valid? ::tarmi/point-coll-2-3d points)
         (spec/valid? (spec/coll-of ::tarmi/point-coll-2-3d) triangles)]
   :post [(spec/valid? (spec/coll-of (spec/coll-of nat-int?)) %)]}
  (let [to-index (fn [coord] (index-of points coord))
        to-face (fn [triangle] (mapv to-index triangle))]
    (mapv to-face triangles)))

(defn bevelled-cuboid
  [side-tuples]
  {:pre [(spec/valid?
           (spec/coll-of
             (spec/tuple ::tarmi/point-3d
                         (spec/coll-of (spec/coll-of ::tarmi/point-3d)
                                       :count 2)))
           side-tuples)]}
  (let [points (mapcat (fn [[top [left right]]] (concat [top] left right))
                       side-tuples)]
    (model/polyhedron
      points
      (coords-to-indices points (bevel-triangles side-tuples))
      :convexity 1)))

(defn tuboid
  "A polyhedron describing an irregular ring- or tube-like shape.
  This is based on the naïve assumption that incoming point sequences describe
  the vertices of the object starting on the low end of all three axes and
  moving to higher values clockwise for the left-hand side (low x) of the
  object."
  ;; Assumptions and the logic of relevant internal functions
  ;; satisfy OpenSCAD’s requirement that:
  ;;   “All faces must have points ordered in the same direction. OpenSCAD
  ;;    prefers clockwise when looking at each face from outside inwards.”
  ;; Failing to meet this requirement will cause rendering errors on
  ;; intersection.
  [& point-seqs]
  {:pre [(spec/valid? (spec/coll-of ::tarmi/point-coll-3d) point-seqs)]}
  (let [points (apply concat point-seqs)]
    (model/polyhedron
      points
      (coords-to-indices points (apply tuboid-triangles point-seqs))
      :convexity 3)))
