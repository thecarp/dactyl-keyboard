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
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.misc :as misc]
            [dactyl-keyboard.cad.matrix :as matrix]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.key.switch :refer [cap-channel-negative
                                                    cap-positive]]
            [dactyl-keyboard.param.access :refer [most-specific key-properties
                                                  compensator]]))


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
  OpenSCAD module names."
  [getopt]
  (reduce
    (fn [coll [style-key explicit]]
      (let [safe-get #(get explicit %1 (%1 capdata/option-defaults))
            switch-type (safe-get :switch-type)]
        (assoc coll style-key
          (merge
            capdata/option-defaults
            {:module-keycap (str "keycap_" (misc/key-to-scadstr style-key))
             :module-switch (str "switch_" (misc/key-to-scadstr switch-type))
             :skirt-length (measure/default-skirt-length switch-type)
             :vertical-offset (measure/plate-to-stem-end switch-type)
             :error-stem-positive (getopt :dfm :keycaps :error-stem-positive)
             :error-stem-negative (getopt :dfm :keycaps :error-stem-negative)
             :error-body-positive (getopt :dfm :error-general)}
            explicit))))
    {}
    (getopt :keys :styles)))

(defn derive-cluster-properties
  "Derive basic properties for each key cluster."
  [getopt]
  (let [by-cluster (fn [coll key] (assoc coll key (chart-cluster key getopt)))]
   {:by-cluster (reduce by-cluster {} (all-clusters getopt))}))

(defn print-matrix
  "Print a schematic picture of a key cluster. For your REPL."
  [getopt cluster]
  (let [prop (partial derived getopt cluster)]
    (doseq [row (reverse (prop :row-range)) column (prop :column-range)]
      (print (if (key-requested? getopt cluster [column row]) "□" "·"))
      (if (= column (prop :last-column)) (println)))))


;;;;;;;;;;;;;;;;;;
;; Other Models ;;
;;;;;;;;;;;;;;;;;;

(defn- single-plate
  "The shape of a key mounting plate."
  [getopt key-style]
  (let [thickness (getopt :main-body :key-mount-thickness)
        style-data (getopt :keys :derived key-style)
        [x y] (map measure/key-length (get style-data :unit-size [1 1]))]
   (model/translate [0 0 (/ thickness -2)]
     (model/cube x y thickness))))

(defn web-post
  "A shape for attaching things to a corner of a switch mount."
  [getopt]
  (model/cube (getopt :main-body :key-mount-corner-margin)
              (getopt :main-body :key-mount-corner-margin)
              (getopt :main-body :web-thickness)))

(defn mount-corner-post
  "A post shape that comes offset for one corner of a key mount."
  [getopt key-style side]
  {:pre [(compass/intermediates side)]}
  (->> (web-post getopt)
       (model/translate (place/mount-corner-offset getopt key-style side))))


;;;;;;;;;;;;;;;;;;;;;;;;;
;; Interface Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(defn cluster-plates [getopt cluster]
  (apply model/union
    (map #(place/cluster-place getopt cluster %
            (single-plate getopt
              (most-specific getopt [:key-style] cluster %)))
         (derived getopt cluster :key-coordinates))))

(defn cluster-cutouts [getopt cluster]
  (apply model/union
     (map #(place/cluster-place getopt cluster %1
             (model/call-module
               (:module-switch (key-properties getopt cluster %1))))
          (derived getopt cluster :key-coordinates))))

(defn cluster-channels [getopt cluster]
  (letfn [(modeller [coord]
            (letfn [(most [path]
                      (most-specific getopt path cluster coord))]
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
