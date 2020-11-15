;;; Unit tests for the compass module.

(ns dactyl-keyboard.compass-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.core.matrix :refer [mmul]]
            [scad-tarmi.core :refer [π]]
            [dactyl-keyboard.compass :as compass]))

(deftest maps
  (testing "short-to-long samples"
    (is (= (:N compass/short-to-long) :north))
    (is (= (:NNE compass/short-to-long) :north))
    (is (= (:NE compass/short-to-long) :north))
    (is (= (:ENE compass/short-to-long) :east))))

(deftest keyword-to-radians
  (testing "radian samples"
    (is (zero? (compass/radians :N)))
    (is (zero? (compass/radians :north)))
    (is (zero? (:N compass/radians)))
    (is (= (:S compass/radians) (- π)))))

(deftest keyword-to-matrix
  (testing "rotation by matrix"
    (let [m* (fn [side] (mmul (side compass/matrices) [3 50]))]
      (is (= (m* :N) [3.0 50.0]))
      (is (= (m* :E) [50.0 -3.0]))
      (is (= (m* :S) [-3.0 -50.0]))
      (is (= (m* :W) [-50.0 3.0])))))

(deftest modulus
  (testing "north quadrant"
    (is (= (compass/northern-modulus :N) :N))
    (is (= (compass/northern-modulus :NNW) :NNW))
    (is (= (compass/northern-modulus :NNE) :NNE)))
  (testing "intercardinal corner case"
    (is (= (compass/northern-modulus :NE) :NE)))
  (testing "east quadrant"
    (is (= (compass/northern-modulus :ENE) :NNW))
    (is (= (compass/northern-modulus :E) :N))
    (is (= (compass/northern-modulus :ESE) :NNE))
    (is (= (compass/northern-modulus :SE) :NE)))
  (testing "west quadrant"
    (is (= (compass/northern-modulus :WSW) :NNW))
    (is (= (compass/northern-modulus :W) :N))
    (is (= (compass/northern-modulus :WNW) :NNE))
    (is (= (compass/northern-modulus :NW) :NE))))

(deftest turning
  (testing "reversal"
    (is (= (compass/reverse :N) :S))
    (is (= (compass/reverse :E) :W))
    (is (= (compass/reverse :S) :N))
    (is (= (compass/reverse :NE) :SW))
    (is (= (compass/reverse :SE) :NW))
    (is (= (compass/reverse :SSW) :NNE)))
  (testing "right turn"
    (is (= (compass/sharp-right :N) :E))
    (is (= (compass/sharp-right :NE) :SE))
    (is (= (compass/sharp-right :NNE) :ESE)))
  (testing "left turn"
    (is (= (compass/sharp-left :N) :W))
    (is (= (compass/sharp-left :NE) :NW))
    (is (= (compass/sharp-left :NNE) :WNW))))

(deftest to-grid
  (testing "cardinal"
    (is (= (compass/to-grid :N) [0 1]))
    (is (= (compass/to-grid :N true) [0 1]))
    (is (= (compass/to-grid :N true 0) 0)))
  (testing "intercardinal"
    (is (= (compass/to-grid :SW) [-1 -1]))
    (is (= (compass/to-grid :SW true) [-1 -1]))
    (is (= (compass/to-grid :SW true 1) -1)))
  (testing "intermediate"
    (is (= (compass/to-grid :NNE) [1 1]))
    (is (= (compass/to-grid :ENE) [1 1]))
    (is (= (compass/to-grid :NNE true) [0 1]))
    (is (= (compass/to-grid :ENE true) [1 0]))
    (is (= (compass/to-grid :ENE true 1) 0))))

(deftest variary-delta
  (testing "nullary delta"
    (is (= (compass/delta-x) 0))
    (is (= (compass/delta-y) 0)))
  (testing "unary delta"
    (is (= (compass/delta-x :N) 0))
    (is (= (compass/delta-y :N) 1)))
  (testing "binary delta"
    (is (= (compass/delta-x :N :E) 1))
    (is (= (compass/delta-y :N :E) 1)))
  (testing "trinary delta"  ; Not a use case at time of design.
    (is (= (compass/delta-x :N :E :E) 1))
    (is (= (compass/delta-y :N :E :E) 1))))
