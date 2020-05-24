;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Polygon/Polyhedron Functions                                        ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; These are general functions for placing vertices rather than building up
;;; complex shapes from primitive solids.

;;; A lot of the logic here is intended to satisfy OpenSCAD’s requirement that:
;;;
;;;   “All faces must have points ordered in the same direction. OpenSCAD
;;;    prefers clockwise when looking at each face from outside inwards.”
;;;
;;; Failing to meet this requirement will cause rendering errors on
;;; intersection, difference etc.
;;;
;;; thi.ng.geom seems to have no corresponding requirement, nor does this module
;;; compute face normals to ensure that faces are correctly ordered. Instead,
;;; it has unchecked expectations upon its inputs, which is brittle.

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

(defn- face-for-geom [face] [(mapv vec3 face) nil])

(defn tessellate
  "Tessellate an arbitary 3D surface via thi.ng.
  This is all about a round trip through the thi.ng API.
  Return a vector of triangles."
  ;; Exposed for unit testing only.
  [faces]
  {:pre [(spec/valid? (spec/coll-of ::tarmi/point-coll-3d) faces)]
   :post [(spec/valid? (spec/coll-of ::tarmi/point-coll-3d) %)]}
  (->> (mapv face-for-geom faces)
    (geom/into (basic-mesh))
    geom/tessellate
    :faces  ; Return a set, lose deterministic order.
    (map geom/vertices)
    sort  ; Create a new deterministic order.
    vec))

(defn- bite-tail [coll] {:pre [(vector? coll)]} (conj coll (first coll)))
(defn- last-first [coll] (rest (bite-tail coll)))

(defn- faces-between-lines
  "Describe a surface between two sequences of points of the same length.
  Return triangles described with point coordinates.
  As the penultimate step, before calling tessellate, rearrange each group of
  four points into OpenSCAD’s clockwise order. This assumes that the two
  sequences of points are themselves roughly parallel."
  [point-seqs]
  {:pre [(spec/valid? (spec/coll-of ::tarmi/point-coll-3d :count 2) point-seqs)
         (apply = (map count point-seqs))
         (= (count point-seqs) 2)]
   :post [(spec/valid? (spec/coll-of ::tarmi/point-coll-3d) %)]}
  (->> point-seqs
    (apply interleave)
    (partition 4 2)
    (map (fn [[a b c d]] [a c d b]))
    (tessellate)))

(defn- fill-between-lines
  "Like faces-between-lines but connecting each line back to its starting
  point, forming a complete surface all the way between two loops."
  [point-seqs]
  (faces-between-lines (map bite-tail point-seqs)))


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
  [points triangles]
  {:pre [(spec/valid? ::tarmi/point-coll-2-3d points)
         (spec/valid? (spec/coll-of ::tarmi/point-coll-2-3d) triangles)]
   :post [(spec/valid? (spec/coll-of (spec/coll-of nat-int?)) %)]}
  (let [to-index (fn [coord] (index-of points coord))
        to-face (fn [triangle] (mapv to-index triangle))]
    (mapv to-face triangles)))

(defn from-triangle-coordinates
  "An OpenSCAD polyhedron. Unlike model/polyhedron, this function takes
  its triangles defined in terms of coordinates, not indices in the list of
  points."
  ([triangles]
   (from-triangle-coordinates (distinct (apply concat triangles)) triangles))
  ([points triangles]
   (from-triangle-coordinates points triangles 4))
  ([points triangles convexity]
   {:pre [(spec/valid? ::tarmi/point-coll-3d points)
          (spec/valid? (spec/coll-of ::tarmi/point-coll-3d) triangles)
          (nat-int? convexity)]}
   (model/polyhedron points
                     (coords-to-indices points triangles)
                     :convexity convexity)))

(defn from-face-coordinates
  "An OpenSCAD polyhedron tessellated by thi.ng."
  ([faces]
   (from-triangle-coordinates (tessellate faces)))
  ([points faces & more]
   (apply from-triangle-coordinates points (tessellate faces) more)))

(defn bevelled-cuboid
  [side-tuples]
  {:pre [(spec/valid?
           (spec/coll-of
             (spec/tuple ::tarmi/point-3d
                         (spec/coll-of (spec/coll-of ::tarmi/point-3d)
                                       :count 2)))
           side-tuples)]}
  (let [corner-pairs (mapv (fn [[top lower]] (mapv (partial into [top]) lower))
                           side-tuples)
        bevel-edges (vec (apply concat corner-pairs))]  ; 8 sequences.
    (from-triangle-coordinates
      (distinct (apply concat bevel-edges))
      ;; Find faces; some redundant tessellation here.
      (->> [;; Top rectangle.
            [(mapv first side-tuples)]  ; 1 sequence of 4 points.
            ;; The upper corners, one triangle on each side.
            (mapv (fn [[top [left right]]]
                    [(first right) top (first left)])
                  side-tuples)
            ;; The rectangular lower corners.
            (mapcat faces-between-lines
                    (map second side-tuples))  ; 4 pairs of coordinate sequences.
            ;; The upper bevels along the sides, and the sides themselves.
            (mapcat faces-between-lines
                    (partition 2 (last-first bevel-edges)))
            ;; The bottom octagon, reversed because it’s facing down.
            [(reverse (mapv last bevel-edges))]]
        (apply concat)
        (tessellate))
      1)))

(defn tuboid
  "A polyhedron describing an irregular ring- or tube-like shape.
  This is based on the naïve assumption that incoming point sequences are
  arranged clockwise as seen looking at the right-hand side (high x) of the
  object from the outside (higher x)."
  [& [outer-left inner-left outer-right inner-right :as point-seqs]]
  {:pre [(spec/valid? (spec/coll-of ::tarmi/point-coll-3d) point-seqs)
         (apply = (map count point-seqs))
         (= (count point-seqs) 4)]}
  (from-triangle-coordinates
    (apply concat point-seqs)
    (mapcat fill-between-lines
      [[inner-left outer-left]    ; Left-hand-side aperture.
       [outer-right inner-right]  ; Right-hand side aperture, hence flipped.
       [inner-right inner-left]      ; Interior.
       [outer-left outer-right]])))  ; Exterior, hence flipped.
