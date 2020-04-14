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

(let [leaf-map (map-like {:anchoring (map-like anchored-3d-position-map)
                          :sweep identity  ; Permit nil in the final result.
                          :size vec})
      leaf
        (fn parse-leaf
          ([anchor]
           (parse-leaf anchor nil))
          ([anchor side]
           (parse-leaf anchor side nil nil))
          ([anchor side segment]
           (parse-leaf anchor side segment segment))
          ([anchor side first-segment last-segment]
           (parse-leaf anchor side first-segment last-segment {}))
          ([anchor side first-segment last-segment options]
           {:pre [(map? options)]}
           (leaf-map
             (reduce
               (fn [coll [item path parser]]
                 (cond
                   (map? item) (soft-merge item coll)
                   (some? item) (assoc-in coll path (parser item))
                   :else coll))
               (merge {:anchoring {:anchor :origin}, :sweep nil} options)
               [[anchor [:anchoring :anchor] keyword]
                [side [:anchoring :side] keyword]
                [first-segment [:anchoring :segment] int]
                [last-segment [:sweep] int]]))))
      branch-skeleton {:chunk-size int
                       :above-ground boolean
                       :highlight boolean}
      dispatch-fn (fn [brancher]
                     (fn dispatch [cnd]
                       (cond
                         (and (map? cnd) (:hull-around cnd)) (brancher cnd)
                         (string? (first cnd)) (apply leaf cnd)
                         (and (map? cnd) (:anchoring cnd)) (leaf cnd)
                         :else (map dispatch cnd))))
      tail (fn parse [candidate]
             ((map-like (merge branch-skeleton
                               {:hull-around (dispatch-fn parse)}))
              candidate))]
  (def tweak-grove
    "Parse the tweak configuration.

    In local nomenclature, the “tweaks” parameter is a grove of trees. Each
    non-leaf node beneath the name level of the grove is a tree that can have
    some extra properties. Lower-level non-leaf nodes are just branches and
    cannot have these extra properties.

    The grove is parsed using a pair of dispatchers, each with its own branch
    parser. The top-level dispatcher replaces itself with the lower-level
    dispatcher each time it passes a non-leaf node, and the lower-level
    dispatcher sustains itself by the trick of its parser being a function that
    refers to itself and thereby passes itself along by recreating the
    lower-lever dispatcher on each pass.

    A candidate to the dispatcher can be a lazy sequence describing a single
    point (a leaf), a lazy sequence of such sequences, or a map. If it is a
    map, it may contain a similar nested structure, or a predigested leaf.

    The leaf parser is permissive, having multiple arities where any positional
    argument can be replaced by a map.  The base case is one anchor, typically
    a key alias, with ordinary specifiers for a side and vertical segment off
    that anchor. However, the parser also accepts a second segment ID to form a
    sweep across a range of segments, and in place of any or all positional
    arguments after the first (typically coming in last), the parser takes a
    map of extras that may overlap the meaning of the positional arguments."
    (map-of keyword
            (dispatch-fn
              (map-like (merge branch-skeleton
                               {:positive boolean
                                :at-ground boolean
                                :body keyword
                                :hull-around (dispatch-fn tail)}))))))

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
(spec/def ::positive boolean?)
(spec/def ::body #{:auto :main-body :central-housing})
(spec/def ::anchor keyword?)
(spec/def ::alias (spec/and keyword?
                            #(not (= :origin %))
                            #(not (= :rear-housing %))))
(spec/def ::segment (spec/int-in 0 5))
(spec/def ::sweep (spec/nilable ::segment))
(spec/def ::thickness (spec/and number? (complement neg?)))
(spec/def ::highlight boolean?)
(spec/def ::at-ground boolean?)
(spec/def ::above-ground boolean?)
(spec/def :tweak/chunk-size (spec/and int? #(> % 1)))
(spec/def :tweak/size ::tarmi/point-3d)
(spec/def ::spline-point
  (spec/keys :req-un [::position]  ; 2D.
             :opt-un [::alias]))
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
;; TODO: Make sure the various placement functions affected by flexible/side
;; can actually take all directions, by lossy approximation where necessary.
(spec/def :flexible/side compass/all-short)
(spec/def :two/offset ::tarmi/point-2d)
(spec/def :three/offset ::tarmi/point-3d)
(spec/def :three/override (spec/coll-of (spec/nilable number?) :count 3))
(spec/def :three/translation ::tarmi/point-3d)
(spec/def :flexible/offset ::tarmi/point-2-3d)

;; Users thereof:
(spec/def ::foot-plate (spec/keys :req-un [::points]))
(spec/def ::anchored-2d-position
  (spec/keys :opt-un [::anchor :flexible/side ::segment :two/offset]))
(spec/def ::anchored-3d-position
  (spec/keys :opt-un [::anchor :flexible/side ::segment :three/offset]))
(spec/def ::anchoring ::anchored-3d-position)
(spec/def ::named-secondary-positions
  (spec/map-of ::alias
               (spec/keys :opt-un [::anchoring :three/override
                                   :three/translation])))
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
(spec/def :tweak/hull-around (spec/coll-of (spec/or :leaf ::tweak-leaf
                                                    :map ::tweak-branch)))
(spec/def ::tweak-name-map (spec/map-of keyword? :tweak/hull-around))
(spec/def ::tweak-branch
  (spec/keys :req-un [:tweak/hull-around]
             :opt-un [::highlight :tweak/chunk-size ::above-ground
                      ;; Additional keys expected in trees only:
                      ::positive ::at-ground ::body]))

(spec/def ::nameable-spline (spec/coll-of ::spline-point))

;; Other:
(spec/def ::key-cluster #(not (= :derived %)))
(spec/def ::cluster-style #{:standard :orthographic})
(spec/def ::plate-installation-style #{:threads :inserts})
(spec/def ::compass-angle-map
  (spec/map-of compass/cardinals (spec/and (complement neg?)
                                           #(<= % (* 3/8 tarmi/π)))))

(spec/def ::wrist-rest-style #{:threaded :solid})
(spec/def ::wrist-position-style #{:main-side :mutual})
(spec/def ::wrist-block #{:main-side :plinth-side})
(spec/def ::column-disposition
  (spec/keys ::opt-un [::rows-below-home ::rows-above-home]))
(spec/def ::flexcoord (spec/or :absolute int? :extreme #{:first :last}))
(spec/def ::flexcoord-2d (spec/coll-of ::flexcoord :count 2))
(spec/def ::key-coordinates ::flexcoord-2d)  ; Exposed for unit testing.
(spec/def ::wall-segment ::segment)
(spec/def ::wall-extent (spec/or :partial ::wall-segment :full #{:full}))
(spec/def ::tweak-leaf
  (spec/and
    (spec/keys :req-un [::anchoring ::sweep]
               :opt-un [:tweak/size])
    ;; Require a start to a sweep
    (fn [{:keys [anchoring sweep]}] (if (some? sweep)
                                        (some? (:segment anchoring))
                                        true))
    ;; Make sure the sweep does not end before it starts.
    (fn [{:keys [anchoring sweep]}] (<= (get anchoring :segment 0)
                                        (or sweep 5)))))
(spec/def ::foot-plate-polygons (spec/coll-of ::foot-plate))

(spec/def ::descriptor  ; Parameter metadata descriptor.
  #{:path :heading-template :help :default :parse-fn :validate :resolve-fn})
(spec/def ::parameter-spec (spec/map-of ::descriptor some?))
