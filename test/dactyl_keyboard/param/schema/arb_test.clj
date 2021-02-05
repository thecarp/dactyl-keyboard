(ns dactyl-keyboard.param.schema.arb-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.spec.alpha :as spec]
            [scad-tarmi.core :refer [π]]
            [dactyl-keyboard.param.schema.arb :as arb]))

(defn forest  ; Test fixture.
  "Parse and validate the given configuration for arbitrary shapes."
  [raw]
  (let [parsed (arb/parse-grove raw)]
    [(spec/valid? ::arb/map parsed), parsed]))

(defn node
  [raw]  ; Parse and validate single node.
  (let [[validity parsed] (forest {::trash [raw]})]
    [validity (-> parsed ::trash first)]))
(def neutral-leaf (second (node {})))
(defn facit [delta] (merge-with merge neutral-leaf delta))

(deftest test-test-fixtures
  (testing "no salient parts of a leaf"
    (is (= (facit {})
           {:anchoring {:anchor :origin
                        :side nil
                        :segment nil
                        :preserve-orientation false
                        :intrinsic-offset [0 0 0]
                        :extrinsic-offset [0 0 0]
                        :intrinsic-rotation [0 0 0]
                        :extrinsic-rotation [0 0 0]}})))
  (testing "some salient parts of a leaf"
    (is (= (facit {:anchoring {:side :N}})
           {:anchoring {:anchor :origin
                        :side :N
                        :segment nil
                        :preserve-orientation false
                        :intrinsic-offset [0 0 0]
                        :extrinsic-offset [0 0 0]
                        :intrinsic-rotation [0 0 0]
                        :extrinsic-rotation [0 0 0]}}))))

(deftest test-top-level
  (testing "nil forest"
    (is (= (forest nil)  ; Entire section nullified.
           [true {}])))
  (testing "empty forest"
    (is (= (forest {})
           [true {}])))
  (testing "nil grove"
    (is (= (forest {"a" nil})  ; Single grove (from upstream) nullified.
           [true {:a nil}])))
  (testing "empty grove"
    (is (= (forest {"a" []})
           [false {:a []}])))
  (testing "tree in place of grove list"
    (is (= (forest {"a" {:hull-around []}})
           [false {:a {:hull-around []}}])))
  (testing "leaf in place of grove list"
    (is (= (first (forest {"a" {:anchoring {:anchor :origin}}})) false)))
  (testing "leaves and trees at top level"
    (is (= (forest {"a" [{:size [2 1 1]}
                         {:hull-around [{:size [1 1 2]}]}]
                    "b" [{:hull-around [{:size [2 1 2]}]}
                         {:size [1 2 1]}]})
           [true
            {:a [(facit {:anchoring {:anchor :origin}, :size [2 1 1]})
                 {:hull-around [(facit {:anchoring {:anchor :origin}
                                        :size [1 1 2]})]}]
             :b [{:hull-around [(facit {:anchoring {:anchor :origin}
                                        :size [2 1 2]})]}
                 (facit {:anchoring {:anchor :origin} :size [1 2 1]})]}]))))

(deftest test-node-level
  (testing "nil branch"
    (is (= (first (node {:hull-around nil})) false)))
  (testing "empty branch"
    (is (= (node {:hull-around []})
           [false {:hull-around []}])))
  (testing "short-format leaf, anchor only"
    (is (= (node ["a0"])
           [true (facit {:anchoring {:anchor :a0}})]))
    (is (= (node ["a0", nil])
           [true (facit {:anchoring {:anchor :a0}})])))
  (testing "short-format leaf, side and segment"
    (is (= (node ["a1", "N", 4])
           [true (facit {:anchoring {:anchor :a1, :side :N, :segment 4}})])))
  (testing "short-format leaf, nil side with trailing segments"
    (is (= (node ["a2", nil, 1, 3])
           [true (facit {:anchoring {:anchor :a2, :segment 1} :sweep 3})])))
  (testing "short-format leaf, side, segment and sweep"
    (is (= (node ["a3", "SSW", 0, 2])
           [true (facit {:anchoring {:anchor :a3, :side :SSW, :segment 0}
                         :sweep 2})])))
  (testing "short-format low leaf, maximal"
    (is (= (node ["a4", "SW", 2, 3, {:anchoring {:preserve-orientation true
                                                 :intrinsic-offset [0 1 0]
                                                 :extrinsic-offset [1 0 1]
                                                 :intrinsic-rotation [1 1 0]
                                                 :extrinsic-rotation [0 1 1]}
                                     :size [1 2 4]}])
           [true (facit {:anchoring {:anchor :a4, :side :SW, :segment 2,
                                     :preserve-orientation true
                                     :intrinsic-offset [0 1 0]
                                     :extrinsic-offset [1 0 1]
                                     :intrinsic-rotation [1 1 0]
                                     :extrinsic-rotation [0 1 1]}
                         :sweep 3
                         :size [1 2 4]})])))
  (testing "short-format low leaf, map in first position"
    (let [[validity value] (node [{:anchoring {:intrinsic-offset [0 0 1]}}])]
      (is (= validity false))
      (is (= (first value) (facit {:anchoring {:anchor :origin, :intrinsic-offset [0 0 1]}})))))
  (testing "long-format low leaf, default anchor"
    (is (= (node {:anchoring {:extrinsic-offset [0 0 1]}})
           [true (facit {:anchoring {:anchor :origin, :extrinsic-offset [0 0 1]}})])))
  (testing "long-format high leaf, near maximal"
    (is (= (node {:cut true
                  :body "central-housing"
                  :at-ground true
                  :above-ground false
                  :anchoring {:anchor "x"
                              :side "S"
                              :segment 0
                              :intrinsic-offset [-1 -2 -1]
                              :extrinsic-offset [-3 -2 -1]
                              :intrinsic-rotation ["pi/-10" 0 0]
                              :extrinsic-rotation [0 "pi/10" 0]}
                  :sweep 1
                  :size [1 10 2]})
           [true {:cut true
                  :body :central-housing
                  :at-ground true
                  :above-ground false
                  :anchoring {:anchor :x
                              :side :S
                              :segment 0
                              :preserve-orientation false  ; Not in input.
                              :intrinsic-offset [-1 -2 -1]
                              :extrinsic-offset [-3 -2 -1]
                              :intrinsic-rotation [(/ π -10) 0 0]
                              :extrinsic-rotation [0 (/ π 10) 0]}
                  :sweep 1
                  :size [1 10 2]}]))))
