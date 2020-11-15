;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Shape Parameter Accessors                                           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This file describes the interpretation of configuration files for the
;;; application. It parses and validates deserialized data and builds
;;; functions used throughout the application to access that data.

(ns dactyl-keyboard.param.access
  (:require [clojure.string :as string]
            [scad-tarmi.dfm :refer [error-fn]]
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.param.base :as base]
            [dactyl-keyboard.param.tree.main :as main]))


;;;;;;;;;;;;;;
;; Internal ;;
;;;;;;;;;;;;;;

;; The locally namespaced keyword ::none is used as a sentinel for trawling the
;; user configuration.
(def setting? (fn [value] (not (= ::none value))))


;;;;;;;;;;;;;;;
;; Interface ;;
;;;;;;;;;;;;;;;

(defn checked-configuration
  "Attempt to describe any errors in the user configuration."
  [candidate]
  (try
    (base/consume-branch (base/inflate main/raws) candidate)
    (catch clojure.lang.ExceptionInfo e
      (let [data (ex-data e)]
       (println "Configuration error:" (.getMessage e))
       (println "    At key(s):" (string/join " → " (map name (:keys data))))
       (if (:accepted-keys data)
         (println "    Accepted key(s) there:"
                  (string/join ", " (map name (:accepted-keys data)))))
       (if (contains? data :raw-value)
         (println "    Value before parsing:" (:raw-value data)))
       (if (contains? data :parsed-value)
         (println "    Value after parsing:" (:parsed-value data)))
       (if (contains? data :parser)
         (println "    Parser:" (:parser data)))
       (if (:spec-explanation data)
         (println "    Validator output:" (:spec-explanation data)))
       (if (:original-exception data)
         (do (println "    Caused by:")
             (print "      ")
             (println
               (string/join "\n      "
                 (string/split-lines (pr-str (:original-exception data)))))))
       (System/exit 1)))))

(defn option-accessor
  "Close over a—potentially incomplete—user configuration."
  [build-options]
  (letfn [(value-at [path] (get-in build-options path ::none))
          (path-exists? [path] (setting? (value-at path)))
          (step [path key]
            (let [next-path (conj path key)]
              (if (path-exists? next-path) next-path path)))
          (backtrack [path] (reduce step [] path))
          (keys-if-map [value] (if (map? value) (keys value) value))]
    (fn [& path]
      (when-not (path-exists? path)
        (throw (ex-info "Configuration lacks key"
                 {:path path
                  :last-good (backtrack path)
                  :at-last-good (keys-if-map (value-at (backtrack path)))
                  :type :missing-parameter})))
      (value-at path))))

(let [a :dactyl-keyboard.cad.key/any
      side-ids (conj compass/all-short a)]
  (defn most-specific
    "Find the most specific setting applicable to a given key."
    ([getopt end-path cluster coord]
     (most-specific getopt end-path cluster coord a))
    ([getopt end-path cluster [column row] side]
     {:pre [(side side-ids)]}
     (let [pool (getopt :by-key :derived)
           combos (for [C [cluster a], c [column a], r [row a], s [side a]]
                    [C c r s])
           value (first
                   (filter setting?
                     (map #(get-in pool (concat % end-path) ::none)
                          (distinct combos))))]
       (when-not (setting? value)
         (throw
           (ex-info "Sought key-specific configuration on invalid path"
             {:end-of-path end-path
              :type :missing-parameter})))
       (when (nil? value)
         (throw
           ;; nil is not a valid setting for any key-specific parmeters.
           ;; Getting nil therefore implies that built-in defaults have
           ;; been disabled by the user.
           (ex-info "Unset key-specific configuration"
             {:end-of-path end-path
              :type :missing-parameter})))
       value))))

(defn resolve-anchor
  "Resolve the name of a feature using derived settings."
  ([getopt name]
   {:pre [(keyword? name)]}
   (getopt :derived :anchors name))
  ([getopt name predicate]
   (let [properties (resolve-anchor getopt name)]
     (if (predicate properties)
       properties
       (throw (ex-info "Named anchor cannot be used for subject feature"
                       {:name name, :properties properties,
                        :predicate predicate}))))))

(defn key-properties
  "The properties of a specific key, including derived data."
  [getopt cluster coord]
  (getopt :keys :derived (most-specific getopt [:key-style] cluster coord)))

(defn salient-anchoring
  "Strip a set of anchoring settings of some default values.
  This can keep the anchoring map relatively simple and salient for debugging
  purposes, while allowing easier application of default values for side and
  segment codes when they are actually missing, as opposed to nil."
  [anchoring]
  (->> anchoring
    (remove (comp nil? val))
    (remove (comp (partial = [0 0 0]) val))
    (remove (comp (partial = false) val))
    (into {})))

(defn compensator
  "Make a scad-tarmi error function to compensate for printing errors."
  [getopt]
  (error-fn (getopt :dfm :error-general)))
