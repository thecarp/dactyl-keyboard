;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Constants and Minor Utility Functions                               ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; These are potentially useful in parameters and have very little to do
;;; with CAD or the keyboard.

(ns dactyl-keyboard.generics
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

(def unorderered-corners
  "Keywords are provided for the four corners of a feature in the compass-based
  metaphor describing the plane of that feature."
  [:NE :SE :SW :NW])

;; Each switch mount has four corners with offsets in two directions,
;; but these corners need to be more specific than the unordered corners above.
;; Capitals in symbol names are reserved for these shorthand definitions
;; of the four corners. In each case, the cardinal direction naming the side
;; of the key comes first. The second item names one end of that side.
(def NNE [:north :east])  ; North by north-east.
(def ENE [:east :north])
(def SSE [:south :east])
(def ESE [:east :south])
(def SSW [:south :west])
(def WSW [:west :south])
(def NNW [:north :west])
(def WNW [:west :north])

(def keyword-to-directions
  "Decode sets of directions from configuration data."
  {:NNE NNE
   :ENE ENE
   :SSE SSE
   :ESE ESE
   :SSW SSW
   :WSW WSW
   :NNW NNW
   :WNW WNW})

(def all-corner-keywords
  "The union of unordered and ordered corner identifiers as a vector of
  keywords, clockwise around the compass."
  [:NNE :NE :ENE :ESE :SE :SSE :SSW :SW :WSW :WNW :NW :NNW])

(defn directions-to-unordered-corner
  "Reduce directional corner code to non-directional corner code, as
  used for rear housing."
  [tuple]
  (cond
    (#{NNE ENE} tuple) :NE
    (#{SSE ESE} tuple) :SE
    (#{SSW WSW} tuple) :SW
    (#{NNW WNW} tuple) :NW))

(def unordered-corner-to-x-ordered
  "Favor the x axis when converting up from an unordered corner keyword
  to an ordered corner keyword."
  {:NE :ENE, :SE :ESE, :SW :WSW, :NW :WNW})

(defn soft-merge
  "Merge mappings depth-first so as to retain leaves except where specifically
  overridden."
  [& maps]
  (apply (partial merge-with
           (fn [old new] (if (map? old) (soft-merge old new) new)))
         maps))

(defn key-to-scadstr [k] (string/replace (name k) "-" "_"))
