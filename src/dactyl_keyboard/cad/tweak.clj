;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Tweak Plating                                                       ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module describes models for a general system of tweaks. Tweaks alter
;;; the shape of the keyboard case by connecting named features.

(ns dactyl-keyboard.cad.tweak
  (:require [clojure.spec.alpha :as spec]
            [scad-clj.model :as model]
            [scad-tarmi.core :as tarmi-core :refer [π]]
            [scad-tarmi.maybe :as maybe]
            [dactyl-keyboard.cad.auxf :as auxf]
            [dactyl-keyboard.cad.body :refer [body-plate-hull]]
            [dactyl-keyboard.cad.body.main :as body]
            [dactyl-keyboard.cad.body.central :as central]
            [dactyl-keyboard.cad.body.wrist :as wrist]
            [dactyl-keyboard.cad.mcu :as mcu]
            [dactyl-keyboard.cad.misc :as misc]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.key :as key]
            [dactyl-keyboard.param.schema.valid :as valid]
            [dactyl-keyboard.param.access :refer [resolve-anchor]]
            [dactyl-keyboard.param.proc.anch :as anch]))


;;;;;;;;;;;;;
;; General ;;
;;;;;;;;;;;;;

(defn- node-type [node]
  (cond
    (spec/valid? ::valid/tweak-branch node) ::branch
    (spec/valid? ::valid/tweak-leaf node) ::leaf
    :else (throw (ex-info "Unclassifiable tweak node."
                   {:node node}))))

(defn- get-leaf [picker node]
  (case (node-type node)
    ::branch (get-leaf picker (-> node :hull-around picker))
    ::leaf node))

(defn- get-body
  "Retrieve a non-auto body ID for a node.
  This takes either a leaf or branch."
  [getopt node]
  (anch/resolve-body getopt
    (get node :body :auto)
    (get-in (get-leaf first node) [:anchoring :anchor])))

(defn- segment-range
  [{:keys [anchoring sweep]}]
  {:pre [(some? sweep) (some? (:segment anchoring))]}
  (range (:segment anchoring) (inc sweep)))

(defn- single-step-node
  "Simplify a leaf node to cover one part of a sweep."
  [node segment]
  (-> node (dissoc :sweep) (assoc-in [:anchoring :segment] segment)))

(defn- splay
  "Represent one leaf node as a list of one or more non-sweeping leaves."
  [{:keys [sweep] :as node}]
  {:pre [(spec/valid? ::valid/tweak-leaf node)]
   :post [(spec/valid? (spec/coll-of ::valid/tweak-leaf) %)]}
  (if sweep
    (mapv (partial single-step-node node) (segment-range node))
    [node]))

(defn- forest
  "Retrieve the complete set of nodes as one list, without names."
  [getopt]
  (apply concat (vals (getopt :tweaks))))

(defn screener
  "Compose a predicate function for filtering the forest.
  Where a value has been specified in opts, check for it."
  ; Exposed for unit testing.
  [getopt {:keys [bodies] :as opts}]
  {:pre [(seq opts)]}  ; Must not be empty, because every-pred is arity 1+.
  (let [is (fn [key default]
             (let [target (key opts)]  ; Target value from caller.
               (when (some? target)
                 (fn [node] (= (get node key default) target)))))]
    (apply every-pred
      (remove nil?
        [(is :positive true)
         (is :above-ground true)
         (is :at-ground false)
         (when bodies (fn [node] ((get-body getopt node) bodies)))]))))


;;;;;;;;
;; 3D ;;
;;;;;;;;

(defn- pick-3d-shape
  "Pick the model for a tweak. Return a tuple of that model and an indicator
  for whether the model is already in place. Positioning depends both on the
  type of anchor and secondary parameters about the target detail upon it.
  By default, use the most specific dimensions available for the post,
  defaulting to a post for key-cluster webbing."
  [getopt {:keys [anchoring size] :as node}]
  {:pre [(spec/valid? ::valid/tweak-leaf node)]}
  (let [{:keys [anchor side segment offset]} anchoring
        {::anch/keys [type primary]} (resolve-anchor getopt anchor)
        default (fn [shape] (if size (apply model/cube size) shape))]
    (case type
      ::anch/central-gabel
        [true
         (central/tweak-post getopt anchor)]
      ::anch/central-adapter
        [true
         (central/tweak-post getopt anchor)]
      ::anch/rear-housing
        [false  ; TODO: Refactor based on central-housing-like logic.
         (body/rhousing-post getopt)]
      ::anch/mcu-grip
        [false
         (default (apply model/cube (getopt :mcu :support :grip :size)))]
      ;; If a side of the MCU plate is specifed, put a nodule there,
      ;; else use the entire base of the plate.
      ::anch/mcu-lock-plate
        [false
         (if side
           (default misc/nodule)
           (default (mcu/lock-plate-base getopt false)))]
      ::anch/wr-block
        [true
         (let [{:keys [mount-index block-key]} (resolve-anchor getopt anchor)]
           (place/wrist-block-place getopt mount-index block-key
                                    side segment offset
             (if (or side segment offset)
               (default misc/nodule)
               (default (wrist/block-model getopt mount-index block-key)))))]
      ::anch/wr-nut
        ;; Ignore side and segment as inapplicable to a nut.
        [true
         (let [{:keys [mount-index block-key fastener-index]}
               (resolve-anchor getopt anchor)]
           (place/wrist-nut-place getopt mount-index block-key fastener-index
                                  offset
             (default (wrist/nut getopt mount-index block-key fastener-index))))]
      ::anch/port-hole
        [true
         (place/port-place getopt anchor
           (if (or side segment offset)
             (maybe/translate (place/port-hole-offset getopt anchoring)
               ;; Use a nodule by default for tenting the ceiling slightly,
               ;; as would be useful for DFM.
               (default misc/nodule))
             (default (auxf/port-hole-base getopt anchor))))]
      ::anch/port-holder
        [true
         (place/port-place getopt primary
           (if (or side segment offset)
             (maybe/translate (place/port-holder-offset getopt
                                (assoc anchoring :anchor primary))
               (default (auxf/port-tweak-post getopt primary)))
             (default (auxf/port-holder getopt primary))))]
      [false (default (key/web-post getopt))])))

