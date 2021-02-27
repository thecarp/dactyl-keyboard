;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Shape Parameter Parsers                                             ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; Parameter metadata imitates clojure.tools.cli with extras.

(ns dactyl-keyboard.param.schema.parse
  (:require [clojure.spec.alpha :as spec]
            [scad-tarmi.core :refer [π]]
            [scad-klupe.schema.iso]
            [dmote-keycap.schema :as capschema]
            [dactyl-keyboard.compass :as compass]))


;;;;;;;;;;;;;
;; Parsers ;;
;;;;;;;;;;;;;

(defn tuple-of
  "A maker of parsers for vectors."
  [item-parser]
  (fn [candidate] (into [] (map item-parser candidate))))

(defn map-like
  "Return a parser of a map where the exact keys are known."
  [key-value-parsers]
  (letfn [(parse-item [[key value]]
            (if-let [value-parser (get key-value-parsers key)]
              [key (value-parser value)]
              (throw (Exception. (format "Invalid key: %s" key)))))]
    (fn [candidate] (into {} (map parse-item candidate)))))

(defn map-of
  "Return a parser of a map where the general type of key is known."
  [key-parser value-parser]
  (letfn [(parse-item [[key value]]
            [(key-parser key) (value-parser value)])]
    (fn [candidate] (into {} (map parse-item candidate)))))

(defn integer-fn
  "Work around a facet of clj-yaml, wherein integer keys to maps are parsed as
  keywords, probably because JS objects can only have string keys."
  [non-number-fallback candidate]
  (try
    (int candidate)  ; Input like “1”.
    (catch ClassCastException _
      (try
        (Integer/parseInt (name candidate))  ; Input like “:1” (clj-yaml key).
        (catch java.lang.NumberFormatException _
          (non-number-fallback candidate))))))

;; Preserve any non-integer as is, for the validator to judge.
(def integer (partial integer-fn identity))

;; Parse non-integer strings as keywords.
(def keyword-or-integer (partial integer-fn keyword))

(defn pad-to-3-tuple
  "Pad a single number, or a vector of 1–3 numbers, to a vector of 3 numbers."
  ;; Notice this is distinct from the pad-to-3d utility function. This one
  ;; favours the duplication of an xy coordinate over z when given a 2-tuple.
  [candidate]
  (vec
    (if (number? candidate)
      (repeat 3 candidate)
      (case (count candidate)
        1 (pad-to-3-tuple (first candidate))
        2 [(first candidate) (first candidate) (second candidate)]
        3 candidate))))

(let [re #"(?i)^(π|pi)(\s?(\*|/)\s?(-?\d+\.?\d*))?$"]
  (defn compass-incompatible-angle
    "A parser for angles in radians.
    This function supports a limited notation for dividing and multiplying by
    π."
    [candidate]
    {:post [(number? %)]}
    (try
      (num candidate)  ; Input like “1”.
      (catch ClassCastException e
        ;; Not a number. Assume a string starting with “π”.
        ;; Parse out the operator and the factor or divisor.
        (if-let [[_ _ _ op n] (re-matches re candidate)]
          ((case op "/" / *) π (if n (Float/parseFloat n) 1))
          (throw e))))))

(defn optional-keyword
  "Convert to a short keyword for a compass point, even from a long string.
  Also accept nil."
  [candidate]
  (when (some? candidate) (keyword candidate)))

(def compass-angle-map (map-of keyword compass-incompatible-angle))

(defn compass-compatible-angle
  "A parser that takes an identifier of an angle, including via the compass.
  When given a string, this function first tries to parse it in the narrow
  range of compass-incompatible angles and, failing that, converts it to a keyword
  and recurses upon it. A keyword is looked up as a compass point, returning a
  number on a hit and nil on a miss."
  [candidate]
  {:post [(or (nil? %) (number? %))]}
  (if (keyword? candidate)
    (candidate compass/radians)
    (try
      (compass-incompatible-angle candidate)
      (catch ClassCastException _
        (compass-compatible-angle (keyword candidate))))))

(def nameable-spline
  (tuple-of
    (map-like
      {:position (tuple-of num)
       :alias keyword})))

(def explicit-bolt-properties (map-like scad-klupe.schema.iso/bolt-parsers))

(defn implicit-bolt-properties
  "Parse the properties of a bolt with an implicit, dynamic addition.
  Where the user has not supplied any information at all on the length of
  the bolt, interpolate a total length of 10 mm. This fallback is designed
  not to interfere with the user’s choice among scad-klupe’s options for how
  to specify a length."
  [candidate]
  (let [explicit (explicit-bolt-properties candidate)]
    (merge explicit
      (when-not (spec/valid? :scad-klupe.schema.base/bolt-length-specifiers
                  explicit)
        {:unthreaded-length 10}))))

(defn nested-key-fn
  [parameter-set depth]
  (map-like (merge {:parameters parameter-set}
                   (when (pos? depth)
                     (let [descent (nested-key-fn parameter-set (dec depth))]
                       {:clusters (map-of keyword descent)
                        :columns (map-of keyword-or-integer descent)
                        :rows (map-of keyword-or-integer descent)
                        :sides (map-of keyword descent)})))))

(def central-housing-interface
  (tuple-of
    (map-like
      {:at-ground boolean
       :above-ground boolean
       :base (map-like {:offset vec
                        :left-hand-alias keyword
                        :right-hand-alias keyword})
       :adapter (map-like {:alias keyword
                           :segments (map-of integer
                                             (map-like {:intrinsic-offset
                                                        (tuple-of num)}))})})))

(def central-housing-normal-positions
  (tuple-of
    (map-like
      {:starting-point keyword
       :direction-point keyword
       :axial-offset num
       :radial-offset num})))

(def keycap-map
  "A parser for the options exposed by the dmote-keycap library.
  For ease of maintenance, this map is complete, even though some options to
  dmote-keycap, such as filename, are effectively useless inside a DMOTE
  configuration."
  (map-of keyword (map-like capschema/option-parsers)))
