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
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.misc :refer [soft-merge]]))


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

(defn keyword-or-integer
  "A parser that takes a number as an integer or a string as a keyword.
  This works around a peculiar facet of clj-yaml, wherein integer keys to
  maps are parsed as keywords."
  [candidate]
  (try
    (int candidate)  ; Input like “1”.
    (catch ClassCastException _
      (try
        (Integer/parseInt (name candidate))  ; Input like “:1” (clj-yaml key).
        (catch java.lang.NumberFormatException _
          (keyword candidate))))))           ; Input like “:first” or “"first"”.

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

(defn any-compass-point
  "Convert to a short keyword for a compass point, even from a long string.
  Also accept nil."
  [candidate]
  (when (some? candidate) (compass/convert-to-any-short (keyword candidate))))

(def compass-angle-map (map-of any-compass-point compass-incompatible-angle))

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

(def anchored-2d-position-map
  {:anchor keyword
   :side keyword  ; Which side (e.g. corner) of anchor to use.
   :segment int   ; Which segment of anchor to use.
   :offset vec})

(def anchored-3d-position-map anchored-2d-position-map)

(def named-secondary-positions
  (map-of keyword
    (map-like
      {:anchoring (map-like anchored-3d-position-map)
       :override vec
       :translation vec})))

(def anchored-2d-positions
  (tuple-of (map-like anchored-2d-position-map)))

(def projecting-2d-positions
  (tuple-of (map-like (assoc anchored-2d-position-map
                        :direction compass-compatible-angle))))

(def anchored-polygons
  (tuple-of
    (map-like
      {:points anchored-2d-positions})))

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

(def central-housing-interface
  (tuple-of
    (map-like
      {:at-ground boolean
       :above-ground boolean
       :base (map-like {:offset vec
                        :left-hand-alias keyword
                        :right-hand-alias keyword})
       :adapter (map-like {:offset vec
                           :alias keyword})})))

(def central-housing-normal-positions
  (tuple-of
    (map-like
      {:starting-point keyword
       :direction-point keyword
       :lateral-offset num
       :radial-offset num})))

(def mcu-grip-anchors
  (tuple-of
    (map-like
      {:side keyword
       :offset vec
       :alias keyword})))

(let [leaf-skeleton {:anchoring (map-like anchored-3d-position-map)
                     :sweep int
                     :size vec}
      branch-skeleton {:chunk-size int
                       :highlight boolean}
      top-extras {:positive boolean
                  :at-ground boolean
                  :above-ground boolean
                  :body keyword}
      tail-leafer (map-like leaf-skeleton)
      destructure-leaf
        (fn parse-leaf
          ([anchor]
           (parse-leaf anchor nil))
          ([anchor side]
           (parse-leaf anchor side nil nil))
          ([anchor side segment]
           (parse-leaf anchor side segment nil))
          ([anchor side segment sweep]
           (parse-leaf anchor side segment sweep {}))
          ([anchor side segment sweep options]
           {:pre [(map? options)]}
           (reduce
             (fn [coll [item path parser]]
               (cond
                 (map? item) (soft-merge coll item)
                 (some? item) (assoc-in coll path (parser item))
                 :else coll))
             (merge {:anchoring {:anchor :origin}} options)
             [[anchor [:anchoring :anchor] keyword]
              [side [:anchoring :side] keyword]
              [segment [:anchoring :segment] int]
              [sweep [:sweep] int]])))
      dispatch-fn
        (fn [brancher leafer]
           (fn dispatch [cnd]
             (cond
               (and (map? cnd) (contains? cnd :hull-around)) (brancher cnd)
               (map? cnd) (leafer (destructure-leaf cnd))
               (string? (first cnd)) (leafer (apply destructure-leaf cnd))
               :else (map dispatch cnd))))
      tail-brancher (fn parse [candidate]
                      ((map-like (merge branch-skeleton
                                        {:hull-around
                                         (dispatch-fn parse tail-leafer)}))
                       candidate))]
  (def tweak-grove
    "Parse the tweak configuration.

    In local nomenclature, the “tweaks” parameter is a grove of trees. Each
    node beneath the name level of the grove is a tree that can have
    some extra properties. Subordinate nodes cannot have these extra properties.

    The grove is parsed using a pair of dispatchers, each with its own branch
    and leaf parsers. The initial dispatcher replaces itself with a secondary
    dispatcher each time it passes a branch node, and the secondary
    dispatcher sustains itself by the trick of its parser being a function that
    refers to itself and thereby passes itself along by recreating the
    lower-lever dispatcher on each pass.

    A candidate to the dispatcher can be a lazy sequence describing a single
    point (a leaf), a lazy sequence of such sequences, or a map. If it is a
    map, it may contain a similar nested structure, or a predigested leaf.

    The basic leaf parser is permissive, having multiple arities where any
    positional argument can be replaced by a map. However, if a short-form
    (sequence) leaf starts with a map, the dispatcher will not identify it as a
    leaf, because of ambiguity with respect to a node list. A more stateful
    parser could handle that case."
    (map-of keyword
            (dispatch-fn
              (map-like (merge branch-skeleton top-extras
                               {:hull-around
                                (dispatch-fn tail-brancher tail-leafer)}))
              (map-like (merge leaf-skeleton top-extras))))))

(def keycap-map
  "A parser for the options exposed by the dmote-keycap library.
  For ease of maintenance, this map is complete, even though some options to
  dmote-keycap, such as filename, are effectively useless inside a DMOTE
  configuration."
  (map-of keyword (map-like capschema/option-parsers)))