(defn- leaf-blade-3d
  "One model at one vertical segment of one feature."
  [getopt {:keys [anchoring] :as node}]
  {:pre [(spec/valid? ::valid/tweak-leaf node)]}
  (let [[placed item] (pick-3d-shape getopt node)]
    (if placed
      item
      (place/reckon-with-anchor getopt (assoc anchoring :subject item)))))

(defn- model-leaf-3d
  "(The hull of) one or more models of one type on one side, in place, in 3D."
  [getopt node]
  {:pre [(spec/valid? ::valid/tweak-leaf node)]}
  (apply maybe/hull (map (partial leaf-blade-3d getopt) (splay node))))

(declare model-node-3d)

(defn- model-branch-3d
  [getopt {:keys [at-ground hull-around chunk-size highlight] :as node}]
  {:pre [(spec/valid? ::valid/tweak-branch node)]}
  (let [prefix (if highlight model/-# identity)
        shapes (map (partial model-node-3d getopt) hull-around)
        hull (if at-ground (partial body-plate-hull getopt
                                    (get-body getopt node))
                           maybe/hull)]
    (prefix
      (apply (if chunk-size model/union hull)
        (if chunk-size
          (map (partial apply hull) (partition chunk-size 1 shapes))
          shapes)))))

(defn- model-node-3d
  "Screen a tweak node. If it’s relevant, represent it as a model."
  [getopt node]
  {:pre [(spec/valid? ::valid/tweak-node node)]}
  (case (node-type node)
    ::branch (model-branch-3d getopt node)
    ::leaf (model-leaf-3d getopt node)))

(defn plating
  "User-requested additional shapes for some body, in 3D."
  [getopt positive body]
  (apply maybe/union
    (map (partial model-node-3d getopt)
         (filter
           (screener getopt {:positive positive,
                             :above-ground true,
                             :bodies #{body}})
           (forest getopt)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 2D: Projections to the Floor ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- floor-pair
  "Produce coordinate pairs for a polygon.
  Pick just one leaf in a branch node, and just one post in a leaf, on the
  assumption that they’re not all ringing the case."
  [getopt [leaf-picker segment-picker bottom] node]
  {:pre [(spec/valid? ::valid/tweak-node node)]
   :post [(spec/valid? ::tarmi-core/point-2d %)]}
  (as-> node point
    (leaf-picker point)
    (splay point)
    (segment-picker point)  ; A single- or no-segment leaf.
    (:anchoring point)
    (assoc point :bottom bottom)  ; Amended metadata for placement.
    (place/reckon-with-anchor getopt point)
    (take 2 point)))  ; [x y] coordinates.

(defn- maybe-polygon
  "A single version of the footprint of a tweak.
  Tweaks so small that they amount to fewer than three vertices are ignored
  because they wouldn’t have any area."
  [getopt pickers nodes]
  {:pre [(spec/valid? ::valid/tweak-list nodes)]}
  (let [points (mapv (partial floor-pair getopt pickers) nodes)]
    (when (> (count points) 2)
      (model/polygon points))))

(defn- maybe-floor-shadow
  "A sequence of polygons representing a tweak node."
  [getopt node]
  {:pre [(spec/valid? ::valid/tweak-node node)]}
  (for [leaf-picker [first last],
        segment-picker [first last],
        bottom [false true]]
       (maybe-polygon getopt
         [(partial get-leaf leaf-picker) segment-picker bottom]
         (case (node-type node)
           ::branch (:hull-around node)
           ::leaf [node]))))

(defn- floor-shadow-set
  "Versions of a tweak footprint.
  This is a semi-brute-force-approach to the problem that we cannot easily
  identify which vertices shape the outside of the case at z = 0."
  [getopt node]
  {:pre [(spec/valid? ::valid/tweak-node node)]}
  (apply maybe/union (distinct (maybe-floor-shadow getopt node))))

(defn floor-polygons
  "The combined footprint of user-requested additional shapes that go to
  the floor. No body is selected; the central housing and the main body
  share bottom plates."
  [getopt]
  (apply maybe/union
    (map (partial floor-shadow-set getopt)
         (filter (screener getopt {:at-ground true}) (forest getopt)))))

