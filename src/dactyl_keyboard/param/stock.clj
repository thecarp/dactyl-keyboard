;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Shape Parameter Evergreens                                          ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module collects parameter metadata (packages of defaults, parsers,
;;; validators and documentation) that are useful in more than one place.

(ns dactyl-keyboard.param.stock
  (:require [scad-klupe.schema.iso :as iso]
            [dactyl-keyboard.param.schema.parse :as parse]
            [dactyl-keyboard.param.schema.valid :as valid]))


;;;;;;;;;;;;;;
;; Metadata ;;
;;;;;;;;;;;;;;


(def alias-metadata
  {:default ::placeholder :parse-fn keyword :validate [::valid/alias]})

(def anchoring-documentation
  (str "The concept of anchoring is explained "
       "[here](options-anchoring.md), "
       "along with the parameters available in this section."))

;; Screw/bolt metadata always includes a diameter and head type.
(let [defaults {:m-diameter 6, :head-type :countersunk}]
  (def explicit-threaded-bolt-metadata
    {:default defaults
     :parse-fn parse/explicit-bolt-properties
     :validate [::iso/bolt-parameter-keys]})
  (def implicit-threaded-bolt-metadata
    {:default defaults
     :parse-fn parse/implicit-bolt-properties
     :validate [::valid/comprehensive-bolt-properties]}))

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
       "The DMOTE application provides some parameters that differ from the "
       "default values in `scad-klupe` itself, in the following ways:\n"
       "\n"
       "* `negative`: The DMOTE application automatically sets this to `true` "
       "for bolt models that represent negative space.\n"
       "* `compensator`: The application automatically injects a DFM "
       "function.\n"
       "* `include-threading`: This is `true` by default in `scad-klupe` and "
       "`false` by default in the DMOTE application. The main reason for this "
       "difference is the general pattern of defaulting to false in the "
       "application for predictability. Secondary reasons are rendering "
       "performance, the option of tapping threads after printing, and the "
       "uselessness of threads in combination with heat-set inserts.\n"))

