;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Miscellaneous Body Utilities                                        ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.body
  (:require [scad-tarmi.maybe :as maybe]
            [dactyl-keyboard.cad.misc :refer [bottom-extrusion wafer]]))


(defn bottom-plate-hull [getopt with-plate & shapes]
  "Extend passed shapes down to the level of the bottom of the body, as
  constrained by an optional bottom plate."
  (let [plate (if with-plate (- (getopt :main-body :bottom-plate :thickness)) 0)]
    (maybe/hull
      (apply maybe/union shapes)
      (bottom-extrusion wafer
        (apply maybe/translate [0 0 plate] shapes)))))

(defn main-plate-hull [getopt & shapes]
  (apply bottom-plate-hull getopt
         (getopt :main-body :bottom-plate :include) shapes))

(defn wrist-plate-hull [getopt & shapes]
  (apply bottom-plate-hull getopt
         (getopt :wrist-rest :bottom-plate :include) shapes))

(defn body-plate-hull [getopt body & shapes]
  "Pick a bottom-plate inclusion parameter based on stated
  body affiliation. Fall back to main plate."
  (apply (case body :wrist-rest wrist-plate-hull main-plate-hull)
         getopt shapes))
