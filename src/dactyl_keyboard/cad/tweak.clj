;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Arbitrary Shapes and Tweaks                                         ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module describes models for a general system of arbitrary shapes.
;;; Tweaks, being the main use case, alter the shape of the keyboard case
;;; by connecting named features.

(ns dactyl-keyboard.cad.tweak
  (:require [clojure.spec.alpha :as spec]
            [scad-clj.model :as model]
            [scad-tarmi.core :as tarmi-core :refer [π]]
            [scad-tarmi.maybe :as maybe]
            [dactyl-keyboard.cad.auxf :as auxf]
            [dactyl-keyboard.cad.body :refer [body-plate-hull]]
            [dactyl-keyboard.cad.body.wrist :as wrist]
            [dactyl-keyboard.cad.flange :as flange]
            [dactyl-keyboard.cad.key :refer [single-plate]]
            [dactyl-keyboard.cad.key.web :refer [web-post]]
            [dactyl-keyboard.cad.mcu :as mcu]
            [dactyl-keyboard.cad.misc :as misc]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.param.access :refer [resolve-anchor]]
            [dactyl-keyboard.param.proc.anch :as anch]
            [dactyl-keyboard.param.schema.arb :as arb]))


;;;;;;;;;;;;;
;; General ;;
;;;;;;;;;;;;;

