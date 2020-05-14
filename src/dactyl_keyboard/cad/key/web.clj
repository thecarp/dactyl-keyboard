;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Key Webbing                                                         ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Logic for connecting switch mount plates to one another.

(ns dactyl-keyboard.cad.key.web
  (:require [scad-clj.model :as model]
            [scad-tarmi.util :refer [loft]]
            [dactyl-keyboard.cad.matrix :as matrix]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.key :as key]
            [dactyl-keyboard.compass :as compass :refer [sharp-left sharp-right]]
            [dactyl-keyboard.param.access :as access :refer [most-specific compensator]]))


(defn- bridge
  "Produce bridges between one key mount and its immediate neighbours."
  [spotter placer corner-finder coord-here]
  (let [coord-north (matrix/walk coord-here :N)
        coord-east (matrix/walk coord-here :E)
        coord-northeast (matrix/walk coord-here :N :E)
        fill-here (spotter coord-here)
        fill-north (spotter coord-north)
        fill-east (spotter coord-east)
        fill-northeast (spotter coord-northeast)]
    [;; Connecting columns.
     (when (and fill-here fill-east)
       (loft 3
         [(placer coord-here (corner-finder :ENE))
          (placer coord-east (corner-finder :WNW))
          (placer coord-here (corner-finder :ESE))
          (placer coord-east (corner-finder :WSW))]))
     ;; Connecting rows.
     (when (and fill-here fill-north)
       (loft 3
         [(placer coord-here (corner-finder :WNW))
          (placer coord-north (corner-finder :WSW))
          (placer coord-here (corner-finder :ENE))
          (placer coord-north (corner-finder :ESE))]))
     ;; Selectively filling the area between all four possible mounts.
     (loft 3
       [(when fill-here (placer coord-here (corner-finder :ENE)))
        (when fill-north (placer coord-north (corner-finder :ESE)))
        (when fill-east (placer coord-east (corner-finder :WNW)))
        (when fill-northeast (placer coord-northeast (corner-finder :WSW)))])]))

(defn cluster [getopt cluster]
  "A union of shapes covering the interstices between points in a matrix.
  The matrix models a cluster of keys."
  (apply model/union
    (mapcat
      (partial bridge
        (partial key/key-requested? getopt cluster)  ; Spotter.
        (partial place/cluster-place getopt cluster)  ; Placer.
        (fn [side]  ; Corner finder.
          {:pre [(compass/intermediates side)]}
          (let [directions (compass/intermediate-to-tuple side)
                key-style (most-specific getopt [:key-style] cluster directions)]
             (key/mount-corner-post getopt key-style side))))
      (matrix/coordinate-pairs
        (getopt :key-clusters :derived :by-cluster cluster :column-range)
        (getopt :key-clusters :derived :by-cluster cluster :row-range)))))
