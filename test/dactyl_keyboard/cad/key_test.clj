(ns dactyl-keyboard.cad.key_test
  (:require [clojure.test :refer [deftest testing is]]
            [dactyl-keyboard.fixture :refer [unit-testing-accessor]]
            [dactyl-keyboard.cad.key :refer [derive-nested-properties]]))


(let [a :dactyl-keyboard.cad.key/any]
  (deftest derived-nested
    (testing "emptiness"
      ;; A degenerate case to be occluded by built-in defaults.
      (let [getopt (unit-testing-accessor {:by-key {}})]
        (is (= (derive-nested-properties getopt)
               {}))))
    (testing "shallow nested structure"
      (let [getopt (unit-testing-accessor
                     {:by-key {:parameters {::k ::v}}})]
        (is (= (derive-nested-properties getopt)
               {a {a {a {a {::k ::v}}}}}))))
    (testing "deep nested structure"
      (let [location [:by-key :clusters :c :columns 2 :rows 3 :sides :N
                      :parameters]
            getopt (unit-testing-accessor (assoc-in {} location {::k ::v}))]
        (is (= (derive-nested-properties getopt)
               {:c {2 {3 {:N {::k ::v}}}}}))))
    (testing "mixed"
      (let [getopt
            (unit-testing-accessor
              (-> {}
                (assoc-in [:by-key :parameters :p2] 2)
                (assoc-in [:by-key :clusters :c1 :parameters :p1] 5)
                (assoc-in [:by-key :clusters :c1 :parameters :p2] 6)
                (assoc-in [:by-key :clusters :c1 :columns 1 :parameters :p2] 8)
                (assoc-in [:by-key :sides :SSW :clusters :c2 :parameters :p2] 1)
                (assoc-in [:by-key :rows 5 :parameters :p2] 4)
                (assoc-in [:by-key :rows 5 :columns 1 :parameters :p2] 3)))]
        (is (= (derive-nested-properties getopt)
               {:c1 {a {a {a {:p1 5, :p2 6}}}
                     1 {a {a {:p2 8}}}}
                :c2 {a {a {:SSW {:p2 1}}}}
                a   {1 {5 {a {:p2 3}}}
                     a {5 {a {:p2 4}}
                        a {a {:p2 2}}}}}))))
    (testing "relative matrix position"
      (let [location [:by-key :clusters :c :columns :last :rows :first
                      :parameters]
            derived [:key-clusters :derived :by-cluster :c]
            getopt (unit-testing-accessor
                     (-> {}
                       (assoc-in location {::k ::v})
                       (assoc-in (conj derived :column-range) '(0 1 2))
                       (assoc-in (conj derived :row-indices-by-column)
                                 {2 '(-3 -2 -1 0 -1)})))]
        (is (= (derive-nested-properties getopt)
               {:c {2 {-3 {a {::k ::v}}}}}))))))
