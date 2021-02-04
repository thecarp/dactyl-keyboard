;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Shape Parameter Enrichment                                          ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module connects feature-specific functionality from all over the
;;; application to enrich the user’s chosen configuration with additional data
;;; derived from it.

(ns dactyl-keyboard.param.rich
  (:require [dactyl-keyboard.misc :refer [soft-merge]]
            [dactyl-keyboard.param.access :as access]
            [dactyl-keyboard.param.proc.anch :as anch]
            [dactyl-keyboard.cad.body.custom :as custom-body]
            [dactyl-keyboard.cad.body.main :as main-body]
            [dactyl-keyboard.cad.body.central :as central]
            [dactyl-keyboard.cad.body.wrist :as wrist]
            [dactyl-keyboard.cad.key :as key]
            [dactyl-keyboard.cad.mcu :as mcu]))


;;;;;;;;;;;;;;;
;; Interface ;;
;;;;;;;;;;;;;;;


(def derivers-static
  "A vector of configuration locations and functions for expanding them."
  ;; Mind the order. One of these may depend upon earlier steps.
  [[[:keys] key/derive-style-properties]
   [[:key-clusters] key/derive-cluster-properties]
   [[:by-key] key/derive-nested-properties]
   [[:central-housing] central/derive-properties]
   [[] (fn [getopt] {:anchors (anch/collect getopt)
                     :bodies (custom-body/collect getopt)})]
   [[:main-body :rear-housing] main-body/rhousing-properties]
   [[:mcu] mcu/derive-properties]
   [[:wrist-rest] wrist/derive-properties]])

(defn derivers-dynamic
  "Additions for more varied parts of a configuration."
  [getopt]
  (for [i (range (count (getopt :wrist-rest :mounts)))]
       [[:wrist-rest :mounts i] #(wrist/derive-mount-properties % i)]))

(defn enrich-option-metadata
  "Derive certain properties that are implicit in the user configuration.
  Use a gradually expanding but temporary build option accessor.
  Store the results under the “:derived” key in each section."
  [build-options]
  (reduce
    (fn [coll [path callable]]
      (soft-merge
        coll
        (assoc-in coll (conj path :derived)
                       (callable (access/option-accessor coll)))))
    build-options
    (concat derivers-static
            (derivers-dynamic (access/option-accessor build-options)))))
