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
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.param.proc.anch :as anch]
            [dactyl-keyboard.param.access :refer [most-specific]]))

(defn- bridge
  "Produce bridges between one key mount and its immediate neighbours."
  [spotter placer coord-here]
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
         [(placer coord-here :NE)
          (placer coord-east :NW)
          (placer coord-here :SE)
          (placer coord-east :SW)]))
     ;; Connecting rows.
     (when (and fill-here fill-north)
       (loft 3
         [(placer coord-here :NW)
          (placer coord-north :SW)
          (placer coord-here :NE)
          (placer coord-north :SE)]))
     ;; Selectively filling the area between all four possible mounts.
     (loft 3
       [(when fill-here (placer coord-here :NE))
        (when fill-north (placer coord-north :SE))
        (when fill-east (placer coord-east :NW))
        (when fill-northeast (placer coord-northeast :SW))])]))

(defn web-post
  "The shape of a corner of a switch mount."
  ;; TODO: With the introduction of styles to key wall segments, combine this
  ;; with flange boss segment model functions.
  ([getopt cluster coord side]
   (web-post getopt cluster coord side 0))
  ([getopt cluster coord side segment]
   {:pre [(compass/all side)]}
   (apply model/cube
     (most-specific getopt [:wall :segments (or segment 0) :size] cluster coord side))))

(defn cluster
  "A union of shapes covering the interstices between points in a matrix.
  The matrix models a cluster of keys."
  [getopt cluster]
  (apply model/union
    (mapcat
      (partial bridge
        (partial key/key-requested? getopt cluster)  ; Spotter.
        (fn [coord side]  ; Placer.
          (place/by-type getopt
            {::anch/type ::anch/key-mount, :cluster cluster, :coordinates coord
             :side side, :subject (web-post getopt cluster coord side)})))
      (matrix/coordinate-pairs
        (getopt :key-clusters :derived :by-cluster cluster :column-range)
        (getopt :key-clusters :derived :by-cluster cluster :row-range)))))
