;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Shape Parameter Parsers and Validators                              ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; Parameter metadata imitates clojure.tools.cli with extras.

(ns dactyl-keyboard.param.schema
  (:require [clojure.spec.alpha :as spec]
            [scad-tarmi.core :as tarmi]
            [scad-klupe.iso :refer [head-length]]
            [scad-klupe.schema.iso]
            [dmote-keycap.schema :as capschema]
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.cots :as cots]))


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

(defn any-compass-point
  "Convert to a short keyword for a compass point, even from a long string."
  [candidate]
  (compass/convert-to-any-short (keyword candidate)))

(def compass-angle-map (map-of any-compass-point num))

(defn compass-compatible-angle
  "A parser that takes an identifier of an angle. A string is converted
  to a keyword and recursed upon, a keyword is looked up as a compass
  point (returning a number on a hit and nil on a miss). Any other value is
  returned unchanged, on the assumption that it’s an angle in radians."
  [candidate]
  (cond
    (string? candidate) (compass-compatible-angle (keyword candidate))
    (keyword? candidate) (get compass/radians candidate)
    :else candidate))

(def anchored-2d-position-map
  {:anchor keyword
   :side keyword  ; Which side (e.g. corner) of anchor to use.
   :segment int   ; Which segment of anchor to use.
   :offset vec})

(def anchored-3d-position-map anchored-2d-position-map)

(def named-secondary-positions
  (map-of keyword (map-like anchored-3d-position-map)))

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

(defn case-tweak-position
  "Parse notation for a tweak position.
  This is normally a range of wall segments off a specific key corner, but
  anything down to a single alias without further specification is allowed."
  ([alias]
   [(keyword alias) nil 0 0])
  ([alias side]
   (case-tweak-position alias side 0 0))  ; Default to segment 0.
  ([alias side segment]
   (case-tweak-position alias side segment segment))
  ([alias side s0 s1]
   [(keyword alias) (keyword side) (int s0) (int s1)]))

(defn case-tweaks [candidate]
  "Parse a tweak. This can be a lazy sequence describing a single
  point, a lazy sequence of such sequences, or a map. If it is a
  map, it may contain a similar nested structure."
  (if (string? (first candidate))
    (apply case-tweak-position candidate)
    (if (map? candidate)
      ((map-like {:chunk-size int
                  :at-ground boolean
                  :above-ground boolean
                  :highlight boolean
                  :hull-around case-tweaks})
       candidate)
      (map case-tweaks candidate))))

(def case-tweak-map
  "A parser of a map of names to tweaks."
  (map-of keyword case-tweaks))

(def keycap-map
  "A parser for the options exposed by the dmote-keycap library.
  For ease of maintenance, this map is complete, even though some options to
  dmote-keycap, such as filename, are effectively useless inside a DMOTE
  configuration."
  (map-of keyword (map-like capschema/option-parsers)))


;;;;;;;;;;;;;;;;
;; Validators ;;
;;;;;;;;;;;;;;;;

;; A complex spec predicate for scad-klupe bolts, extending to the
;; relationships between length specifiers including head length.
;; Because head length is calculated along the way, this validator does
;; not provide very helpful output on failure.
(spec/def ::comprehensive-bolt-properties
  (spec/and
    :scad-klupe.schema.iso/bolt-parameters
    (fn [{:keys [m-diameter head-type] :as parameters}]
      (spec/valid? :scad-klupe.schema.base/bolt-length-parameters
        (assoc parameters :head-length (head-length m-diameter head-type))))))

