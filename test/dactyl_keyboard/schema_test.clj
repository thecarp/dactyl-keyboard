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

(deftest test-tweaks
  (let [forest (fn [raw]  ; Parse and validate tweak setting.
                 (let [parsed (schema/tweak-grove raw)]
                   [(spec/valid? ::schema/tweak-name-map parsed)
                    parsed]))
        node (fn [raw]  ; Parse and validate single node.
               (let [[validity parsed] (forest {::trash [raw]})]
                 [validity (-> parsed ::trash first)]))]
    (testing "empty forest"
      (is (= (forest {})
             [true {}])))
    (testing "empty grove"
      (is (= (forest {"a" []})
             [false {:a []}])))
    (testing "tree in place of grove list"
      (is (= (forest {"a" {:hull-around []}})
             [false {:a {:hull-around []}}])))
    (testing "leaf in place of grove list"
      (is (= (forest {"a" {:anchoring {:anchor :origin}}})
             [false {:a {:anchoring {:anchor :origin}}}])))
    (testing "leaves and trees at top level"
      (is (= (forest {"a" [{:size [2 1 1]}
                           {:hull-around [{:size [1 1 2]}]}]
                      "b" [{:hull-around [{:size [2 1 2]}]}
                           {:size [1 2 1]}]})
             [true
              {:a [{:anchoring {:anchor :origin} :size [2 1 1]}
                   {:hull-around [{:anchoring {:anchor :origin}
                                   :size [1 1 2]}]}]
               :b [{:hull-around [{:anchoring {:anchor :origin}
                                   :size [2 1 2]}]}
                   {:anchoring {:anchor :origin} :size [1 2 1]}]}])))
    (testing "empty branch"
      (is (= (node {:hull-around []})
             [false {:hull-around []}])))
    (testing "short-format leaf, anchor only"
      (is (= (node ["a0"])
             [true {:anchoring {:anchor :a0}}]))
      (is (= (node ["a0", nil])
             [true {:anchoring {:anchor :a0}}])))
    (testing "short-format leaf, side and segment"
      (is (= (node ["a1", "N", 4])
             [true {:anchoring {:anchor :a1, :side :N, :segment 4}}])))
    (testing "short-format leaf, nil side with trailing segments"
      (is (= (node ["a2", nil, 1, 3])
             [true {:anchoring {:anchor :a2, :segment 1} :sweep 3}])))
    (testing "short-format leaf, side, segment and sweep"
      (is (= (node ["a3", "SSW", 0, 2])
             [true {:anchoring {:anchor :a3, :side :SSW, :segment 0}
                    :sweep 2}])))
    (testing "short-format low leaf, maximal"
      (is (= (node ["a4", "SW", 2, 3, {:anchoring {:offset [0 1 0]}
                                       :size [1 2 4]}])
             [true {:anchoring {:anchor :a4, :side :SW,
                                :segment 2, :offset [0 1 0]}
                    :sweep 3
                    :size [1 2 4]}])))
    (testing "short-format low leaf, map in first position"
      (is (= (node [{:anchoring {:offset [0 0 1]}}])
             [false '({:anchoring {:anchor :origin, :offset [0 0 1]}})])))
    (testing "long-format low leaf, default anchor"
      (is (= (node {:anchoring {:offset [0 0 1]}})
             [true {:anchoring {:anchor :origin, :offset [0 0 1]}}])))
    (testing "long-format high leaf, maximal"
      (is (= (node {:positive false
                    :body "central-housing"
                    :at-ground true
                    :above-ground false
                    :anchoring {:anchor "x"
                                :side "S"
                                :segment 0
                                :offset [-3 -2 -1]}
                    :sweep 1
                    :size [1 10 2]})
             [true {:positive false
                    :body :central-housing
                    :at-ground true
                    :above-ground false
                    :anchoring {:anchor :x
                                :side :S
                                :segment 0
                                :offset [-3 -2 -1]}
                    :sweep 1
                    :size [1 10 2]}])))))
