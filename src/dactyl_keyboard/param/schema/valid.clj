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
            [dmote-keycap.schema :as capschema]
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.cots :as cots]))


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

(spec/def ::switch-type (set (keys cots/switch-facts)))
(spec/def ::comprehensive-key-style
  (spec/and ::capschema/base-parameters (spec/keys :opt-un [::switch-type])))


;;;;;;;;;;;;;;;;;;;;;;
;; Shady Hardcoding ;;
;;;;;;;;;;;;;;;;;;;;;;

(def built-in-bodies #{:auto :main :central-housing :wrist-rest})
(def built-in-anchors #{:origin :mcu-pcba
                        :rear-housing-exterior :rear-housing-interior})
(spec/def ::original #(not (= :derived %)))


;;;;;;;;;;;;;;;;;;;;;;
;; Local Primitives ;;
;;;;;;;;;;;;;;;;;;;;;;

;; These are used with spec/keys, making the base names sensitive:

(spec/def ::include boolean?)
(spec/def ::positive boolean?)
(spec/def ::body (spec/and keyword? built-in-bodies))
(spec/def ::custom-body (spec/and keyword? #(not (built-in-bodies %))))
(spec/def ::anchor keyword?)
(spec/def ::alias (spec/and keyword? #(not (built-in-anchors %))))
(spec/def ::key-cluster ::original)

(spec/def ::thickness (spec/and number? (complement neg?)))
(spec/def ::highlight boolean?)
(spec/def ::at-ground boolean?)
(spec/def ::above-ground boolean?)
(spec/def :three/intrinsic-rotation ::tarmi/point-3d)

(spec/def :central/left-hand-alias ::alias)
(spec/def :central/right-hand-alias ::alias)
(spec/def :central/starting-point keyword?)
(spec/def :central/direction-point keyword?)
(spec/def :central/axial-offset #(not (zero? %)))
(spec/def :central/radial-offset #(not (zero? %)))
(spec/def :central/intrinsic-offset ::tarmi/point-3d)
(spec/def :central/segments (spec/map-of integer?
                                         (spec/keys :req-un [:central/intrinsic-offset])))
(spec/def :numeric/direction number?)
(spec/def :intercardinal/side compass/intercardinals)
(spec/def :intermediate/side compass/intermediates)
;; TODO: Make sure the various placement functions affected by flexible/side
;; can actually take all directions, by lossy approximation where necessary.
(spec/def :flexible/side compass/all)
(spec/def :two/offset ::tarmi/point-2d)
(spec/def :three/offset ::tarmi/point-3d)
(spec/def :three/override (spec/coll-of (spec/nilable number?) :count 3))
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
  (spec/keys :req-un [:three/offset]
             :opt-un [:central/left-hand-alias :central/right-hand-alias]))
(spec/def :central/adapter
  (spec/keys :opt-un [:central/segments ::alias]))
(spec/def :central/interface-node
  (spec/keys :req-un [:central/base]
             :opt-un [:central/adapter ::at-ground ::above-ground]))
(spec/def :central/fastener-node
  (spec/keys :req-un [:central/starting-point
                      :central/axial-offset
                      :central/radial-offset]
             :opt-un [:central/direction-point]))

(spec/def ::central-housing-interface (spec/coll-of :central/interface-node))
(spec/def ::central-housing-normal-positions (spec/coll-of :central/fastener-node))
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
