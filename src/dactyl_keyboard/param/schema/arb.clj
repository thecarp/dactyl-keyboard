;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Arbitrary Shape Schema                                              ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; Metadata pertaining to how arbitrary shapes can be specified.

;;; The title of the module is derived from the words “arbitrary” and
;;; “arboretum”, the latter from the Latin “arbor” for “tree”, trees being a
;;; source of nomenclature throughout.

(ns dactyl-keyboard.param.schema.arb
  (:require [clojure.spec.alpha :as spec]
            [scad-tarmi.core :as tarmi]
            [dactyl-keyboard.misc :refer [soft-merge]]
            [dactyl-keyboard.param.schema.parse :as parse]
            [dactyl-keyboard.param.schema.anch :as anch]))


;;;;;;;;;;;;;
;; Parsing ;;
;;;;;;;;;;;;;

(let [leaf-skeleton {:anchoring anch/parse-anchoring
                     :sweep int
                     :size parse/pad-to-3-tuple}
      branch-skeleton {:chunk-size int
                       :highlight boolean}
      top-extras {:body keyword
                  :cut boolean
                  :reflect boolean
                  :at-ground boolean
                  :above-ground boolean
                  :to-ground boolean
                  :shadow-ground boolean
                  :polyfill boolean}
      tail-leafer (parse/map-like leaf-skeleton)
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
              (nil? cnd) nil
              (and (map? cnd) (contains? cnd :hull-around)) (brancher cnd)
              (map? cnd) (leafer (destructure-leaf cnd))
              (string? (first cnd)) (leafer (apply destructure-leaf cnd))
              :else (map dispatch cnd))))
      tail-brancher (fn parse [candidate]
                      ((parse/map-like (merge branch-skeleton
                                              {:hull-around
                                               (dispatch-fn parse tail-leafer)}))
                       candidate))]
  (def parse-grove
    "Parse a configuration for arbitrary shapes.

    The grove is parsed using a pair of dispatchers, each with its own branch
    and leaf parsers. The initial dispatcher replaces itself with a secondary
    dispatcher each time it passes a branch node, and the secondary dispatcher
    sustains itself by the trick of its parser being a function that refers to
    itself and thereby passes itself along by recreating the lower-level
    dispatcher on each pass.

    A candidate to the dispatcher can be a lazy sequence describing a single
    point (a leaf), a lazy sequence of such sequences, or a map. If it is a
    map, it may contain a similar nested structure, or a predigested leaf.

    The basic leaf parser is permissive, having multiple arities where any
    positional argument can be replaced by a map. However, if a short-form
    (sequence) leaf starts with a map, the dispatcher will not identify it as a
    leaf, because of ambiguity with respect to a node list. A more stateful
    parser could handle that case."
    (parse/map-of
      keyword
      (dispatch-fn
        (parse/map-like (merge branch-skeleton top-extras
                               {:hull-around
                                (dispatch-fn tail-brancher tail-leafer)}))
        (parse/map-like (merge leaf-skeleton top-extras))))))


;;;;;;;;;;;;;;;;
;; Validation ;;
;;;;;;;;;;;;;;;;

(spec/def ::segment (spec/int-in 0 5))
(spec/def ::sweep ::segment)
(spec/def ::chunk-size (spec/and int? #(> % 1)))
(spec/def ::size ::tarmi/point-3d)
(spec/def ::node (spec/or :leaf ::leaf, :branch ::branch))
(spec/def ::list (spec/coll-of ::node :min-count 1))
(spec/def ::hull-around ::list)
(spec/def ::branch (spec/keys :req-un [::hull-around]
                              :opt-un [::highlight ::chunk-size
                                       ;; Additional keys expected in trees only:
                                       ::body ::cut ::reflect
                                       ::above-ground ::at-ground
                                       ::to-ground ::shadow-ground ::polyfill]))

(spec/def ::leaf  ;; A leaf in its parsed (unfolded) form.
  (spec/and
    (spec/keys :req-un [::anch/anchoring] :opt-un [::sweep ::size])
    ;; Require a start to a sweep.
    (fn [{:keys [anchoring sweep]}]
      (if (some? sweep) (some? (:segment anchoring)) true))
    ;; Make sure the sweep ends after it starts.
    (fn [{:keys [anchoring sweep] :or {sweep 5}}]
      (< (or (:segment anchoring) 0) sweep))))

;; High level:
(spec/def ::map (spec/map-of keyword? (spec/nilable ::list)))


;;;;;;;;;;;;;;;
;; Composite ;;
;;;;;;;;;;;;;;;

(def parameter-metadata {:freely-keyed true, :default {},
                         :parse-fn parse-grove, :validate [::map]})