;; Used with spec/keys, making the names sensitive:
(spec/def ::include boolean?)
(spec/def ::anchor keyword?)
(spec/def ::alias (spec/and keyword?
                            #(not (= :origin %))
                            #(not (= :rear-housing %))))
(spec/def ::segment (spec/int-in 0 5))
(spec/def ::thickness (spec/and number? (complement neg?)))
(spec/def ::highlight boolean?)
(spec/def ::at-ground boolean?)
(spec/def ::above-ground boolean?)
(spec/def ::chunk-size (spec/and int? #(> % 1)))
(spec/def ::hull-around (spec/coll-of (spec/or :leaf ::tweak-plate-leaf
                                               :map ::tweak-plate-map)))
(spec/def ::spline-point
  (spec/keys :req-un [::position]  ; 2D.
             :opt-un [::alias]))
(spec/def ::port-type (set (conj (keys cots/port-facts) :custom)))
(spec/def :port/size ::tarmi/point-3d)
(spec/def :port/intrinsic-rotation ::tarmi/point-3d)
(spec/def :central/offset ::tarmi/point-3d)
(spec/def :central/left-hand-alias ::alias)
(spec/def :central/right-hand-alias ::alias)
(spec/def :central/base
  (spec/keys :req-un [:central/offset]
             :opt-un [:central/left-hand-alias :central/right-hand-alias]))
(spec/def :central/adapter
  (spec/keys :opt-un [:central/offset ::alias]))
(spec/def :central/interface-node
  (spec/keys :req-un [:central/base]
             :opt-un [:central/adapter ::at-ground ::above-ground]))
(spec/def :central/starting-point keyword?)
(spec/def :central/direction-point keyword?)
(spec/def :central/lateral-offset #(not (zero? %)))
(spec/def :central/radial-offset #(not (zero? %)))
(spec/def :central/fastener-node
  (spec/keys :req-un [:central/starting-point
                      :central/lateral-offset
                      :central/radial-offset]
             :opt-un [:central/direction-point]))

;; Also used with spec/keys, with closer competition, hence non-local,
;; non-module namespacing.
(spec/def :numeric/direction number?)
(spec/def :intercardinal/side compass/intercardinals)
(spec/def :intermediate/side compass/intermediates)
(spec/def :flexible/side compass/noncardinals)
(spec/def :two/offset ::tarmi/point-2d)
(spec/def :three/offset ::tarmi/point-3d)
(spec/def :flexible/offset ::tarmi/point-2-3d)

;; Users thereof:
(spec/def ::foot-plate (spec/keys :req-un [::points]))
(spec/def ::anchored-2d-position
  (spec/keys :opt-un [::anchor :flexible/side :two/offset]))
(spec/def ::anchored-3d-position
  (spec/keys :opt-un [::anchor :flexible/side :three/offset]))
(spec/def ::named-secondary-positions
  (spec/map-of ::alias
               (spec/keys :req-un [::anchor]
                          :opt-un [:flexible/side ::segment :three/offset])))
(spec/def ::anchored-2d-list (spec/coll-of ::anchored-2d-position))
(spec/def ::projecting-2d-list
  (spec/coll-of
    (spec/and
      ::anchored-2d-position
      (spec/keys :opt-un [:numeric/direction]))))
(spec/def ::points ::anchored-2d-list)
(spec/def ::central-housing-interface (spec/coll-of :central/interface-node))
(spec/def ::central-housing-normal-positions (spec/coll-of :central/fastener-node))
(spec/def ::mcu-grip-anchors
  (spec/coll-of
    (spec/keys :req-un [::alias :intercardinal/side]
               :opt-un [:flexible/offset])))
(spec/def ::port-holder (spec/keys :req-un [::include ::alias ::thickness]))
(spec/def ::port-map
  (spec/map-of
    ::alias
    (spec/keys :opt-un [::include ::port-type :port/size :three/position
                        :port/intrinsic-rotation ::port-holder])))
(spec/def ::tweak-name-map (spec/map-of keyword? ::hull-around))
(spec/def ::tweak-plate-map
  (spec/keys :req-un [::hull-around]
             :opt-un [::highlight ::chunk-size ::at-ground ::above-ground]))
(spec/def ::nameable-spline (spec/coll-of ::spline-point))

;; Other:
(spec/def ::key-cluster #(not (= :derived %)))
(spec/def ::cluster-style #{:standard :orthographic})
(spec/def ::plate-installation-style #{:threads :inserts})
(spec/def ::compass-angle-map
  (spec/map-of compass/cardinals (spec/and (complement neg?)
                                           #(<= % (* 3/8 tarmi/π)))))

(spec/def ::wrist-rest-style #{:threaded :solid})
(spec/def ::wrist-position-style #{:case-side :mutual})
(spec/def ::wrist-block #{:case-side :plinth-side})
(spec/def ::column-disposition
  (spec/keys ::opt-un [::rows-below-home ::rows-above-home]))
(spec/def ::flexcoord (spec/or :absolute int? :extreme #{:first :last}))
(spec/def ::flexcoord-2d (spec/coll-of ::flexcoord :count 2))
(spec/def ::key-coordinates ::flexcoord-2d)  ; Exposed for unit testing.
(spec/def ::wall-segment ::segment)
(spec/def ::wall-extent (spec/or :partial ::wall-segment :full #{:full}))
(spec/def ::tweak-plate-leaf
  (spec/tuple keyword? (spec/nilable :flexible/side) ::wall-segment ::wall-segment))
(spec/def ::foot-plate-polygons (spec/coll-of ::foot-plate))

(spec/def ::descriptor  ; Parameter metadata descriptor.
  #{:path :heading-template :help :default :parse-fn :validate :resolve-fn})
(spec/def ::parameter-spec (spec/map-of ::descriptor some?))
