;;; Application-specific fixtures for unit testing.

(ns dactyl-keyboard.fixture
  (:require [clojure.test :refer [with-test-out]]
            [clojure.pprint :refer [pprint]]
            [dactyl-keyboard.param.access :refer [checked-configuration option-accessor]]
            [dactyl-keyboard.param.rich :refer [enrich-option-metadata]]))

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

(defn rich-accessor
  "Inject built-in defaults and derivation into a configuration map."
  [build-options]
  (try
    (-> build-options checked-configuration enrich-option-metadata unit-testing-accessor)
    (catch clojure.lang.ExceptionInfo e
      (with-test-out
        (println "Could not enrich mock options:")
        (pprint (ex-data e)))
      (throw e))))
