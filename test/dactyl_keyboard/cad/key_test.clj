(ns dactyl-keyboard.cad.key_test
  (:require [clojure.test :refer [deftest testing is]]
            [dactyl-keyboard.fixture :refer [unit-testing-accessor]]
            [dactyl-keyboard.cad.key :as k]))

(let [a ::k/any]
  (deftest derived-nested
    (testing "emptiness"
      ;; A degenerate case to be occluded by built-in defaults.
      (let [getopt (unit-testing-accessor {:by-key {}})]
        (is (= (k/derive-nested-properties getopt)
               {}))))
    (testing "shallow nested structure"
      (let [getopt (unit-testing-accessor
                     {:by-key {:parameters {::k ::v}}})]
        (is (= (k/derive-nested-properties getopt)
               {a {a {a {a {::k ::v}}}}}))))
    (testing "deep nested structure"
      (let [location [:by-key :clusters :c :columns 2 :rows 3 :sides :N
                      :parameters]
            getopt (unit-testing-accessor (assoc-in {} location {::k ::v}))]
        (is (= (k/derive-nested-properties getopt)
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
        (is (= (k/derive-nested-properties getopt)
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
        (is (= (k/derive-nested-properties getopt)
               {:c {2 {-3 {a {::k ::v}}}}}))))))

(defn- scenario
  "Chart a cluster. Return a local partial derived configuration and an
  occlusion predicate based upon it."
  [matrix-columns]
  (let [getopt (fn [& keys]
                 (get-in
                   {:key-clusters
                     {::unittest-cluster
                       {:matrix-columns matrix-columns}}}
                   keys))]
    [(k/chart-cluster ::unittest-cluster getopt)
     (partial k/key-requested? getopt ::unittest-cluster)]))

(deftest single-key-cluster
  (let [[getopt, req?] (scenario [{}])]
    (testing "general derivation"
      (is (= (:last-column getopt) 0))
      (is (= (:column-range getopt) [0]))
      (is (= (:key-coordinates getopt) '([0 0])))
      (is (= (:row-indices-by-column getopt) {0 '(0)}))
      (is (= (:column-indices-by-row getopt) {0 '(0)})))
    (testing "occlusion"
      (is (= (req? [-1 0]) false))
      (is (= (req? [0 -1]) false))
      (is (= (req? [0 0]) true))
      (is (= (req? [0 1]) false))
      (is (= (req? [1 0]) false)))))

(deftest five-key-cluster  ; ┗┓-shaped
  (let [[getopt, req?] (scenario [{:rows-above-home 2}
                                  {}
                                  {:rows-below-home 2}])]
    (testing "general derivation"
      (is (= (:last-column getopt) 2))
      (is (= (:column-range getopt) [0 1 2]))
      (is (= (:key-coordinates getopt)
             '([0 0] [0 1] [0 2] [1 0] [2 -2] [2 -1] [2 0])))
      (is (= (:row-indices-by-column getopt)
             '{0 (0 1 2), 1 (0), 2 (-2 -1 0)}))
      (is (= (:coordinates-by-column getopt)
             '{0 ([0 0] [0 1] [0 2]), 1 ([1 0]), 2 ([2 -2] [2 -1] [2 0])}))
      (is (= (:column-indices-by-row getopt)
             '{-2 (2), -1 (2), 0 (0 1 2), 1 (0), 2 (0)})))
    (testing "occlusion"
      (is (= (req? [-1 0]) false))
      (is (= (req? [0 -1]) false))
      (is (= (req? [0 0]) true))
      (is (= (req? [0 1]) true))
      (is (= (req? [0 2]) true))
      (is (= (req? [0 3]) false))
      (is (= (req? [1 -1]) false))
      (is (= (req? [1 0]) true))
      (is (= (req? [1 1]) false))
      (is (= (req? [2 -3]) false))
      (is (= (req? [2 -2]) true))
      (is (= (req? [2 -1]) true))
      (is (= (req? [2 0]) true))
      (is (= (req? [2 1]) false)))))
