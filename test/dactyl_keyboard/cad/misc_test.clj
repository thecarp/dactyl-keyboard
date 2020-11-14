(ns dactyl-keyboard.cad.misc-test
  (:require [clojure.test :refer [deftest testing is]]
            [dactyl-keyboard.cad.misc
             :refer [shallow-wrap walled-corner-xyz bevelled-corner-xyz]]))

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

(deftest walled-corners
  (testing "xyz centre"
    (is (= (walled-corner-xyz nil 1 [100 100 100] 0) [0.0 0.0 0.0]))
    (is (= (walled-corner-xyz nil 1 [100 100 100] 10) [0.0 0.0 0.0])))
  (testing "xyz without wall"
    (is (= (walled-corner-xyz :N 1 [100 100 100] 0) [0.0 50.0 0.0]))
    (is (= (walled-corner-xyz :NW 1 [100 100 100] 0) [-50.0 50.0 0.0]))
    (is (= (walled-corner-xyz :NW 0 [100 100 100] 0) [-50.0 50.0 50.0]))
    (is (= (walled-corner-xyz :NNW 2 [100 100 100] 0) [-50.0 50.0 -50.0])))
  (testing "xyz with wall"
    (is (= (walled-corner-xyz :N 1 [100 100 100] 10) [0.0 45.0 0.0]))
    (is (= (walled-corner-xyz :NW 1 [100 100 100] 10) [-45.0 45.0 0.0]))
    (is (= (walled-corner-xyz :NW 0 [100 100 100] 10) [-45.0 45.0 45.0]))
    (is (= (walled-corner-xyz :NNW 2 [100 100 100] 10) [-45.0 45.0 -45.0]))))

(deftest bevelled-corners
  (testing "xyz centre"
    (is (= (bevelled-corner-xyz nil nil [100 100 100] 0) [0.0 0.0 0.0]))
    (is (= (bevelled-corner-xyz nil nil [100 100 100] 10) [0.0 0.0 0.0])))
  (testing "xyz without wall"
    (is (= (bevelled-corner-xyz :N 0 [100 100 100] 0) [0.0 50.0 50.0]))
    (is (= (bevelled-corner-xyz :N 1 [100 100 100] 0) [0.0 50.0 50.0]))
    (is (= (bevelled-corner-xyz :N 2 [100 100 100] 0) [0.0 50.0 0.0]))
    (is (= (bevelled-corner-xyz :N 3 [100 100 100] 0) [0.0 50.0 -50.0]))
    (is (= (bevelled-corner-xyz :NW 0 [100 100 100] 0) [-50.0 50.0 50.0]))
    (is (= (bevelled-corner-xyz :NW 1 [100 100 100] 0) [-50.0 50.0 50.0]))
    (is (= (bevelled-corner-xyz :NW 2 [100 100 100] 0) [-50.0 50.0 0.0]))
    (is (= (bevelled-corner-xyz :NW 3 [100 100 100] 0) [-50.0 50.0 -50.0]))
    (is (= (bevelled-corner-xyz :NNW 0 [100 100 100] 0) [-50.0 50.0 50.0]))
    (is (= (bevelled-corner-xyz :NNW 1 [100 100 100] 0) [-50.0 50.0 50.0]))
    (is (= (bevelled-corner-xyz :NNW 2 [100 100 100] 0) [-50.0 50.0 0.0]))
    (is (= (bevelled-corner-xyz :NNW 3 [100 100 100] 0) [-50.0 50.0 -50.0]))
    (is (= (bevelled-corner-xyz :WNW 0 [100 100 100] 0) [-50.0 50.0 50.0]))
    (is (= (bevelled-corner-xyz :WNW 1 [100 100 100] 0) [-50.0 50.0 50.0]))
    (is (= (bevelled-corner-xyz :WNW 2 [100 100 100] 0) [-50.0 50.0 0.0]))
    (is (= (bevelled-corner-xyz :WNW 3 [100 100 100] 0) [-50.0 50.0 -50.0])))
  (testing "xyz with wall"
    (is (= (bevelled-corner-xyz :N 0 [100 100 100] 10) [0.0 40.0 50.0]))
    (is (= (bevelled-corner-xyz :N 1 [100 100 100] 10) [0.0 50.0 40.0]))
    (is (= (bevelled-corner-xyz :N 2 [100 100 100] 10) [0.0 50.0 0.0]))
    (is (= (bevelled-corner-xyz :N 3 [100 100 100] 10) [0.0 50.0 -50.0]))
    (is (= (bevelled-corner-xyz :NW 0 [100 100 100] 10) [-40.0 40.0 50.0]))
    (is (= (bevelled-corner-xyz :NW 1 [100 100 100] 10) [-50.0 50.0 40.0]))
    (is (= (bevelled-corner-xyz :NW 2 [100 100 100] 10) [-50.0 50.0 0.0]))
    (is (= (bevelled-corner-xyz :NW 3 [100 100 100] 10) [-50.0 50.0 -50.0]))
    (is (= (bevelled-corner-xyz :NNW 0 [100 100 100] 10) [-40.0 40.0 50.0]))
    (is (= (bevelled-corner-xyz :NNW 1 [100 100 100] 10) [-40.0 50.0 40.0]))
    (is (= (bevelled-corner-xyz :NNW 2 [100 100 100] 10) [-40.0 50.0 0.0]))
    (is (= (bevelled-corner-xyz :NNW 3 [100 100 100] 10) [-40.0 50.0 -50.0]))
    (is (= (bevelled-corner-xyz :WNW 0 [100 100 100] 10) [-40.0 40.0 50.0]))
    (is (= (bevelled-corner-xyz :WNW 1 [100 100 100] 10) [-50.0 40.0 40.0]))
    (is (= (bevelled-corner-xyz :WNW 2 [100 100 100] 10) [-50.0 40.0 0.0]))
    (is (= (bevelled-corner-xyz :WNW 3 [100 100 100] 10) [-50.0 40.0 -50.0]))))
