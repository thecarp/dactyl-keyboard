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
  (testing "Simple input."
    (is (= (poly/coords-to-indices [[1 0] [1 1]] [[[1 0] [1 1]]])
           [[0 1]]))))
