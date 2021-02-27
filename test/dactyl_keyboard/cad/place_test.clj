(ns dactyl-keyboard.cad.place_test
  (:require [clojure.test :refer [deftest testing is]]
            [dactyl-keyboard.fixture :refer [rich-accessor]]
            [dactyl-keyboard.cad.place :as place]))


(defn- wall-coordinate-finder
  [options]
  (partial place/wall-segment-offset (rich-accessor options) ::cluster [0 0]))

(deftest key-wall-offsets
  (testing "single source, single segment"
    (let [options {:by-key {:parameters {:wall {:segments {0 {:intrinsic-offset[1 2 3]}}}}}}
          w (wall-coordinate-finder options)
          a (fn [side] (mapv int (w side 0)))]  ; Ease of comparison.
      (is (= (a :N) [0 2 3]))
      (is (= (a :NNE) [1 2 3]))
      (is (= (a :NE) [1 2 3]))
      (is (= (a :ENE) [2 1 3]))
      (is (= (a :E) [2 0 3]))
      (is (= (a :S) [0 -2 3]))
      (is (= (a :NNW) [-1 2 3]))))
  (testing "multiple sources, multiple segments"
    (let [options {:by-key {:parameters {:wall {:segments {0 {:intrinsic-offset [0 0 0]}
                                                           1 {:intrinsic-offset [0 1 -1]}
                                                           2 {:intrinsic-offset [0 1 -5]}}}}
                            :sides {:E {:parameters {:wall {:segments {1 {:intrinsic-offset [0 2 0]}}}}}}}}
          w (wall-coordinate-finder options)
          a (fn [side] (mapv int (w side 2)))]
      (is (= (a :N) [0 2 -6]))
      (is (= (a :NNE) [0 2 -6]))
      (is (= (a :NE) [0 2 -6]))
      (is (= (a :ENE) [2 0 -6]))
      (is (= (a :E) [3 0 -5]))  ; Detail overridden in configuration.
      (is (= (a :S) [0 -2 -6]))
      (is (= (a :NNW) [0 2 -6])))))
