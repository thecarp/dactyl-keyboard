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


(defn- intersect
  [mask shapes]
  (when-not (empty? shapes)
    (model/intersection mask (apply maybe/union shapes))))

(defn above-ground
  "Implement overall limits on passed above-ground shapes.
  Cut off each body either at the level of the floor or the bottom plate. This
  is preferable to designing each body so carefully that it touches the floor
  without penetrating it in the first place."
  [getopt with-plate & shapes]
  (let [plate (if with-plate (getopt :bottom-plates :thickness) 0)]
    (intersect (maybe/translate [0 0 plate]
                 (model/translate (getopt :mask :center)
                   (apply model/cube (getopt :mask :size))))
               (remove nil? shapes))))

(defn above-main-bottom-plate
  "Choose whether to include plate height based on the main-body setting."
  [getopt & shapes]
  (apply above-ground getopt (getopt :main-body :bottom-plate :include) shapes))

(defn above-wrist-bottom-plate
  "Choose whether to include plate height based on the wrist-rest setting."
  [getopt & shapes]
  (apply above-ground getopt (getopt :wrist-rest :bottom-plate :include) shapes))

(defn main-bottom-plate
  "A rectangle or cuboid at the level of the bottom plate.
  This does not have the shape of the plate; it’s just a mask.
  It takes the central housing into account, for restricting a bottom plate
  under that feature to the centre line, on the assumption that the 3D printer
  does not have a large enough build area to print the whole thing."
  [getopt dimensions & shapes]
  {:pre [(contains? #{2 3} dimensions)]}
  (let [[x y _] (getopt :mask :size)
        z (getopt :bottom-plates :thickness)
        c (getopt :central-housing :derived :include-main)]
    (intersect (maybe/translate (take dimensions [(if c (/ x 4) 0) 0 (/ z 2)])
                 (apply (case dimensions 2 model/square model/cube)
                   (take dimensions [(if c (/ x 2) x) y z])))
               (remove nil? shapes))))

(defn at-ground
  "A 2D slice of a 3D object at z=0, restricted by the bottom-plate mask."
  [getopt & shapes]
  (when-not (empty? (remove nil? shapes))
    (main-bottom-plate getopt 2 (apply model/cut shapes))))
