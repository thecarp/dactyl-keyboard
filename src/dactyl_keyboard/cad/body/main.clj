;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Keyboard Case Model — Main Body                                     ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; The main body is one of the predefined bodies of the application.
;;; It was once the only body, and only output.
;;; It is currently distinguished by its ability to include a rear housing, and
;;; other features not yet made general.

;;; The long-term plan is for it to have no distinguishing characteristics, at
;;; which point it will exist only as a parameter default, or disappear.

(ns dactyl-keyboard.cad.body.main
  (:require [dactyl-keyboard.cad.misc :as misc]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.param.access :as access :refer [compensator]]
            [scad-clj.model :as model]
            [scad-klupe.iso :as threaded]
            [scad-tarmi.maybe :as maybe]))


;;;;;;;;;;;;;;;;;;
;; Rear Housing ;;
;;;;;;;;;;;;;;;;;;

(defn rhousing-properties
  "Derive characteristics from parameters for the rear housing."
  [getopt]
  (let [prop (partial getopt :main-body :rear-housing)
        [_ _ z :as ext-size] (prop :size)
        ext-pos (assoc (place/at-named getopt
                         (merge (prop :anchoring)
                                {:preserve-orientation true
                                 ::place/n-dimensions 2}))
                       2 (/ z 2))
        int-pos (update ext-pos 2 #(- % (/ (prop :thickness :roof) 2)))
        ww (* 2 (prop :thickness :walls))
        int-size (mapv - ext-size [ww ww (prop :thickness :roof)])

        front (prop :bevel :exterior)]
    {:position {:exterior ext-pos
                :interior int-pos
                ;; Remaining positions are just offset from the preceding bases.
                :hollow [0 (/ (second int-size) -2) 0]
                :mask [0 (/ front 2) 0]}
     :size {:exterior ext-size
            :interior int-size
            :hollow (update int-size 1 (partial * 2))
            :mask (update ext-size 1 #(- % front))}
     :mount-width (* 2.2 (prop :fasteners :bolt-properties :m-diameter))}))

(defn- place-mount [getopt side shape]
  {:pre [(compass/cardinals side)]}
  (let [prop (partial getopt :main-body :rear-housing)
        d (prop :fasteners :bolt-properties :m-diameter)
        w (prop :derived :mount-width)
        leeway (/ (- (prop :derived :size :exterior 1) w) 2)
        position (fn [y]
                   (place/rhousing-place getopt :interior side 0
                     [(+ (prop :fasteners :sides side :offset)
                         (* (compass/delta-x (compass/reverse side)) (/ w 2)))
                      y
                      (/ (threaded/datum d :hex-nut-height) -2)]))]
   (apply model/hull
     (for [op [- +]] (model/translate (position (op leeway)) shape)))))

(defn- rhousing-mount-pair
  [getopt function]
  (apply maybe/union
    (map
      (partial function getopt)
      (filter #(getopt :main-body :rear-housing :fasteners :sides % :include)
              [:W :E]))))

(defn- rhousing-mount-positive
  [getopt side]
  {:pre [(compass/cardinals side)]}
  (let [d (getopt :main-body :rear-housing :fasteners :bolt-properties :m-diameter)
        w (getopt :main-body :rear-housing :derived :mount-width)]
   (place-mount getopt side
     (model/cube w w (threaded/datum d :hex-nut-height)))))

(defn- rhousing-mount-negative
  [getopt side]
  {:pre [(compass/cardinals side)]}
  (let [d (getopt :main-body :rear-housing :fasteners :bolt-properties :m-diameter)]
   (model/union
     (place-mount getopt side
       (model/cylinder (/ d 2) 20))
     (if (getopt :main-body :rear-housing :fasteners :bosses)
       (place-mount getopt side
         (threaded/nut {:m-diameter d
                        :compensator (compensator getopt) :negative true}))))))

(defn rear-housing-exterior
  "A single polyhedron in place.
  Exposed for use in shaping a bottom plate under the rear housing and as a
  mask for features that should be contained inside the rear housing."
  [getopt]
  (place/rhousing-place getopt :exterior nil nil
    (misc/bevelled-cuboid
      (getopt :main-body :rear-housing :derived :size :exterior)
      (getopt :main-body :rear-housing :bevel :exterior))))

(defn rear-housing-positive
  "A squarish box, open at the bottom and to the south."
  [getopt]
  (let [prop (partial getopt :main-body :rear-housing)
        fast (partial prop :fasteners)]
    (model/union
      (model/intersection
        ;; The mask.
        (model/translate (prop :derived :position :mask)
          (place/rhousing-place getopt :exterior nil nil
            (apply model/cube (prop :derived :size :mask))))
        ;; The main part of the housing:
        ;; An extra deep interior subtracted from an exterior.
        ;; Using the nominal interior model would usually leave four walls.
        (model/difference
          (rear-housing-exterior getopt)
          (model/translate (prop :derived :position :hollow)
            (place/rhousing-place getopt :interior nil nil
              (misc/bevelled-cuboid
                (prop :derived :size :hollow)
                (prop :bevel :interior))))))
      (when (fast :bosses)
        (model/intersection
          (rear-housing-exterior getopt)
          (rhousing-mount-pair getopt rhousing-mount-positive))))))

(defn rear-housing-mount-negatives
  "Negative space inside the rear housing, in place."
  [getopt]
  (rhousing-mount-pair getopt rhousing-mount-negative))
