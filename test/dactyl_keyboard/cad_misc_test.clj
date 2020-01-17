(ns dactyl-keyboard.cad-misc-test
  (:require [clojure.test :refer [deftest testing is]]
            [dactyl-keyboard.cad.misc :refer [shallow-wrap]]))

(defn sample
  [index]
  (let [collection [:a :b :c]]
    (get collection (shallow-wrap collection index))))

(deftest shallow-wrapping
  (testing "at bounds"
    (is (= (sample 0) :a))
    (is (= (sample 2) :c)))
  (testing "out of bounds"
    (is (= (sample -1) :c))
    (is (= (sample -2) :c))
    (is (= (sample 3) :a))
    (is (= (sample 4) :a))))

