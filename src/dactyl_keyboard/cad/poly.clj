;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Polygon/Polyhedron Functions                                        ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; These are general functions for placing vertices rather than building up
;;; complex shapes from primitive solids.

(ns dactyl-keyboard.cad.poly
  (:require [clojure.spec.alpha :as spec]
            [thi.ng.geom.bezier :refer [auto-spline2]]
            [thi.ng.geom.core :refer [tessellate vertices bounds]]
            [thi.ng.geom.polygon :refer [polygon2 inset-polygon]]
            [thi.ng.geom.vector :refer [vec2]]
            [scad-tarmi.core :as tarmi]))


;;;;;;;;;;;;;;;;;;;;;;;;;
;; Interface Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

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
         (integer? resolution)
         (> resolution 0)]
   :post [(spec/valid? ::tarmi/point-coll-2d %)]}
  (butlast (vertices (auto-spline2 (mapv vec2 points) true) resolution)))
