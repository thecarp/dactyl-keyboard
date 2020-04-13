;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Shape Parameter Evergreens                                          ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module collects parameter metadata (packages of defaults, parsers,
;;; validators and documentation) that are useful in more than one place.

(ns dactyl-keyboard.param.stock
  (:require [scad-klupe.schema.iso :as iso]
            [scad-tarmi.core :as tarmi]
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.param.schema :as schema]))


;;;;;;;;;;;;;;
;; Metadata ;;
;;;;;;;;;;;;;;


(def anchoring-documentation
  (str "The concept of anchoring is explained [here](configuration.md)."))

(def anchor-metadata
  {:default :origin :parse-fn keyword :validate [::schema/anchor]})
(def anchor-documentation
  (str "A code identifying an anchor point. This can be the default value "
       "(`origin`) or a name (built-in or alias) identifying a feature."))

(def anchor-side-metadata
  {:default :N, :parse-fn schema/any-compass-point
   :validate [compass/all-short]})
(def anchor-side-documentation
  (str "A compass-point code for one side of the feature named in `anchor`. "
       "The default is `N`, signifying the north side."))

(def anchor-segment-metadata
  {:default 0, :parse-fn num, :validate [integer? (complement neg?)]})
(def anchor-segment-documentation
  (str "An integer identifying one vertical segment of the feature "
       "named in `anchor`. The default is `0`, signifying the topmost "
       "part of the anchor."))

(def anchor-2d-vector-metadata
  {:default [0 0], :parse-fn vec, :validate [::tarmi/point-2d]})
(def anchor-3d-vector-metadata
  {:default [0 0 0], :parse-fn vec, :validate [::tarmi/point-3d]})
(def anchor-2d-offset-documentation
  "A two-dimensional offset in mm from the feature named in `anchor`.")
(def anchor-3d-offset-documentation
  (str "A three-dimensional offset in mm from the feature named in `anchor`. "
       "This is applied in the anchor’s local frame of reference "
       "and may therefore be subject to various rotations etc."))

;; Screw/bolt metadata always includes a diameter and head type.
(let [defaults {:m-diameter 6, :head-type :countersunk}]
  (def explicit-threaded-bolt-metadata
    {:default defaults
     :parse-fn schema/explicit-bolt-properties
     :validate [::iso/bolt-parameter-keys]})
  (def implicit-threaded-bolt-metadata
    {:default defaults
     :parse-fn schema/implicit-bolt-properties
     :validate [::schema/comprehensive-bolt-properties]}))

(def threaded-bolt-documentation
  (str "This parameter describes the properties of a screw or bolt. "
       "It takes a mapping appropriate for the `bolt` function in the "
       "[`scad-klupe.iso`](https://github.com/veikman/scad-klupe) library.\n\n"
       "The following describes only a subset of what you can include here:\n"
       "\n"
       "* `m-diameter`: The ISO metric diameter of a bolt, e.g. `6` for M6.\n"
       "* `head-type`: A keyword describing the head of the bolt, such as "
       "`hex` or `countersunk`.\n"
       "* `total-length`: The length of the threaded part of the bolt, in "
       "mm.\n"
       "\n"
       "Default values provided by the application are bare minima. More "
       "usefully, the application injects DFM functions and flags negative "
       "space for specific uses."))

