;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Anchoring of Features                                               ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module provides a system of references to the features of a keyboard,
;;; enabling the placement of one feature in relation to another.

(ns dactyl-keyboard.param.proc.anch
  (:require [dactyl-keyboard.misc :refer [output-directory soft-merge]]))


(defn- resolve-key-coord-flex
  "Resolve supported keywords in a coordinate pair to names.
  This allows for integers as well as the keywords :first and :last, meaning
  first and last in the column or row. Columns take priority."
  [getopt cluster [c0 r0]]
  {:pre [(keyword? cluster)]}
  (let [prop (partial getopt :key-clusters :derived :by-cluster cluster)
        columns (prop :column-range)
        c1 (case c0 :first (first columns) :last (last columns) c0)
        rows (prop :row-indices-by-column c1)]
   [c1 (case r0 :first (first rows) :last (last rows) r0)]))

(defn- key-cluster
  "Make a map of aliased keys within a single key cluster."
  [getopt cluster]
  (when-not (= cluster :derived)  ; :derived is just metadata.
    (into {}
      (map
        (fn [[alias flex]]
          [alias {::type :key
                  :cluster cluster
                  :coordinates (resolve-key-coord-flex getopt cluster flex)}]))
      (getopt :key-clusters cluster :aliases))))

(defn- central-point-pair
  "Collect any aliases noted in the user configuration for one item in the
  central housing’s interface array. Data about the item is expected to have
  been enriched by the cross-index function for this purpose."
  [source-index item]
  (let [props {::type :central-housing, :index source-index}
        pluck (fn [path part extra]
                (when-let [alias (get-in item path)]
                  [alias (merge props {:part part} extra)]))]
    [(pluck [:base :right-hand-alias] :gabel {:side :right})
     (pluck [:base :left-hand-alias] :gabel {:side :left})
     (pluck [:adapter :alias] :adapter {})]))

(defn- wrist-rest-block-alias
  [getopt mount-index]
  (reduce
    (fn [coll [alias side-key]]
      (merge coll
        {alias {::type :wr-block
                :mount-index mount-index
                :side-key side-key}}))
    {}
    (getopt :wrist-rest :mounts mount-index :blocks :aliases)))

(defn collect
  "Gather names and properties for the placement of keyboard features relative
  to one another."
  [getopt]
  (merge
    ;; One-of-a-kind types:
    {:origin {::type :origin}
     :rear-housing {::type :rear-housing}
     (getopt :mcu :support :lock :plate :alias) {::type :mcu-lock-plate}}

    ;; Keys:
    ;; Unify cluster-specific key aliases into a single global map that
    ;; preserves their cluster of origin and resolves symbolic coordinates
    ;; to absolute values.
    (into {} (mapcat (partial key-cluster getopt) (keys (getopt :key-clusters))))

    ;; Ports:
    ;; The ID of each port automatically doubles as an alias for the negative
    ;; space of that port. The holder around it gets its own alias, with an
    ;; annotation (same as for a secondary) for tracing it to its parent port.
    (apply merge
      (map
        (fn [k]
          {k                                {::type :port-hole}
           (getopt :ports k :holder :alias) {::type :port-holder,
                                             ::primary k}})
        (keys (getopt :ports))))

    ;; The central housing:
    ;; A map of aliases to corresponding indices in the interface array.
    (->> (getopt :central-housing :derived :interface)
      (map-indexed central-point-pair)
      (apply concat)
      (into {}))

    ;; MCU grips:
    ;; Collect the names of MCU grip anchors. Expand 2D offsets to 3D.
    (reduce
      (fn [coll {:keys [side offset alias] :or {offset [0 0]}}]
        (assoc coll alias
          {::type :mcu-grip,
           :side side,
           :offset (subvec (vec (conj offset 0)) 0 3)}))
      {}
      (getopt :mcu :support :grip :anchors))

    ;; Wrist rests:
    ;; First, collect naïve coordinates of named main points. These
    ;; coordinates will require mapping onto the splined outline later. This
    ;; is an awkward consequence of aliases being collected before wrist-rest
    ;; properties are derived, and of indices changing with the actual spline
    ;; operation.
    (reduce
      (fn [coll point-properties]
        (if-let [alias (:alias point-properties)]
          (merge coll
            {alias {::type :wr-perimeter
                    :coordinates (:position point-properties)}})
          coll))
      {}
      (getopt :wrist-rest :shape :spline :main-points))
    ;; Wrist rest blocks.
    (reduce
      (fn [coll mount-index]
        (merge coll (wrist-rest-block-alias getopt mount-index)))
      {}
      (range (count (getopt :wrist-rest :mounts))))

    ;; Named secondary positions:
    (into {}
      (for [[k v] (getopt :secondaries)]
        [k {::type :secondary
            ;; Provide defaults absent in initial parser.
            ;; TODO: Add to parser without requiring a side or segment.
            ::primary (soft-merge {:anchoring {:anchor :origin}
                                   :override [nil nil nil]
                                   :translation [0 0 0]}
                                  v)}]))))

(defn- auto-body
  "Determine the default body of an anchor."
  [getopt anchor]
  (case (getopt :derived :anchors anchor ::type)
    :origin (if (and (getopt :main-body :reflect)
                     (getopt :central-housing :include))
              :central-housing
              :main-body)
    :port-hole (auto-body getopt (getopt :ports anchor :anchoring :anchor))
    :port-anchor (auto-body getopt (getopt :ports anchor :anchoring :anchor))
    :mcu-lock-plate (auto-body getopt (getopt :mcu :anchoring :anchor))
    :secondary (auto-body getopt (getopt :secondaries anchor :anchoring :anchor))
    :central-housing :central-housing
    ;; Default:
    :main-body))

(defn resolve-body
  "Take a body setting for a feature. Return a non-auto body ID."
  [getopt setting anchor]
  {:post [#{:main-body :central-housing}]}
  (case setting
    :auto (let [resolved (auto-body getopt anchor)]
            (if (= resolved :auto)
              :main-body  ; Fall back to the main body if autos are chained up.
              resolved))
  ;; Default:
    setting))
