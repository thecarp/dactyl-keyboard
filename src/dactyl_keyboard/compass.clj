;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; The Compass Metaphor                                                ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module defines the metaphor of a compass as a user-friendly way
;;; to refer to parts of and directions away from features of the keyboard,
;;; in the plane of each feature.

(ns dactyl-keyboard.compass)

(def unordered-corners
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

