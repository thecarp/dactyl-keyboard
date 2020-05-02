;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Polygon/Polyhedron Functions                                        ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; These are general functions for placing vertices rather than building up
;;; complex shapes from primitive solids.

(ns dactyl-keyboard.cad.poly
  (:require [scad-clj.model :as model]
            [clojure.spec.alpha :as spec]
            [thi.ng.geom.bezier :refer [auto-spline2]]
            [thi.ng.geom.core :refer [tessellate vertices bounds]]
            [thi.ng.geom.polygon :refer [polygon2 inset-polygon]]
            [thi.ng.geom.vector :refer [vec2]]
            [scad-tarmi.core :as tarmi :refer [π]]))


;;;;;;;;;;;;;;;
;; Internals ;;
;;;;;;;;;;;;;;;

(defn- tessellate3
  "Naïve 3D tesselation: Return 2 triangles for 4 3D points."
  [points]
  {:pre [(spec/valid? ::tarmi/point-coll-3d points)
         (= (count points) 4)]
   :post [(spec/valid? (spec/coll-of ::tarmi/point-coll-3d) %)
          (= (count %) 2)]}
  [(butlast points) (reverse (rest points))])

(defn- bite-tail [coll] (conj coll (first coll)))

(defn- pave-space
  "Describe a surface between two sequences of points of the same length.
  Return triangles described with point coordinates."
  [[a b]]
  {:pre [(spec/valid? ::tarmi/point-coll-3d a)
         (spec/valid? ::tarmi/point-coll-3d b)
         (= (count a) (count b))]
   :post [(spec/valid? (spec/coll-of ::tarmi/point-coll-3d) %)]}
  (mapcat tessellate3 (partition 4 2 (interleave (bite-tail a) (bite-tail b)))))

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
  (butlast (vertices (auto-spline2 (mapv vec2 points) true) resolution)))

(defn coords-to-indices
  "Take point coordinates and triangles referring to the same
  points by their coordinates. Return point-index triangles.
  This is intended to prepare a list of faces for an OpenSCAD polyhedron."
  ;; Notice that negative indices, as returned by .indexOf for unrecognized
  ;; inputs, are checked as illegal here, meaning that the inputs must match.
  [points triangles]
  {:pre [(spec/valid? ::tarmi/point-coll-2-3d points)
         (spec/valid? (spec/coll-of ::tarmi/point-coll-2-3d) triangles)]
   :post [(spec/valid? (spec/coll-of (spec/coll-of nat-int?)) %)]}
  (letfn [(to-index [coord] (.indexOf points coord))
          (to-face [triangle] (mapv to-index triangle))]
    (mapv to-face triangles)))

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
