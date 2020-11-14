(ns dactyl-keyboard.misc-test
  (:require [clojure.test :refer [deftest testing is]]
            [flatland.ordered.map :refer [ordered-map]]
            [dactyl-keyboard.misc :refer [soft-merge]]))

(deftest test-soft-merge-disparate
  (testing "list in place of a dictionary"
    (is (thrown? java.lang.ClassCastException
          (soft-merge {:a 1} [:a 2]))))
  (testing "dictionary in place of a list"
    (is (thrown? java.lang.IllegalArgumentException
          (soft-merge [:a 1] {:a 2})))))

(deftest test-soft-merge-unordered-maps
  (testing "1 deep, leaf replacement."
    (is (= (soft-merge {:a 1} {:a 2}) {:a 2})))
  (testing "2 deep, leaf replacement."
    (is (= (soft-merge {:a {:b 1}} {:a {:b 2}}) {:a {:b 2}})))
  (testing "2 deep, addition."
    (is (= (soft-merge {:a {:b 1}} {:a {:b 2 :c 3}}) {:a {:b 2 :c 3}})))
  (testing "2 deep, conservation."
    (is (= (soft-merge {:a {:b 1 :c 3}} {:a {:b 2}}) {:a {:b 2 :c 3}}))))

(deftest test-soft-merge-ordered-maps
  (let [om ordered-map]
    (testing "1 deep, leaf replacement."
      (is (= (soft-merge (om :a 1) (om :a 2)) (om :a 2))))
    (testing "2 deep, leaf replacement."
      (is (= (soft-merge (om :a (om :b 1)) (om :a (om :b 2)))
             (om :a (om :b 2)))))
    (testing "2 deep, addition."
      (is (= (soft-merge (om :a (om :b 1)) (om :a (om :b 2 :c 3)))
             (om :a (om :b 2 :c 3)))))
    (testing "2 deep, conservation."
      (is (= (soft-merge (om :a (om :b 1 :c 3)) (om :a (om :b 2)))
             (om :a (om :b 2 :c 3)))))))
