;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Basic Key Utilities                                                 ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.key
  (:require [scad-clj.model :as model]
            [scad-tarmi.core :refer [abs π]]
            [scad-tarmi.maybe :as maybe]
            [dmote-keycap.data :as capdata]
            [dmote-keycap.measure :as measure]
            [dactyl-keyboard.misc :as misc]
            [dactyl-keyboard.cad.matrix :as matrix]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.key.switch :refer [cap-channel-negative
                                                    cap-positive
                                                    switch-for-cap
                                                    mount-thickness]]
            [dactyl-keyboard.param.access :refer [most-specific key-properties
                                                  compensator]]))


;;;;;;;;;;;;;;
;; Internal ;;
;;;;;;;;;;;;;;

(defn adaptive-plate
  "The shape of a key mounting plate based on the style of the key."
  [getopt cluster coord]
  (let [most #(most-specific getopt % cluster coord)
        style-data (getopt :keys :derived (most [:key-style]))
        [x y] (map measure/key-length (get style-data :unit-size [1 1]))
        z (mount-thickness getopt cluster coord)]
    (model/translate [0 0 (/ z -2)]
      (model/cube x y z))))

(defn custom-plate
  "The shape of a key mounting plate not based on the style of the key."
  [getopt cluster coord]
  (let [most #(most-specific getopt [:plate %] cluster coord)
        [x y z] (most :size)]
    (model/translate (mapv + [0 0 (/ z -2)] (most :position))
      (model/cube x y z))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Core Definitions — All Switches ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn all-clusters
  "The identifiers of all defined key clusters."
  [getopt]
  (remove #(= :derived %) (keys (getopt :key-clusters))))

(defn- derived
  "A shortcut to look up a cluster-specific derived configuration detail."
  [getopt & keys]
  (apply (partial getopt :key-clusters :derived :by-cluster) keys))

(defn key-requested?
  "Return true if specified key is requested."
  [getopt cluster [col-i row-i]]
  (let [matrix-cols (getopt :key-clusters cluster :matrix-columns)]
    (if-let [data (nth matrix-cols col-i nil)]
      (cond
        (< row-i 0) (>= (get data :rows-below-home 0) (abs row-i))
        (> row-i 0) (>= (get data :rows-above-home 0) row-i)
        :else true)  ; Home row.
      false)))  ; Column not in matrix.

(defn walk-cluster
  "Walk a key matrix with a cluster-specific predicate function."
  [getopt cluster]
  (matrix/trace-between (partial key-requested? getopt cluster)))

(defn chart-cluster
  "Derive some properties about a key cluster from raw configuration info."
  [cluster getopt]
  (let [raws (getopt :key-clusters cluster)
        matrix-cols (getopt :key-clusters cluster :matrix-columns)
        gather (fn [key default] (map #(get % key default) matrix-cols))
        column-range (range 0 (count matrix-cols))
        last-column (last column-range)
        max-rows-above-home (apply max (gather :rows-above-home 0))
        max-rows-below-home (apply max (gather :rows-below-home 0))
        row-range (range (- max-rows-below-home) (+ max-rows-above-home 1))
        req? (partial key-requested? getopt cluster)
        key-coordinates (matrix/coordinate-pairs column-range row-range req?)
        M (fn [f coll] (into {} (map f coll)))
        row-indices-by-column
          (M (fn [c] [c (filter #(req? [c %]) row-range)])
             column-range)
        coordinates-by-column
          (M (fn [[c rows]] [c (for [r rows] [c r])]) row-indices-by-column)
        column-indices-by-row
          (M
            (fn [r] [r (filter #(req? [% r]) column-range)])
            row-range)
        coordinates-by-row
          (M (fn [[r cols]] [r (for [c cols] [c r])]) column-indices-by-row)]
   {:style (:style raws :standard)
    :last-column last-column
    :column-range column-range
    :row-range row-range
    :key-coordinates key-coordinates
    :row-indices-by-column row-indices-by-column
    :coordinates-by-column coordinates-by-column
    :column-indices-by-row column-indices-by-row
    :coordinates-by-row coordinates-by-row}))

(defn derive-style-properties
  "Derive properties for each key style.
  These properties include DFM settings from other sections of the
  configuration, used here with their dmote-keycap names, and strings for
  OpenSCAD module names. The switch type named in the user’s configuration
  is also simplified for dmote-keycap (as :switch-type) and preserved for
  shaping the mount (as :mount-type)."
  [getopt]
  (reduce
    (fn [coll [style-key explicit]]
      (let [safe-get #(get explicit %1 (%1 capdata/option-defaults))
            mount-type (safe-get :switch-type)
            cap-compatible-type (switch-for-cap mount-type)]
        (assoc coll style-key
          (merge
            capdata/option-defaults
            {:module-keycap (str "keycap_" (misc/key-to-scadstr style-key))
             :module-switch (str "switch_" (misc/key-to-scadstr mount-type))
             :skirt-length (measure/default-skirt-length cap-compatible-type)
             :vertical-offset (measure/plate-to-stem-end cap-compatible-type)
             :error-stem-positive (getopt :dfm :keycaps :error-stem-positive)
             :error-stem-negative (getopt :dfm :keycaps :error-stem-negative)
             :error-body-positive (getopt :dfm :error-general)}
            explicit
            {:mount-type mount-type
             :switch-type cap-compatible-type}))))
    {}
    (getopt :keys :styles)))

(defn derive-cluster-properties
  "Derive basic properties for each key cluster."
  [getopt]
  (let [by-cluster (fn [coll key] (assoc coll key (chart-cluster key getopt)))]
   {:by-cluster (reduce by-cluster {} (all-clusters getopt))}))

(defn- superlative-to-index
  [coll value]
  (case value :first (first coll), :last (last coll), value))

(defn- resolve-column-superlative
  [getopt cluster column]
  (let [prop (partial getopt :key-clusters :derived :by-cluster)]
    (superlative-to-index (prop cluster :column-range) column)))

(defn- resolve-row-superlative
  [getopt {::keys [cluster column] :as selectors} row]
  (when (nil? column)
    ;; TODO: Consider deferring resolution to a later stage.
    (throw (ex-info "Relative matrix row lacks column context."
                    {:enclosing-selectors selectors
                     :target-selector-type ::row
                     :target-value row})))
  (let [prop (partial getopt :key-clusters :derived :by-cluster cluster)]
    (superlative-to-index (get (prop :row-indices-by-column) column) row)))

(defn- resolve-superlative
  [getopt {::keys [cluster] :as selectors} selector-type id]
  (if (and (#{::column ::row} selector-type) (#{:first :last} id))
    (do
      (when (nil? cluster)
        ;; TODO: As in resolve-row-superlative.
        (throw (ex-info "Relative matrix position lacks cluster context."
                        {:enclosing-selectors selectors
                         :target-selector-type selector-type
                         :target-value id})))
      (case selector-type
        ::column (resolve-column-superlative getopt cluster id)
        ::row (resolve-row-superlative getopt selectors id)))
    ;; Else there is nothing to translate.
    id))

(def heading->selector
  {:clusters ::cluster
   :columns  ::column
   :rows     ::row
   :sides    ::side})

(def nested-headings (concat [:parameters] (keys heading->selector)))

(defn- breadcrumb-to-selector
  [getopt coll [heading id]]
  (let [selector (heading heading->selector)]
    (assoc coll selector (resolve-superlative getopt coll selector id))))

(defn- breadcrumbs-to-selectors
  [getopt breadcrumbs]
  ;; TODO: Check for duplicates, e.g. two of the same column ID, or two
  ;; different column IDs. Throw ex-info.
  (reduce (partial breadcrumb-to-selector getopt) {} (partition 2 breadcrumbs)))

(defn- traverse-nested-node
  [getopt breadcrumbs selections heading]
  (let [local (apply getopt (concat [:by-key] breadcrumbs))
        value (heading local)]
    (if (nil? value)
      ;; Not a node.
      selections
      ;; Else a node.
      (if (= heading :parameters)
        ;; Expand collection. First, check for a collision.
        (let [selectors (breadcrumbs-to-selectors getopt breadcrumbs)]
          (assert selectors)
          (when-let [prior (get selections selectors)]
            (throw (ex-info (str "Key property criteria overlap")
                     {:criteria selectors
                      :settings [prior, value]})))
          (assoc selections selectors value))
        ;; Else recurse, by first looking at each of the branch IDs.
        (reduce
          (fn [selections branch]
            (reduce (partial traverse-nested-node getopt
                             (concat breadcrumbs [heading branch]))
                    selections
                    nested-headings))
          selections
          (keys value))))))  ;; Find raw branch IDs.

(defn- marshal-selectors
  "Rearrange the raw user configuration.
  Resolve non-numeric names for columns and rows and return a map of unique
  clouds of such selectors to values specific to each cloud."
  [getopt]
  (reduce (partial traverse-nested-node getopt []) {} nested-headings))

(defn- finalize-nested-structure
  "Rerrange the marshaled user configuration.
  Return a data structure nested hierarchically and with the special token
  ::any where no restriction is specified, i.e. in the absence of a selector
  for that level of the hiearchy."
  [coll selectors values]
  (let [k (mapv #(get selectors % ::any) [::cluster ::column ::row ::side])]
    (assert (nil? (get-in coll k)))
    (assoc-in coll k values)))

(defn derive-nested-properties
  [getopt]
  (reduce-kv finalize-nested-structure {} (marshal-selectors getopt)))

(defn print-matrix
  "Print a schematic picture of a key cluster. For your REPL."
  [getopt cluster]
  (let [prop (partial derived getopt cluster)]
    (doseq [row (reverse (prop :row-range)) column (prop :column-range)]
      (print (if (key-requested? getopt cluster [column row]) "□" "·"))
      (if (= column (prop :last-column)) (println)))))


;;;;;;;;;;;;;;;;;;;;;;;;;
;; Interface Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(defn single-plate
  "The shape of a key mounting plate."
  [getopt cluster coord]
  (if (most-specific getopt [:plate :use-key-style] cluster coord)
    (adaptive-plate getopt cluster coord)
    (custom-plate getopt cluster coord)))

(defn cluster-plates [getopt cluster]
  (apply model/union
    (map #(place/cluster-place getopt cluster %
            (single-plate getopt cluster %))
         (derived getopt cluster :key-coordinates))))

(defn cluster-cutouts [getopt cluster]
  (apply model/union
     (map #(place/cluster-place getopt cluster %1
             (model/call-module
               (:module-switch (key-properties getopt cluster %1))))
          (derived getopt cluster :key-coordinates))))

(defn cluster-channels [getopt cluster]
  (letfn [(modeller [coord]
            (letfn [(most [path] (most-specific getopt path cluster coord))]
              (cap-channel-negative getopt cluster coord
                {:top-width (most [:channel :top-width])
                 :height (most [:channel :height])
                 :margin (most [:channel :margin])})))]
    (apply model/union
      (map #(place/cluster-place getopt cluster % (modeller %))
           (derived getopt cluster :key-coordinates)))))

(defn cluster-keycaps [getopt cluster]
  (apply model/union
    (map #(place/cluster-place getopt cluster %
            (cap-positive getopt cluster %))
         (derived getopt cluster :key-coordinates))))

(defn metacluster
  "Apply passed modelling function to all key clusters."
  [function getopt]
  (apply maybe/union (map #(function getopt %) (all-clusters getopt))))
