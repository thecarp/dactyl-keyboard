;;; Application-specific fixtures for unit testing.

(ns dactyl-keyboard.fixture
  (:require [clojure.test :refer [with-test-out]]
            [clojure.pprint :refer [pprint]]
            [dactyl-keyboard.param.access :refer [option-accessor]]))

(defn unit-testing-accessor
  [build-options]
  (let [service (option-accessor build-options)]
    (fn [& path]
      (try
        (apply service path)
        (catch clojure.lang.ExceptionInfo e
          (with-test-out
            (println "Mock option accessor got unexpected call:")
            (pprint (ex-data e)))
          (throw e))))))

