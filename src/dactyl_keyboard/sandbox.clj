;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Model Sandbox                                                       ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module contains hooks for shapes not otherwise supported by the
;;; application. It is intended for personal use only.
;;;
;;; As an example usage, replace “(model/union)” in the positive and negative
;;; functions below, to make them look like this:
;;;
;;;   (defn positive
;;;     "Positive shapes, added to other stuff."
;;;     [getopt]
;;;     (model/cube 100 100 100))
;;;
;;;   (defn negative
;;;     "Negative space, subtracted from other stuff."
;;;     [getopt]
;;;     (model/cylinder 33 150))
;;;
;;; Then, run the application. A cube with a round hole in it will be
;;; added to the main body of the keyboard.

(ns dactyl-keyboard.sandbox
  (:require [scad-clj.model :as model]))

(defn positive
  "Positive shapes, added to other stuff."
  [getopt]
  (model/union))

(defn negative
  "Negative space, subtracted from other stuff."
  [getopt]
  (model/union))
