;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Shape Parameter Evergreens                                          ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module collects parameter metadata (packages of defaults, parsers,
;;; validators and documentation) that are useful in more than one place.

(ns dactyl-keyboard.param.stock
  (:require [scad-klupe.schema.iso :as iso]
            [dactyl-keyboard.param.schema :as schema]))


;;;;;;;;;;;;;;
;; Metadata ;;
;;;;;;;;;;;;;;

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

