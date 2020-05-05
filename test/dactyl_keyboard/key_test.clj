(ns dactyl-keyboard.key-test
  (:require [clojure.test :refer [deftest testing is]]
            [dactyl-keyboard.cad.key :as key]))


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
    [(key/chart-cluster ::unittest-cluster getopt)
     (partial key/key-requested? getopt ::unittest-cluster)]))

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
