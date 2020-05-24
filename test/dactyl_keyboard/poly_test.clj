;;; Unit tests for the poly module.
;;; This is mostly notes on how thi.ng.geom works.

(ns dactyl-keyboard.poly-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as string]
            [dactyl-keyboard.cad.poly :as poly]
            [thi.ng.geom.vector :refer [vec2 vec3]]
            [scad-clj.model :as model]
            [scad-clj.scad :refer [write-scad]]))

(defn- scad-scene
  "Produce copy-paste friendly OpenSCAD code.
  The scene subtracts a cylinder from an arbitrary shape at
  a scale appropriate for the default view. Difference is used
  because OpenSCAD traditionally fails to detect mesh problems until such
  operations are applied to broken polyhedra."
  [shape]
  (string/replace
    (write-scad (model/difference shape (model/cylinder 25 100)))
    "\n" ""))

(defn- vec3-seq [& points] (mapv (partial apply vec3) points))

(deftest test-upstream
  (testing "thi.ng.geom spec."
    (is (= (vec2 [0 0]) [0.0 0.0]))
    (is (= (mapv vec2 [[0 0] [1 1]]) [[0.0 0.0] [1.0 1.0]]))))

(deftest test-tesselate
  (testing "Ready-made triangle."
    (is (= (poly/tessellate [[[0 0 0] [0.5 0.5 0] [1 0 0]]])
           [[[0.0 0.0 0.0] [0.5 0.5 0.0] [1.0 0.0 0.0]]])))
  (testing "Unit square."
    (is (= (poly/tessellate [[[0 0 0] [0 1 0] [1 1 0] [1 0 0]]])
           [[[0.0 0.0 0.0] [0.0 1.0 0.0] [1.0 1.0 0.0]]
            [[0.0 0.0 0.0] [1.0 1.0 0.0] [1.0 0.0 0.0]]])))
  (testing "Irregular tetrahedron."
    (is (= (poly/tessellate
             [[[1 2 0] [2 0 0] [1 1 1]]  ; North-east face.
              [[0 0 0] [1 1 1] [2 0 0]]  ; South face.
              [[1 2 0] [1 1 1] [0 0 0]]  ; North-west face.
              [[0 0 0] [2 0 0] [1 2 0]]])  ;  Bottom face.
           [[[0.0 0.0 0.0] [1.0 1.0 1.0] [2.0 0.0 0.0]]
            [[0.0 0.0 0.0] [2.0 0.0 0.0] [1.0 2.0 0.0]]
            [[1.0 2.0 0.0] [1.0 1.0 1.0] [0.0 0.0 0.0]]
            [[1.0 2.0 0.0] [2.0 0.0 0.0] [1.0 1.0 1.0]]])))
  (testing "Pyramid."
    (let [[ne se sw nw top] (vec3-seq [2 2 0] [2 0 0] [0 0 0] [0 2 0] [1 1 1])]
      (is (= (poly/tessellate [[se top ne]      ; East face.
                               [sw top se]      ; South face.
                               [nw top sw]      ; West face.
                               [ne top nw]      ; North face.
                               [ne nw sw se]])  ; Bottom.
             [[sw top se]  ; South face sorts first, starting with [0 0 0].
              [nw top sw]  ; West face.
              [se top ne]  ; East face.
              [ne sw se]  ; Bottom, part one.
              [ne nw sw]  ; Bottom, part two.
              [ne top nw]])))))  ; North face.

(deftest test-from-face-coordinates
  (testing "Pyramid minus cylinder."
    ;; This is very similar to the tessellation test for a pyramid,
    ;; but produces an OpenSCAD expression for manually checking rendering.
    ;; The scale is therefore larger.
    (let [[ne se sw nw top] (vec3-seq [40 40 0] [40 0 0] [0 0 0] [0 40 0]
                                      [20 20 20])]
      (is (= (scad-scene (poly/from-face-coordinates
                           [[se top ne] [sw top se] [nw top sw] [ne top nw]
                            [ne nw sw se]]))  ; Bottom.
             "difference () {  polyhedron (points=[[0.0, 0.0, 0.0], [20.0, 20.0, 20.0], [40.0, 0.0, 0.0], [0.0, 40.0, 0.0], [40.0, 40.0, 0.0]], faces=[[0, 1, 2], [3, 1, 0], [2, 1, 4], [4, 0, 2], [4, 3, 0], [4, 1, 3]], convexity=4);  cylinder (h=100, r=25, center=true);}"))))
  (testing "Distorted cuboid minus cylinder."
    (let [[t0 t1 t2 t3] (vec3-seq [40 40 40] [40 0 30] [-5 0 40] [0 40 40])
          [b0 b1 b2 b3] (vec3-seq [40 40 0]  [10 0 0]  [0 0 0]   [0 40 0])]
      (is (= (scad-scene (poly/from-face-coordinates
                           [[t0 t1 t2 t3]  ; Top.
                            [b3 b2 b1 b0]  ; Bottom.
                            [t0 b0 b1 t1]
                            [t1 b1 b2 t2]
                            [t2 b2 b3 t3]
                            [t3 b3 b0 t0]]))
             "difference () {  polyhedron (points=[[-5.0, 0.0, 40.0], [0.0, 0.0, 0.0], [0.0, 40.0, 0.0], [0.0, 40.0, 40.0], [10.0, 0.0, 0.0], [40.0, 40.0, 0.0], [40.0, 40.0, 40.0], [40.0, 0.0, 30.0]], faces=[[0, 1, 2], [0, 2, 3], [2, 1, 4], [2, 4, 5], [3, 2, 5], [3, 5, 6], [7, 1, 0], [7, 4, 1], [6, 0, 3], [6, 4, 7], [6, 7, 0], [6, 5, 4]], convexity=4);  cylinder (h=100, r=25, center=true);}")))))

