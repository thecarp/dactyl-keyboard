(ns dactyl-keyboard.cad.tweak-test
  (:require [clojure.test :refer [deftest testing is]]
            [dactyl-keyboard.fixture :refer [unit-testing-accessor]]
            [dactyl-keyboard.param.proc.anch :as anch]
            [dactyl-keyboard.cad.tweak :refer [screener]]))

(def getopt
  "Fake the data structure normally generated by refining a configuration."
  (unit-testing-accessor
   {:main-body {:reflect false}  ; Disable central housing, part 1.
    :central-housing {:include :false}  ; Disable central housing, part 2.
    :derived {:anchors {:origin {::anch/type ::anch/origin}
                        :c1 {::anch/type ::anch/central-gabel}}}}))

(def leaf-a
  "A leaf anchored to the origin which, in the absence of a central housing,
  is part of the main body."
  {:above-ground false
   :anchoring {:anchor :origin}})

(def leaf-b
  "A leaf anchored a little more directly to the central housing."
  {:anchoring {:anchor :c1}})

(def branch-a
  {:above-ground true  ; Normally implicit.
   :body :central-housing
   :hull-around [leaf-a]})

(def branch-b
  {:cut true
   :at-ground false  ; Normally implicit.
   :hull-around [leaf-a]})

(def branch-c
  {:at-ground true
   :hull-around [leaf-b]})

(def forest
  [leaf-a
   branch-a
   branch-b
   branch-c])

(deftest screening
  (testing "no criteria"
    (is (thrown? java.lang.AssertionError
          (screener getopt {}))))
  (testing "positive"
    (is (= (filter (screener getopt {:cut false}) forest)
           [leaf-a branch-a branch-c])))
  (testing "negative"
    (is (= (filter (screener getopt {:cut true}) forest)
           [branch-b])))
  (testing "at ground"
    (is (= (filter (screener getopt {:at-ground true}) forest)
           [branch-c])))
  (testing "not at ground"
    (is (= (filter (screener getopt {:at-ground false}) forest)
           [leaf-a branch-a branch-b])))
  (testing "above ground"
    (is (= (filter (screener getopt {:above-ground true}) forest)
           [branch-a branch-b branch-c])))
  (testing "not above ground"
    (is (= (filter (screener getopt {:above-ground false}) forest)
           [leaf-a])))
  (testing "main body"
    (is (= (filter (screener getopt {:bodies #{:main}}) forest)
           [leaf-a branch-b])))
  (testing "central housing"
    (is (= (filter (screener getopt {:bodies #{:central-housing}}) forest)
           [branch-a branch-c])))
  (testing "intersection"
    (is (= (filter (screener getopt {:above-ground true
                                     :bodies #{:main}}) forest)
           [branch-b]))))
