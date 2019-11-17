;;; Unit tests for the poly module.
;;; This is mostly notes on how thi.ng.geom works.

(ns dactyl-keyboard.poly-test
  (:require [clojure.test :refer [deftest testing is]]
            [dactyl-keyboard.cad.poly :as poly]
            [thi.ng.geom.vector :refer [vec2]]))

(deftest test-upstream
  (testing "thi.ng.geom spec."
    (is (= (vec2 [0 0]) [0.0 0.0]))
    (is (= (mapv vec2 [[0 0] [1 1]]) [[0.0 0.0] [1.0 1.0]]))))

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

(deftest test-tuboid
  (testing "Minimal valid input."
    (is (= (poly/tuboid [] [] [] [])
           '(:polyhedron {:points (), :faces [], :convexity 3}))))
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
      (is (= faces [[0 4 1] [5 1 4] [1 5 2] [6 2 5] [2 6 3] [7 3 6] [3 7 0]
                    [4 0 7] [12 8 13] [9 13 8] [13 9 14] [10 14 9] [14 10 15]
                    [11 15 10] [15 11 12] [8 12 11] [4 12 5] [13 5 12] [5 13 6]
                    [14 6 13] [6 14 7] [15 7 14] [7 15 4] [12 4 15] [8 0 9]
                    [1 9 0] [9 1 10] [2 10 1] [10 2 11] [3 11 2] [11 3 8]
                    [0 8 3]])))))