(deftest test-spline
  (testing "1D at resolution 0."
    ;; thi.ng allows resolution 0 and will return nil for it.
    ;; This is not sane for SCAD and not permitted by the poly module.
    (is (thrown? java.lang.AssertionError
          (poly/spline [[0 0] [1 0]] 0))))
  (testing "1D at resolution 1."
    (is (= (poly/spline [[0 0] [1 0]] 1)
           [[0.0 0.0] [1.0 0.0]])))
  (testing "1D at resolution 2."
    (is (= (poly/spline [[0 0] [1 0]] 2)
           [[0.0 0.0] [0.5 0.0] [1.0 0.0] [0.5 0.0]])))
  (testing "2D at resolution 2."  ; A loop around a unit triangle.
    (is (= (poly/spline [[0 0] [1 0] [1 1]] 2)
           [[0.0 0.0]     ; Input.
            [0.375 -0.1]  ; Interpolated.
            [1.0 0.0]     ; Input.
            [1.25 0.625]
            [1.0 1.0]
            [0.375 0.475]]))))

(deftest test-from-outline
  (testing "0D at inset 0."
    (is (= (poly/from-outline [] 0)
           [])))
  (testing "0D at inset 0."
    (is (= (poly/from-outline [[0 0]] 0)
           [[0.0 0.0]])))
  (testing "1D at inset 0."
    (is (= (poly/from-outline [[0 0] [1 0]] 0)
           [[0.0 0.0] [1.0 0.0]])))
  (testing "1D at inset 1 (illegal)."
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Unable to inset"
          (poly/from-outline [[0 0] [1 0]] 1))))
  (testing "2D at inset 1."
    (is (= (poly/from-outline [[0 0] [0 5] [5 5] [5 0]] 1)
           [[1.0 1.0] [1.0 4.0] [4.0 4.0] [4.0 1.0]]))))

(deftest test-coords-to-indices
  (testing "Minimal valid input."
    (is (= (poly/coords-to-indices [] [])
           [])))
  (testing "Input with unexpected point."
    (is (thrown? java.lang.AssertionError
          (poly/coords-to-indices [[1 0] [1 2]] [[[0 0] [1 2]]]))))
  (testing "Simple 2D input."
    (is (= (poly/coords-to-indices [[2 3] [3 3] [3 5]]     ; Points.
                                   [[[2 3] [3 3] [3 5]]])  ; One triangle.
           [[0 1 2]])))  ; Inputs match up.
  (testing "Simple 3D input."
    (is (= (poly/coords-to-indices [[4 5 6] [8 5 6] [7 7 7]]
                                   [[[8 5 6] [4 5 6] [7 7 7]]])
           [[1 0 2]]))))  ; First point in input appears second in triangle.

(deftest test-bevelled-cuboid
  (testing "Single-tier octagon."
    (let [ne [[2 2 1] [[[2 3 0]] [[3 2 0]]]]
          se [[2 1 1] [[[3 1 0]] [[2 1 0]]]]
          sw [[1 1 1] [[[1 0 0]] [[0 1 0]]]]
          nw [[1 2 1] [[[0 2 0]] [[1 3 0]]]]
          cuboid (poly/bevelled-cuboid [ne se sw nw])
          {:keys [points faces]} (second cuboid)]
      (is (= (first cuboid) :polyhedron))
      (is (= points [[2 2 1] [2 3 0] [3 2 0]
                     [2 1 1] [3 1 0] [2 1 0]
                     [1 1 1] [1 0 0] [0 1 0]
                     [1 2 1] [0 2 0] [1 3 0]]))
      (is (= faces [[8 6 7] [6 8 10] [6 10 9] [9 11 1] [9 1 0] [11 8 7]
                    [11 10 8] [11 7 5] [11 9 10] [11 5 4] [11 4 2] [11 2 1]
                    [5 3 4] [3 7 6] [3 5 7] [0 6 9] [0 3 6] [0 4 3] [0 2 4]
                    [2 0 1]])))))


(deftest test-tuboid
  (testing "Square profile."
    (let [left-outer  [[0 0 0] [0 5 0] [0 5 5] [0 0 5]]
          left-inner  [[0 1 1] [0 4 1] [0 4 4] [0 1 4]]
          right-outer [[1 0 0] [1 5 0] [1 5 5] [1 0 5]]
          right-inner [[1 1 1] [1 4 1] [1 4 4] [1 1 4]]
          tuboid (poly/tuboid left-outer left-inner right-outer right-inner)
          {:keys [points faces]} (second tuboid)]
      (is (= (first tuboid) :polyhedron))
      (is (= points [[0 0 0] [0 5 0] [0 5 5] [0 0 5] [0 1 1] [0 4 1] [0 4 4]
                     [0 1 4] [1 0 0] [1 5 0] [1 5 5] [1 0 5] [1 1 1] [1 4 1]
                     [1 4 4] [1 1 4]]))
      (is (= faces [[4 5 1] [4 1 0] [7 0 3] [7 4 0] [5 6 2] [5 2 1] [6 3 2]
                    [6 7 3] [8 13 12] [8 9 13] [11 8 12] [11 12 15] [9 14 13]
                    [9 10 14] [10 11 15] [10 15 14] [12 5 4] [12 13 5]
                    [15 4 7] [15 12 4] [13 6 5] [13 14 6] [14 7 6] [14 15 7]
                    [0 1 9] [0 9 8] [3 0 8] [3 8 11] [1 2 10] [1 10 9]
                    [2 3 11] [2 11 10]])))))
