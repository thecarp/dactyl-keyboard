;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Anchoring of Features                                               ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module provides a system of references to the features of a keyboard,
;;; enabling the placement of one feature in relation to another.

(ns dactyl-keyboard.param.proc.anch
  (:require [dactyl-keyboard.misc :refer [output-directory soft-merge]]))


(defn- mapmap
  [function coll]
  (into {} (mapcat function coll)))

(defn- mapmap-indexed
  [function coll]
  (into {} (apply concat (map-indexed function coll))))

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
          [alias {::type ::key-mount
                  :cluster cluster
                  :coordinates (resolve-key-coord-flex getopt cluster flex)}]))
      (getopt :key-clusters cluster :aliases))))

(defn- central-point-pair
  "Collect any aliases noted in the user configuration for one item in the
  central housing’s interface array. Data about the item is expected to have
  been enriched by the cross-index function for this purpose."
  [source-index item]
  (let [pluck (fn [path type extra]
                (when-let [alias (get-in item path)]
                  [alias (merge {::type type, :index source-index} extra)]))]
    [(pluck [:base :right-hand-alias] ::central-gabel {:side :right})
     (pluck [:base :left-hand-alias] ::central-gabel {:side :left})
     (pluck [:adapter :alias] ::central-adapter {})]))

(defn collect
  "Gather names and properties for the placement of keyboard features relative
  to one another."
  [getopt]
  (merge
    ;; One-of-a-kind types:
    {:origin {::type ::origin}
     :rear-housing-exterior {::type ::rear-housing, ::layer :exterior}
     :rear-housing-interior {::type ::rear-housing, ::layer :interior}
     :mcu-pcba {::type ::mcu-pcba}
     (getopt :mcu :support :lock :plate :alias) {::type ::mcu-lock-plate}}

    ;; Keys:
    ;; Unify cluster-specific key aliases into a single global map that
    ;; preserves their cluster of origin and resolves symbolic coordinates
    ;; to absolute values.
    (mapmap (partial key-cluster getopt) (keys (getopt :key-clusters)))

    ;; Ports:
    ;; The ID of each port automatically doubles as an alias for the negative
    ;; space of that port. The holder around it gets its own alias, with an
    ;; annotation (same as for a secondary) for tracing it to its parent port.
    (mapmap
      (fn [k]
        {k                                {::type ::port-hole}
         (getopt :ports k :holder :alias) {::type ::port-holder,
                                           ::primary k}})
      (keys (getopt :ports)))

    ;; The central housing:
    ;; A map of aliases to corresponding indices in the interface array.
    (mapmap-indexed
      central-point-pair
      (getopt :central-housing :derived :interface))

    ;; Wrist rests:
    ;; First, collect naïve coordinates of named main points. These
    ;; coordinates will require mapping onto the splined outline later. This
    ;; is an awkward consequence of aliases being collected before wrist-rest
    ;; properties are derived, and of indices changing with the actual spline
    ;; operation.
    (mapmap
      (fn [{:keys [alias position]}]
        (when alias
          {alias {::type ::wr-perimeter
                  :coordinates position}}))
      (getopt :wrist-rest :shape :spline :main-points))
    ;; Wrist rest blocks.
    (mapmap-indexed
      (fn [mount-index _]
       (mapmap
         (fn [[alias block]]
           {alias {::type ::wr-block
                   :mount-index mount-index
                   :block-key block}})
         (getopt :wrist-rest :mounts mount-index :aliases :blocks)))
      (getopt :wrist-rest :mounts))
    ;; Nuts in or on wrist rest blocks.
    (mapmap-indexed
      (fn [mount-index _]
       (mapmap
         (fn [[alias [block fastener]]]
           {alias {::type ::wr-nut
                   :mount-index mount-index
                   :block-key block
                   :fastener-index fastener}})
         (getopt :wrist-rest :mounts mount-index :aliases :nuts)))
      (getopt :wrist-rest :mounts))

    ;; Flanges:
    (mapmap
      (fn [[flange {:keys [positions]}]]
        (mapmap-indexed
          (fn [index {:keys [boss-alias]}]
            (when boss-alias
              {boss-alias {::type ::flange-boss
                           :flange flange
                           :position-index index}}))
          positions))
      (getopt :flanges))

    ;; Named secondary positions:
    (mapmap
      (fn [[name properties]]
        {name {::type ::secondary
               ;; Provide defaults absent in initial parser.
               ;; TODO: Add to parser without requiring a side or segment.
               ::primary (soft-merge {:anchoring {:anchor :origin}
                                      :override [nil nil nil]}
                                     properties)}})
      (getopt :secondaries))))

(defn- auto-body
  "Determine the default body of an anchor."
  [getopt anchor]
  (let [recurse (fn [& fragment]  ; Look up a parent/primary anchor.
                  (auto-body getopt
                    (apply getopt (concat fragment [:anchoring :anchor]))))
        wr-ambilateral #(case (getopt :derived :anchors anchor :block-key)
                           :partner-side :main
                           :wrist-side :wrist-rest)]
    (case (getopt :derived :anchors anchor ::type)
      ::origin (if (and (getopt :main-body :reflect)
                        (getopt :central-housing :include))
                 :central-housing
                 :main)
      ::wr-block (wr-ambilateral)
      ::wr-nut (wr-ambilateral)
      ::wr-perimeter :wrist-rest
      ::central-gabel :central-housing
      ::central-adapter :main   ; Sic.
      ::mcu-lock-plate (recurse :mcu)
      ::port-hole (recurse :ports anchor)
      ::port-holder (recurse :ports (getopt :derived :anchors anchor ::primary))
      ::secondary (recurse :secondaries anchor)
      ;; Default:
      :main)))

(defn resolve-body
  "Take a body setting for an anchor. Return a non-auto body ID."
  [getopt body anchor]
  {:post [(keyword? %) (not= :auto %)]}
  (case body
    :auto (let [resolved (auto-body getopt anchor)]
            (if (= resolved :auto)
              :main  ; Fall back to the main body if autos are chained up.
              resolved))
    ;; Default:
    body))
