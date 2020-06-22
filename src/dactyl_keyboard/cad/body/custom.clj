;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Custom Body Utilities                                               ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.body.custom
  (:require [scad-clj.model :as model]
            [dactyl-keyboard.cad.tweak :refer [grow unfence]]))


(defn- included?
  [getopt id]
  (case id
    :main true
    :central-housing (getopt :central-housing :include)
    :wrist-rest (getopt :wrist-rest :include)
    ;; TODO: Don’t blow the stack in case of a loop.
    (included? getopt (getopt :custom-bodies id :parent-body))))

(defn collect
  "Collate information on body inclusion and relationships."
  [getopt]
  {:parent->children (group-by #(getopt :custom-bodies % :parent-body)
                               (filter (partial included? getopt)
                                       (keys (getopt :custom-bodies))))})

(defn- cut
  "Grow the mask that delimits a specific, possibly chiral, custom body."
  [getopt mirrored id]
  (let [contain (if mirrored (partial model/mirror [-1 0 0]) model/union)]
    (->> (getopt :custom-bodies id :cut) (unfence) (grow getopt) (contain))))

(defn intersection
  "Model a custom body as a positive shape, without removing child bodies."
  [getopt mirrored child-id parent-base-model]
  (model/intersection parent-base-model (cut getopt mirrored child-id)))

(defn difference
  "Model a parent body with custom bodies removed."
  [getopt mirrored child-ids parent-base-model]
  (apply model/difference parent-base-model
         (map (partial cut getopt mirrored) child-ids)))



