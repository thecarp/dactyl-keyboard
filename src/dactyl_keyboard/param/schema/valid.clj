;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Shape Parameter Validators                                          ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; The application uses clojure.spec to check the sanity of user inputs
;;; as well as function pre- and postconditions.

(ns dactyl-keyboard.param.schema.valid
  (:require [clojure.spec.alpha :as spec]
            [scad-tarmi.core :as tarmi]
            [scad-klupe.iso :refer [head-length]]
            [dactyl-keyboard.compass :as compass]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Based on Third-Party Specs ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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


;;;;;;;;;;;;;;;;;;;;;;
;; Shady Hardcoding ;;
;;;;;;;;;;;;;;;;;;;;;;

(def built-in-bodies #{:auto :main :central-housing :wrist-rest})
(def built-in-anchors #{:origin :rear-housing-exterior :rear-housing-interior})


;;;;;;;;;;;;;;;;;;;;;;
;; Local Primitives ;;
;;;;;;;;;;;;;;;;;;;;;;

;; These are used with spec/keys, making the base names sensitive:

(spec/def ::include boolean?)
(spec/def ::positive boolean?)
(spec/def ::body keyword?)
(spec/def ::custom-body (spec/and ::body #(not (built-in-bodies %))))
(spec/def ::anchor keyword?)
(spec/def ::alias (spec/and keyword? #(not (built-in-anchors %))))
(spec/def ::key-cluster #(not (= :derived %)))
(spec/def ::segment (spec/int-in 0 5))

(spec/def :tweak/sweep ::segment)
(spec/def ::thickness (spec/and number? (complement neg?)))
(spec/def ::highlight boolean?)
(spec/def ::at-ground boolean?)
(spec/def ::above-ground boolean?)
(spec/def :tweak/chunk-size (spec/and int? #(> % 1)))
(spec/def :tweak/size ::tarmi/point-3d)
(spec/def :three/intrinsic-rotation ::tarmi/point-3d)

(spec/def :central/offset ::tarmi/point-3d)
(spec/def :central/left-hand-alias ::alias)
(spec/def :central/right-hand-alias ::alias)
(spec/def :central/starting-point keyword?)
(spec/def :central/direction-point keyword?)
(spec/def :central/lateral-offset #(not (zero? %)))
(spec/def :central/radial-offset #(not (zero? %)))
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


;;;;;;;;;;;;;;;;;;;;;;
;; Local Composites ;;
;;;;;;;;;;;;;;;;;;;;;;

;; Composites using the primitives above:

(spec/def ::spline-point
  (spec/keys :req-un [::position]  ; 2D.
             :opt-un [::alias]))
(spec/def ::nameable-spline (spec/coll-of ::spline-point))

(spec/def :central/base
  (spec/keys :req-un [:central/offset]
             :opt-un [:central/left-hand-alias :central/right-hand-alias]))
(spec/def :central/adapter
  (spec/keys :opt-un [:central/offset ::alias]))
(spec/def :central/interface-node
  (spec/keys :req-un [:central/base]
             :opt-un [:central/adapter ::at-ground ::above-ground]))
(spec/def :central/fastener-node
  (spec/keys :req-un [:central/starting-point
                      :central/lateral-offset
                      :central/radial-offset]
             :opt-un [:central/direction-point]))

(spec/def ::foot-plate (spec/keys :req-un [:two/points]))
(spec/def :two/anchoring
  (spec/keys :opt-un [::anchor :flexible/side ::segment :two/offset]))
(spec/def :three/anchoring
  (spec/keys :opt-un [::anchor :flexible/side ::segment :three/offset]))
(spec/def ::named-secondary-positions
  (spec/map-of ::alias
               (spec/keys :opt-un [:three/anchoring :three/override
                                   :three/translation :tweak/size])))
(spec/def ::anchored-2d-list (spec/coll-of :two/anchoring))
(spec/def :two/points ::anchored-2d-list)  ; Synonym for composition.
(spec/def ::projecting-2d-list (spec/coll-of
                                 (spec/and
                                   :two/anchoring
                                   (spec/keys :opt-un [:numeric/direction]))))
(spec/def ::central-housing-interface (spec/coll-of :central/interface-node))
(spec/def ::central-housing-normal-positions (spec/coll-of :central/fastener-node))
(spec/def ::mcu-grip-anchors (spec/coll-of
                               (spec/keys :req-un [::alias :intercardinal/side]
                                          :opt-un [:flexible/offset])))
(spec/def ::tweak-node (spec/or :leaf ::tweak-leaf, :branch ::tweak-branch))
(spec/def ::tweak-list (spec/coll-of ::tweak-node :min-count 1))
(spec/def :tweak/hull-around ::tweak-list)
(spec/def ::tweak-name-map (spec/map-of keyword? (spec/nilable ::tweak-list)))
(spec/def ::tweak-branch (spec/keys :req-un [:tweak/hull-around]
                                    :opt-un [::highlight :tweak/chunk-size
                                             ::above-ground
                                             ;; Additional keys expected in trees only:
                                             ::positive ::at-ground ::body]))

(spec/def ::cluster-style #{:standard :orthographic})
(spec/def ::plate-installation-style #{:threads :inserts})
(spec/def ::compass-angle-map
  (spec/map-of compass/cardinals (spec/and (complement neg?)
                                           #(<= % (* 3/8 tarmi/π)))))

(spec/def ::wrist-rest-style #{:threaded :solid})
(spec/def ::wrist-position-style #{:partner-side :mutual})
(spec/def ::wrist-block #{:partner-side :wrist-side})
(spec/def ::column-disposition
  (spec/keys ::opt-un [::rows-below-home ::rows-above-home]))
(spec/def ::flexcoord (spec/or :absolute int? :extreme #{:first :last}))
(spec/def ::tweak-leaf
  (spec/and
    (spec/keys :req-un [:three/anchoring] :opt-un [:tweak/sweep :tweak/size
                                                   :three/intrinsic-rotation])
    ;; Require a start to a sweep
    (fn [{:keys [anchoring sweep]}] (if (some? sweep)
                                        (some? (:segment anchoring))
                                        true))
    ;; Make sure the sweep ends after it starts.
    (fn [{:keys [anchoring sweep]}] (< (get anchoring :segment 0)
                                       (or sweep 5)))))
(spec/def ::foot-plate-polygons (spec/coll-of ::foot-plate))
