(ns dactyl-keyboard.schema-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.spec.alpha :as spec]
            [scad-tarmi.core :refer [π]]
            [dactyl-keyboard.param.schema :as schema]))


(deftest test-coordinate-parser
  (testing "single integer flexcoord"
    (is (= (schema/keyword-or-integer 1) 1)))
  (testing "single string flexcoord"
    (is (= (schema/keyword-or-integer "abc") :abc)))
  (testing "single nonsensical flexcoord"
    (is (thrown? java.lang.ClassCastException (schema/keyword-or-integer {}))))
  (testing "string pair"
    (is (= ((schema/tuple-of schema/keyword-or-integer) '("a" "b")) [:a :b]))))

(deftest test-parameter-spec
  (testing "empty"
    (is (= (spec/valid? ::schema/parameter-spec {}) true)))
  (testing "default only"
    (is (= (spec/valid? ::schema/parameter-spec {:default 1}) true)))
  (testing "non-reserved keyword"
    (is (= (spec/valid? ::schema/parameter-spec {:a 1}) false)))
  (testing "nested"
    (is (= (spec/valid? ::schema/parameter-spec {:k {:default 1}}) false))))

(deftest compass-compatible-angle-parser
  (testing "string"
    (is (= (schema/compass-compatible-angle "N") 0.0))
    (is (= (schema/compass-compatible-angle "NE") (/ π 4)))
    (is (= (schema/compass-compatible-angle "west") (* 1.5 π))))
  (testing "keyword"
    (is (= (schema/compass-compatible-angle :N) 0.0))
    (is (= (schema/compass-compatible-angle :NE) (/ π 4))))
  (testing "number"
    (is (= (schema/compass-compatible-angle 0) 0))
    (is (= (schema/compass-compatible-angle 0.1) 0.1))))

(deftest test-parse-anchored-2d-positions
  (testing "parsing anchored 2D positions"
    (is (= (schema/anchored-2d-positions [])
           []))
    (is (= (schema/anchored-2d-positions
             [{:anchor "a", :side "SSW", :offset [0 -1]}])
           [{:anchor :a, :side :SSW, :offset [0 -1]}]))))

(deftest test-coordinate-validator
  (testing "empty"
    (is (= (spec/valid? ::schema/key-coordinates [])
           false)))
  (testing "short"
    (is (= (spec/valid? ::schema/key-coordinates [1])
           false)))
  (testing "literal key coordinates"
    (is (= (spec/valid? ::schema/key-coordinates [1 1])
           true)))
  (testing "a mapping"
    (is (= (spec/valid? ::schema/key-coordinates {1 1})
           false)))
  (testing "a valid keyword"
    (is (= (spec/valid? ::schema/key-coordinates [1 :last])
           true)))
  (testing "an invalid keyword"
    (is (= (spec/valid? ::schema/key-coordinates [1 :soup])
           false))))
