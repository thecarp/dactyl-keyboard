;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Miscellaneous Constants and Minor Utility Functions                 ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; These things have little to do with CAD.

(ns dactyl-keyboard.misc
  (:require [clojure.string :as string]))

(def output-directory
  "Uphold Dactyl tradition with “things” over the scad-app default directory."
  "things")

(def colours
  "OpenSCAD preview colours."
  {:cap-body [220/255 163/255 163/255 1]
   :cap-negative [0.5 0.5 1 1]
   :pcb [26/255, 90/255, 160/255 1]
   :metal [0.5 0.5 0.5 1]
   :bottom-plate [0.25 0.25 0.25 1]
   :rubber [0.5 0.5 1 1]})

(defn soft-merge
  "Merge mappings depth-first so as to retain leaves except where specifically
  overridden."
  [& maps]
  (apply (partial merge-with
           (fn [old new] (if (map? old) (soft-merge old new) new)))
         maps))

(defn key-to-scadstr [k]
  {:pre [(keyword? k)]}
  (string/replace (name k) "-" "_"))
