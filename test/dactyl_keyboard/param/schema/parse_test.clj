(ns dactyl-keyboard.param.schema.parse-test
  (:require [clojure.test :refer [deftest testing is]]
            [scad-tarmi.core :refer [π]]
            [dactyl-keyboard.param.schema.parse :as parse]))


(deftest test-coordinate-parser
  (testing "single integer flexcoord"
    (is (= (parse/keyword-or-integer 1) 1)))
  (testing "single string flexcoord"
    (is (= (parse/keyword-or-integer "abc") :abc)))
  (testing "single nonsensical flexcoord"
    (is (thrown? java.lang.ClassCastException (parse/keyword-or-integer {}))))
  (testing "string pair"
    (is (= ((parse/tuple-of parse/keyword-or-integer) '("a" "b")) [:a :b]))))

(deftest compass-incompatible-angle-parser
  (testing "real number"
    (is (= (parse/compass-incompatible-angle 0.1) 0.1)))
  (testing "integer"
    (is (= (parse/compass-incompatible-angle 1) 1))
    (is (= (parse/compass-incompatible-angle -1) -1)))
  (testing "good string"
    (is (= (parse/compass-incompatible-angle "π") π))
    (is (= (parse/compass-incompatible-angle "π*1") π))
    (is (= (parse/compass-incompatible-angle "π/1") π))
    (is (= (parse/compass-incompatible-angle "pi/2") (/ π 2)))
    (is (= (parse/compass-incompatible-angle "π/ 2") (/ π 2)))
    (is (= (parse/compass-incompatible-angle "π /2.1")
           (/ π (Float/parseFloat "2.1"))))  ;; Java parser on different precision.
    (is (= (parse/compass-incompatible-angle "PI * -19.55")
           (* π (Float/parseFloat "-19.55"))))
    (is (= (parse/compass-incompatible-angle "π* 0.0") 0.0)))
  (testing "bad string"
    (doseq [s ["pie" "p" "τ" "Π" "1π" "2π" "2 * π" "π " "π 1" "π ** 1" "π  *1"
               "π * 1 " "= π * 2" "$pi" "{pi}" "[pi]" "N" "NE"]]
      (is (thrown? java.lang.ClassCastException
            (parse/compass-incompatible-angle s)))))
  (testing "nil"
    (is (thrown? java.lang.AssertionError
          (parse/compass-incompatible-angle nil)))))

(deftest compass-compatible-angle-parser
  (testing "string"
    (is (= (parse/compass-compatible-angle "N") 0.0))
    (is (= (parse/compass-compatible-angle "NE") (/ π -4)))
    (is (= (parse/compass-compatible-angle "west") (* -1.5 π)))
    (is (= (parse/compass-compatible-angle "π * 3") (* 3 π))))
  (testing "keyword"
    (is (= (parse/compass-compatible-angle :N) 0.0))
    (is (= (parse/compass-compatible-angle :NE) (/ π -4))))
  (testing "number"
    (is (= (parse/compass-compatible-angle 0) 0))
    (is (= (parse/compass-compatible-angle 0.1) 0.1)))
  (testing "nil"
    (is (thrown? java.lang.AssertionError
          (parse/compass-compatible-angle nil)))))
