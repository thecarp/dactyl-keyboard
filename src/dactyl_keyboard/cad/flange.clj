;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Flanges                                                             ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; Screws and their bosses for attaching bottom plates and other loose parts
;;; of a keyboard.

(ns dactyl-keyboard.cad.flange
  (:require [clojure.set :refer [intersection]]
            [scad-clj.model :as model]
            [scad-tarmi.core :refer [π]]
            [scad-tarmi.maybe :as maybe]
            [scad-tarmi.util :refer [loft]]
            [dactyl-keyboard.cad.misc :as misc :refer [merge-bolt]]
            [dactyl-keyboard.misc :refer [key-to-scadstr]]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.param.proc.anch :as anch]))

(defn name-module
  "Name an OpenSCAD module for the negative of a flange fastener."
  [flange]
  (str "flange_" (key-to-scadstr flange) "_negative"))

(defn- item-body
  "The resolved body of a specific flange screw."
  [getopt flange position-index]
  (anch/resolve-body getopt
                     (getopt :flanges flange :body)
                     (getopt :flanges flange :positions position-index
                             :anchoring :anchor)))

(defn build-negative
  "The shape of a screw and optional insert. Threading is disabled with
  inserts, which start just above any bottom plate.
  Written for use in defining an OpenSCAD module."
  [getopt flange]
  (let [prop (partial getopt :flanges flange)
        ins (partial prop :inserts)
        base (if (prop :bottom) (getopt :bottom-plates :thickness) 0)
        gap (max 0 (- (ins :height) base))]
    (maybe/union
      (when (prop :bolts :include)
       (model/-#
        (maybe/rotate [(if (prop :bottom) π 0) 0 0]
          (merge-bolt getopt (prop :bolts :bolt-properties)
                             (when (ins :include) {:include-threading false})))))
      (when (ins :include)
        (maybe/union
          (model/translate [0 0 (+ (ins :height) (/ (ins :length) 2))]
            (model/cylinder [(/ (ins :diameter :bottom) 2)
                             (/ (ins :diameter :top) 2)]
                            (ins :length)))
          (when-not (zero? gap)
            ;; Add a channel along the screw for placing the insert.
            ;; Raise it above the bottom plate, for bottom flanges.
            ;; TODO: Parameterize this to allow placing the insert from an
            ;; arbitrary direction and distance.
            ;; TODO: Styles of inserts, including scad-klupe’s square nuts.
            (maybe/translate [0 0 (+ base (/ gap 2))]
              (model/cylinder (/ (ins :diameter :bottom) 2) gap))))))))

(defn segment-model
  "Take a boss segment configuration. Return OpenSCAD scaffolding."
  [{:keys [style diameter height]}]
  (case style
    :cylinder (model/cylinder (/ diameter 2) height)
    :sphere (model/sphere (/ diameter 2))))

(defn- segment-from-zero
  [getopt flange position-index segment]
  (maybe/translate
    (place/flange-segment-offset getopt flange position-index segment)
    (segment-model (getopt :flanges flange :bosses :segments segment))))

(defn boss-model
  "Take a boss segment configuration and range. Return OpenSCAD scaffolding."
  [getopt flange position-index]
  (let [prop (partial getopt :flanges flange)]
    (loft (map (partial segment-from-zero getopt flange position-index)
               (range 0 (inc (apply max (keys (prop :bosses :segments)))))))))

(defn- item-shape
  "Recall the shape of a complete flange screw or boss."
  [getopt flange position-index positive]
  (if positive
    (boss-model getopt flange position-index)
    (model/call-module (name-module flange))))

(defn- flatten-flanges
  "Generate a list of all unique flange names and positions therein."
  [getopt]
  (remove nil?
    (for [flange (keys (getopt :flanges))
          n (range (count (getopt :flanges flange :positions)))]
      (when (getopt :flanges flange :include)
        [flange n]))))

(defn- add-body-fn [body] (fn [bodies] (conj (or bodies #{}) body)))

(defn- map-flanges-to-bodies
  [getopt]
  (reduce (fn [coll [flange position-index]]
            (let [body (item-body getopt flange position-index)]
              (update coll flange (add-body-fn body))))
          {}
          (flatten-flanges getopt)))

(defn relevant-modules
  "Name all flange OpenSCAD modules relevant to any of named bodies.
  This does not take e.g. bottom-plate inclusion into account."
  [getopt & bodies]
  (->> (remove
         (fn [[_ relevant-set]]
           (empty? (intersection (set bodies) relevant-set)))
         (map-flanges-to-bodies getopt))
    (map first)  ; Take flange keywords.
    (set)  ; Deduplicate.
    (sort)  ; Deterministic.
    (mapv name-module)))

(defn- body-pred
  "Compose a complex function for whether a given flange screw
  belongs on a given level of any of a given set of bodies."
  [bodies]
  {:pre [(set? bodies) (every? keyword? bodies)]}
  (let [bodies (set bodies)]
    (fn [getopt include-bottom include-top [flange position-index]]
      (let [body (item-body getopt flange position-index)
            require-bottom (getopt :flanges flange :bottom)]
        (and (bodies body) (if require-bottom include-bottom include-top))))))

(defn- item-in-place
  "One model of a screw or boss, in place.
  If the item should be reflected, do so at the upper level.
  If the item should be reflected and is also negative space, assume it is
  chiral and flip it in place at the lower well as well. This second flip
  should counteract the local effects of reflection at the upper level, thus
  preserving screw threads in both copies of the item."
  [getopt positive offset reflect [flange position-index]]
  {:pre [(boolean? positive) (boolean? offset) (boolean? reflect)]}
  (let [pose (partial place/flange-place getopt flange position-index 0)
        shape (item-shape getopt flange position-index positive)]
    (maybe/translate
      ;; Conditionally raise bottom flanges as DFM.
      [0 0 (if (and offset (getopt :flanges flange :bottom))
             (getopt :dfm :bottom-plate :fastener-plate-offset)
             0)]
      (maybe/union
        (pose shape)
        (when (or reflect  ; Reflection forced by caller.
                  (getopt :flanges flange :reflect)
                  (getopt :flanges flange :positions position-index :reflect))
          (model/mirror [-1 0 0]
            (pose (if positive shape (model/mirror [-1 0 0] shape)))))))))

(defn union
  "Shapes in place for all flanges associated with one or more bodies.
  If “positive” is true, place bosses, else negative space (screws, inserts).
  Body keywords are used to make a predicate function which it then used to
  select flanges and positions in flanges."
  [positive & bodies]
  {:pre [(every? keyword? bodies)]}
  (let [base-pred (body-pred (set bodies))]
    (fn closure
      ([getopt]
       (closure getopt {}))
      ([getopt {:keys [include-bottom include-top offset-bottom reflect]
                :or {include-bottom true, include-top true,
                     offset-bottom false, reflect false}}]
       (apply maybe/union
         (map #(item-in-place getopt positive offset-bottom reflect %)
              (filter (partial base-pred getopt include-bottom include-top)
                      (flatten-flanges getopt))))))))

;; TODO: Remove the following and adopt an interface more like the tweak union
;; functions.

;; The proper selection of fasteners varies with the program output.
;; For example, as long as the bottom plate for the main body also
;; covers half of the central housing, the screw holes for the bottom
;; plate must include positions from two bodies.

(def bosses-in-main-body (union true :main))
(def bosses-in-central-housing (union true :central-housing))
(def bosses-in-wrist-rest (union true :wrist-rest))

(def holes-in-main-body (union false :main))
(def holes-in-central-housing (union false :central-housing))
(def holes-in-wrist-rest (union false :wrist-rest))
