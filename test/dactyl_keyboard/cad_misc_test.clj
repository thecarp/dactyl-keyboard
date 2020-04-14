(ns dactyl-keyboard.cad-misc-test
  (:require [clojure.test :refer [deftest testing is]]
            [dactyl-keyboard.cad.misc :refer [shallow-wrap
                                              cube-corner-xyz]]))

(let [sample (fn [index]
               (let [collection [:a :b :c]]
                 (get collection (shallow-wrap collection index))))]
  (deftest shallow-wrapping
    (testing "at bounds"
      (is (= (sample 0) :a))
      (is (= (sample 2) :c)))
    (testing "out of bounds"
      (is (= (sample -1) :c))
      (is (= (sample -2) :c))
      (is (= (sample 3) :a))
      (is (= (sample 4) :a)))))

(deftest cube-corners
  (testing "xyz centre"
    (is (= (cube-corner-xyz nil 1 [100 100 100] 0) [0.0 0.0 0.0]))
    (is (= (cube-corner-xyz nil 1 [100 100 100] 10) [0.0 0.0 0.0])))
  (testing "xyz without wall"
    (is (= (cube-corner-xyz :N 1 [100 100 100] 0) [0.0 50.0 0.0]))
    (is (= (cube-corner-xyz :NW 1 [100 100 100] 0) [-50.0 50.0 0.0]))
    (is (= (cube-corner-xyz :NW 0 [100 100 100] 0) [-50.0 50.0 50.0]))
    (is (= (cube-corner-xyz :NNW 2 [100 100 100] 0) [-50.0 50.0 -50.0])))
  (testing "xyz with wall"
    (is (= (cube-corner-xyz :N 1 [100 100 100] 10) [0.0 45.0 0.0]))
    (is (= (cube-corner-xyz :NW 1 [100 100 100] 10) [-45.0 45.0 0.0]))
    (is (= (cube-corner-xyz :NW 0 [100 100 100] 10) [-45.0 45.0 45.0]))
    (is (= (cube-corner-xyz :NNW 2 [100 100 100] 10) [-45.0 45.0 -45.0]))))
