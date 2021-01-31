;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Anchoring Schema                                                    ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; Metadata pertaining to configuration of the anchoring of features.

(ns dactyl-keyboard.param.schema.anch
  (:require [clojure.spec.alpha :as spec]
            [scad-tarmi.core :as tarmi]
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.param.base :as base]
            [dactyl-keyboard.param.schema.parse :as parse]
            [dactyl-keyboard.param.schema.valid :as valid]))

(def anchor-metadata
  {:default :origin :parse-fn keyword :validate [::valid/anchor]})
(def anchor-side-metadata
  {:default nil
   :parse-fn parse/optional-keyword
   :validate [(spec/nilable compass/all)]})
(def anchor-segment-metadata
  {:default nil
   :parse-fn (fn [candidate] (when (some? candidate) (int candidate)))
   :validate [(spec/nilable (every-pred integer? (complement neg?)))]})
(def anchor-3d-offset-metadata
  {:default [0 0 0], :parse-fn vec, :validate [::tarmi/point-3d]})
(def anchor-3d-angle-metadata
  {:default [0 0 0]
   :parse-fn (parse/tuple-of parse/compass-compatible-angle)
   :validate [::tarmi/point-3d]})

(def anchoring-raws
  "The full set of anchoring parameters, in the flat base format."
  [[]
   [[:anchor] anchor-metadata]
   [[:side] anchor-side-metadata]
   [[:segment] anchor-segment-metadata]
   [[:intrinsic-offset] anchor-3d-offset-metadata]
   [[:intrinsic-rotation] anchor-3d-angle-metadata]
   [[:extrinsic-offset] anchor-3d-offset-metadata]
   [[:extrinsic-rotation] anchor-3d-angle-metadata]
   [[:preserve-orientation] {:default false, :parse-fn boolean}]])

;; See also salient-anchoring.
(def parse-anchoring (base/parser-with-defaults anchoring-raws))
(def validate-anchoring (base/delegated-validation anchoring-raws))
(def anchoring-metadata
  "Metadata for a parameter specifying a position through anchoring.
  For use in the configuration spec."
  {:heading-template "Section %s"
   :default (parse-anchoring {})
   :parse-fn parse-anchoring
   :validate [validate-anchoring]})
(let [position (parse/map-of keyword parse-anchoring)]
  (def anchoring-map-metadata
    "A map of freely keyed positions that use only standard anchoring features.
    Ignore items with the value nil; this allows canceling out entries from
    other configuration files named earlier on the command line."
    {:freely-keyed true
     :default {}
     :parse-fn (fn [raw] (position (remove #(nil? (second %)) raw)))
     :validate [(spec/map-of ::valid/alias validate-anchoring)]}))

;; Registration:
(spec/def ::anchoring validate-anchoring)
