;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Tweaks and Workarounds                                              ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module contains hooks for shapes not otherwise supported by the
;;; application. It is intended for personal use only.

;;; Remove the “#_” prefix to activate a function.

(ns dactyl-keyboard.sandbox
  (:require [scad-clj.model :as model]))

(defn positive
  "Positive shapes, added to other stuff."
  [getopt]
  #_(model/cube 100 100 100))

(defn negative
  "Negative space, subtracted from other stuff."
  [getopt]
  #_(model/cylinder 33 150))