(defn- node-type [node]
  (cond
    (spec/valid? ::arb/branch node) ::branch
    (spec/valid? ::arb/leaf node) ::leaf
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

(defn- top?
  "Determine whether passed node should influence a body above the bottom
  plate."
  [{:keys [above-ground to-ground]}]
  (if (some? above-ground) above-ground (or to-ground false)))

(defn- bottom?
  "Determine whether passed node should influence a bottom plate."
  [{:keys [at-ground to-ground shadow-ground polyfill]}]
  (if (some? at-ground) at-ground (or to-ground shadow-ground polyfill false)))

(defn- projected-at-ground?
  "Determine whether passed node should be projected onto the ground plane for
  bottom plating purposes."
  [{:keys [to-ground shadow-ground] :as node}]
  (if (some? shadow-ground)
    shadow-ground
    (or (and to-ground (bottom? node)) false)))

(defn- polyfilled-at-ground?
  "Determine whether passed node should be filled onto the ground plane for
  bottom plating purposes."
  [{:keys [to-ground shadow-ground polyfill] :as node}]
  (if (some? polyfill)
    polyfill
    (and (bottom? node) (not (or to-ground shadow-ground)))))

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
  {:pre [(spec/valid? ::arb/leaf node)]
   :post [(spec/valid? (spec/coll-of ::arb/leaf) %)]}
  (if sweep
    (mapv (partial single-step-node node) (segment-range node))
    [node]))

;; Remove grove names from a forest.
(defn unfence [setting] (apply concat (vals setting)))

(defn- tweak-forest [getopt] (unfence (getopt :tweaks)))


;;;;;;;;
;; 3D ;;
;;;;;;;;

(defn- central-housing-line
  "Combine two points on the central housing into one line."
  [getopt anchor]
  (model/hull
    misc/nodule
    (model/translate
      (mapv - (place/at-named getopt {:anchor anchor, :segment 1})
              (place/at-named getopt {:anchor anchor, :segment 0}))
      misc/nodule)))

(defn- anchor-specific-shape
  "Find a 3D model associated with an anchor, or a part thereof."
  [getopt {:keys [anchor side segment]}]
  (let [{::anch/keys [type primary] :as resolved} (resolve-anchor getopt anchor)]
    (case type
      ::anch/key-mount
        (let [{:keys [cluster coordinates]} resolved]
          (if side (web-post getopt cluster coordinates side segment)
                   (single-plate getopt cluster coordinates)))
      ::anch/central-gabel
        (when-not segment (central-housing-line getopt anchor))
      ::anch/central-adapter
        (when-not segment (central-housing-line getopt anchor))
      ::anch/mcu-pcba
        (when-not (or side segment)
          (apply model/cube
                 (misc/map-to-3d-vec (getopt :mcu :derived :pcb))))
      ;; If a side of the MCU plate is specifed, put a nodule there,
      ;; else use the entire base of the plate.
      ::anch/mcu-lock-plate
        (when-not side (mcu/lock-plate-base getopt false))
      ::anch/wr-block
        (when-not (or side segment)
          (let [{:keys [mount-index block-key]} resolved]
            (wrist/block-model getopt mount-index block-key)))
      ::anch/wr-nut
        ;; Ignore side and segment as inapplicable to a nut.
        (let [{:keys [mount-index block-key fastener-index]} resolved]
          (wrist/nut getopt mount-index block-key fastener-index))
      ::anch/port-hole
        ;; With anchoring selectors, use a nodule by default for tenting the
        ;; ceiling slightly, as would be useful for DFM. Else use the whole
        ;; port, minus its flared face.
        (when-not (or side segment) (auxf/port-hole-base getopt anchor))
      ::anch/port-holder
        (if (or side segment)
          (auxf/port-tweak-post getopt primary)
          (auxf/port-holder getopt primary))
      ::anch/flange-boss
        (let [{:keys [flange position-index]} resolved]
          (if segment
            (flange/segment-model
              (getopt :flanges flange :bosses :segments segment))
            (flange/boss-model getopt flange position-index)))
      ::anch/secondary
        (let [{:keys [size]} (getopt :secondaries anchor)]
          (when size (apply model/cube size)))
      ;; Other types of anchors are never associated with a shape.
      nil)))

(defn- pick-3d-shape
  "Pick the model for a tweak.
  Use the most specific dimensions available. In decreasing order of
  preference, that is a size requested for the individual tweak, a size
  associated with the anchor, or a nodule."
  [getopt {:keys [anchoring size] :as node}]
  {:pre [(spec/valid? ::arb/leaf node)]}
  (first (filter some? [(when size (apply model/cube size))
                        (anchor-specific-shape getopt anchoring)
                        misc/nodule])))

(defn- leaf-blade-3d
  "One model, in place."
  [getopt {:keys [anchoring] :as node}]
  {:pre [(spec/valid? ::arb/leaf node)]}
  (place/at-named getopt anchoring (pick-3d-shape getopt node)))

(defn- model-leaf-3d
  "(The hull of) one or more models of one type on one side, in place, in 3D."
  [getopt node]
  {:pre [(spec/valid? ::arb/leaf node)]}
  (apply maybe/hull (map (partial leaf-blade-3d getopt) (splay node))))

(declare model-node-3d)

(defn- model-branch-3d
  [getopt bottom
   {:keys [reflect to-ground hull-around chunk-size highlight] :as node}]
  {:pre [(spec/valid? ::arb/branch node)]}
  (let [
        shapes (map (partial model-node-3d getopt bottom) hull-around)
        hull (if (or (and bottom (projected-at-ground? node))
                     (and (not bottom) to-ground))
               (partial body-plate-hull getopt (get-body getopt node))
               maybe/hull)]
    (cond->> shapes
      chunk-size (partition chunk-size 1)  ; Fragment of scad-tarmi.util/loft.
      chunk-size (map (partial apply hull))
      chunk-size (apply model/union)
      (not chunk-size) (apply hull)
      reflect misc/reflect-x
      highlight model/-#)))

(defn- model-node-3d
  "Screen a tweak node. If it’s relevant, represent it as a model."
  [getopt bottom node]
  {:pre [(spec/valid? ::arb/node node)]}
  (case (node-type node)
    ::branch (model-branch-3d getopt bottom node)
    ::leaf (model-leaf-3d getopt node)))

(defn grow
  "A user-specified shape, composed with the structure of a tweak forest."
  [getopt bottom nodes]
  (apply maybe/union (map (partial model-node-3d getopt bottom) nodes)))

(defn select
  "Filter tweaks by aligning predicate functions."
  ;; Exposed for unit testing.
  [getopt {:keys [bodies projected-at-ground polyfilled-at-ground
                  include-positive include-negative
                  include-bottom include-top]}]
  {:pre [(set? bodies) (every? keyword bodies)]}
  (filterv
    (every-pred
      (fn [node] (bodies (get-body getopt node)))
      (fn [node]
        (if (nil? projected-at-ground)
          true
          (= projected-at-ground (projected-at-ground? node))))
      (fn [node]
        (if (nil? polyfilled-at-ground)
          true
          (= polyfilled-at-ground (polyfilled-at-ground? node))))
      (fn [{:keys [cut]}]
        (or (and include-negative cut)
            (and include-positive (not cut))))
      (fn [node]
        (or (and (or (and include-bottom
                          (bottom? node))
                     (and polyfilled-at-ground
                          (polyfilled-at-ground? node))
                     (and projected-at-ground
                          (projected-at-ground? node)))
                 (not= include-top false (not (top? node))))
            (and include-top (top? node)
                 (not= include-bottom false (not (bottom? node)))))))
    (tweak-forest getopt)))

(defn union-3d
  "User-requested additional shapes for some bodies, in 3D."
  [getopt {:keys [include-top include-bottom] :as criteria}]
  (grow getopt (and (not include-top) include-bottom)
        (select getopt criteria)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 2D: Projections to the Floor ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- floor-pair
  "Produce coordinate pairs for a polygon.
  Pick just one leaf in a branch node, and just one post in a leaf, on the
  assumption that they’re not all ringing the case."
  [getopt [leaf-picker segment-picker bottom] node]
  {:pre [(spec/valid? ::arb/node node)]
   :post [(spec/valid? ::tarmi-core/point-2d %)]}
  (as-> node point
    (leaf-picker point)
    (splay point)
    (segment-picker point)  ; A single- or no-segment leaf.
    (:anchoring point)
    (assoc point :bottom bottom)  ; Amended metadata for placement.
    (place/at-named getopt point)
    (take 2 point)))  ; [x y] coordinates.

(defn- maybe-polygon
  "A single version of the footprint of a tweak.
  Tweaks so small that they amount to fewer than three vertices are ignored
  because they wouldn’t have any area."
  [getopt pickers nodes]
  {:pre [(spec/valid? ::arb/list nodes)]}
  (let [points (mapv (partial floor-pair getopt pickers) nodes)]
    (when (> (count points) 2)
      (model/polygon points))))

(defn- maybe-floor-poly
  "A sequence of polygons representing a tweak node."
  [getopt node]
  {:pre [(spec/valid? ::arb/node node)]}
  (for [leaf-picker [first last],
        segment-picker [first last],
        bottom [false true]]
       (maybe-polygon getopt
         [(partial get-leaf leaf-picker) segment-picker bottom]
         (case (node-type node)
           ::branch (:hull-around node)
           ::leaf [node]))))

(defn- floor-poly-set
  "Versions of a tweak footprint.
  This is a semi-brute-force-approach to the problem that we cannot easily
  identify which vertices shape the outside of the case at z = 0."
  [getopt node]
  {:pre [(spec/valid? ::arb/node node)]}
  (apply maybe/union (distinct (maybe-floor-poly getopt node))))

(defn union-polyfill
  "The combined footprint of user-requested additional shapes.
  By default, target the main bottom plate and use only nodes that are tagged
  for polyfill."
  [getopt criteria]
  (apply maybe/union
    (map (partial floor-poly-set getopt)
         (select getopt (merge {:bodies #{:main :central-housing}
                                :polyfilled-at-ground true
                                :include-bottom true, :include-positive true}
                               criteria)))))
