;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Masking                                                             ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This tiny module is used to restrict the shape of each body.
;;; Owing to the design of OpenSCAD, restriction is not done with a
;;; two-dimensional plane but with a cube.

(ns dactyl-keyboard.cad.mask
  (:require [scad-clj.model :as model]
            [scad-tarmi.maybe :as maybe]))

(defn above-ground
  "Implement overall limits on passed above-ground shapes.
  Cut off each body either at the level of the floor or the bottom plate. This
  is preferable to designing each body so carefully that it touches the floor
  without penetrating it in the first place."
  [getopt with-plate & shapes]
  (let [plate (if with-plate (getopt :main-body :bottom-plate :thickness) 0)]
    (model/intersection
      (maybe/translate [0 0 plate]
        (model/translate (getopt :mask :center)
          (apply model/cube (getopt :mask :size))))
      (apply model/union shapes))))


