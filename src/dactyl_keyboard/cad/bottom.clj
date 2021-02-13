;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Bottom Plating                                                      ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.bottom
  (:require [clojure.set :as setlib]
            [scad-clj.model :as model]
            [scad-tarmi.maybe :as maybe]
            [dactyl-keyboard.cad.auxf :as auxf]
            [dactyl-keyboard.cad.body.central :as central]
            [dactyl-keyboard.cad.body.main :refer [rear-housing-exterior]]
            [dactyl-keyboard.cad.body.wrist :as wrist]
            [dactyl-keyboard.cad.flange :as flange]
            [dactyl-keyboard.cad.key :as key]
            [dactyl-keyboard.cad.key.wall :as wall]
            [dactyl-keyboard.cad.mask :as mask]
            [dactyl-keyboard.cad.misc :refer [reflect-x wafer]]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.tweak :as tweak]
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.misc :refer [colours]]
            [dactyl-keyboard.param.access :refer [most-specific compensator]]))


;;;;;;;;;;;
;; Basic ;;
;;;;;;;;;;;

(defn- to-3d
  "Build a 3D bottom plate from a 2D block."
  [getopt block]
  (model/extrude-linear
    {:height (getopt :bottom-plates :thickness), :center false}
    block))


;;;;;;;;;;
;; Case ;;
;;;;;;;;;;

(defn- wall-base-3d
  "A sliver cut from the tweaked main case wall.
  This includes tweaks that are hulled to ground, but excludes tweaks that are
  tagged up to appear in the bottom plate without being hulled to ground,
  because the latter are modelled separately and more cheaply as polygonals in
  this module."
  [getopt]
  (to-3d getopt
    (model/cut
      (key/metacluster wall/cluster getopt)
      (tweak/union-3d getopt {:include-positive true, :include-bottom true,
                              :bodies #{:main :central-housing},
                              :projected-at-ground true}))))

(defn- floor-finder
  "Make a function that takes a key mount and returns a 2D vertex
  (xy-coordinate pair) for the exterior wall of the case extending from
  that key mount."
  [getopt cluster]
  (fn [[coord side-tuple]]
    (let [side (compass/tuple-to-intermediate side-tuple)
          most #(most-specific getopt [:wall %] cluster coord side)]
      (when (most :to-ground)
        (take 2 (place/wall-corner-place getopt cluster coord
                  {:side side, :segment (most :extent), :vertex true}))))))

(defn- cluster-floor-polygon
  "A polygon approximating a floor-level projection of a key clusters’s wall."
  [getopt cluster]
  (maybe/polygon
    (filter some?  ; Get rid of edges with partial walls.
      (mapcat identity  ; Flatten floor-finder output by one level only.
        (reduce
          (fn [coll position]
            (conj coll
              (map (floor-finder getopt cluster)
                   (wall/connecting-wall position))))
          []
          (key/walk-cluster getopt cluster))))))

(defn- check-chousing-polygon
  [points]
  (when (< (count (distinct points)) 2)
    (throw (ex-info (str "Too few points at ground level in central housing. "
                         "Unable to draw bottom plate.")
             {:available-points points
              :minimum-count 2}))))

(defn- chousing-floor-polygon
  "A polygon for the body of the central housing
  This goes through all interface points at ground and then reverses through
  the most extreme of those points, shifted to x=0."
  [getopt]
  (let [gabel (central/interface getopt :at-ground [:points :at-ground :gabel])
        centre-full (sort (map #(assoc % 0 0) gabel))]
    (check-chousing-polygon gabel)
    (model/polygon (concat gabel [(last centre-full) (first centre-full)]))))

(defn- chousing-adapter-polygon
  "A polygon for the central-housing adapter."
  [getopt]
  (let [gabel (central/interface getopt :at-ground [:points :at-ground :gabel])
        adapter (central/interface getopt :at-ground [:points :at-ground :adapter])]
    (check-chousing-polygon gabel)
    (model/polygon (concat gabel (reverse adapter)))))

(defn- case-positive-2d
  "A union of polygons representing the interior of the case, including the
  central housing, when configured to appear."
  ;; Built for maintainability rather than rendering speed.
  [getopt]
  (maybe/union
    (key/metacluster cluster-floor-polygon getopt)
    (mask/at-ground getopt
      (flange/union getopt
        {:include-positive true, :include-bottom true,
         :bodies (setlib/union
                   #{:main}
                   (when (getopt :central-housing :derived :include-main)
                     #{:central-housing}))}))
    (tweak/union-polyfill getopt {})
    (when (getopt :central-housing :derived :include-main)
      (chousing-floor-polygon getopt))
    (when (getopt :central-housing :derived :include-adapter)
      (chousing-adapter-polygon getopt))
    ;; With a rear housing that connects to a regular key cluster wall, there
    ;; is a distinct possibility that two polygons (one for the housing, one
    ;; for the cluster wall) will overlap at one vertex, forming a union where
    ;; that particular intersection has no thickness.
    ;; As of 2019-04-9, OpenSCAD’s development snapshots will render a linear
    ;; extrusion from such a shape perfectly and it will produce a valid STL
    ;; that a slicer can handle.
    ;; However, if you then order some Boolean interaction between the
    ;; extrusion and any other 3D shape, while OpenSCAD will still render it,
    ;; it will not slice correctly: The mesh will be broken at the
    ;; intersection.
    (when (getopt :main-body :rear-housing :include)
      ;; To work around the problem, the rear housing floor polygon is moved a
      ;; tiny bit toward the origin, preventing the vertex overlap.
      (model/cut
        (model/translate [0 (- wafer)])
        (rear-housing-exterior getopt)))
    (when (and (getopt :wrist-rest :include)
               (= (getopt :wrist-rest :style) :threaded))
      (model/cut (wrist/all-partner-side-blocks getopt)))
    (maybe/cut (auxf/ports-positive getopt #{:main :central-housing}))))

(defn case-positive
  "A model of a bottom plate for the entire case but not the wrist rests.
  Screw holes not included."
  [getopt]
  (model/union
    (wall-base-3d getopt)
    (to-3d getopt (case-positive-2d getopt))))

(defn case-complete
  "A printable model of a case’s bottom plate in one piece."
  [getopt]
  (model/color (:bottom-plate colours)
    (maybe/difference
      (case-positive getopt)
      (flange/union getopt
        {:include-negative true, :include-bottom true,
         :bodies (setlib/union
                   #{:main}
                   (when (getopt :central-housing :derived :include-main)
                     #{:central-housing}))}))))


;;;;;;;;;;;;;;;;;
;; Wrist Rests ;;
;;;;;;;;;;;;;;;;;

(defn- wrist-positive-2d [getopt]
  (maybe/union
    (model/cut (wrist/projection-maquette getopt))
    (model/cut (flange/union getopt
                 {:include-positive true, :include-bottom true,
                  :bodies (setlib/union #{:wrist-rest})}))
    (tweak/union-polyfill getopt {:bodies #{:wrist-rest}})))

(defn wrist-positive
  "3D wrist-rest bottom plate without screw holes."
  [getopt]
  (to-3d getopt (wrist-positive-2d getopt)))

(defn wrist-complete
  "A printable model of a wrist-rest bottom plate in one piece."
  [getopt]
  (model/color (:bottom-plate colours)
    (maybe/difference
      (wrist-positive getopt)
      (flange/union getopt
        {:include-negative true, :include-bottom true,
         :bodies (setlib/union #{:wrist-rest})}))))


;;;;;;;;;;;;;;;;;;;;;
;; Combined Plates ;;
;;;;;;;;;;;;;;;;;;;;;

(defn- lateral-positive
  "A combined bottom plate for everything but the central housing.
  Where a wrist rest is included, this function assumes the use of a solid or
  threaded wrist rest and will attach its bottom plate to the main body’s.
  Where plates abut or overlap, this will obviously prevent the use of threaded
  fasteners in adjusting the wrist rest."
  [getopt]
  (model/union
    (wall-base-3d getopt)
    (to-3d getopt
      (maybe/union
        (case-positive-2d getopt)
        (when (and (getopt :wrist-rest :include)
                   (getopt :wrist-rest :bottom-plate :include))
          (wrist/hulled-block-pairs getopt)
          (wrist-positive-2d getopt))))))

(defn- bilateral
  "Fit passed shape onto both sides of a central housing, if any."
  [getopt shape]
  (if (getopt :central-housing :derived :include-main)
    (reflect-x shape)
    shape))

(defn combined-positive
  "The positive space of a combined bottom plate: One plate that extends to
  cover the central housing, if that’s included, and/or the wrist rests, if
  they’re included."
  [getopt]
  (bilateral getopt (lateral-positive getopt)))

(defn combined-complete
  "A combined, possibly bilateral plate with holes."
  [getopt]
  (model/color (:bottom-plate colours)
    (maybe/difference
      (combined-positive getopt)
      (flange/union getopt
        {:reflect (getopt :central-housing :derived :include-main)
         :include-bottom true, :include-negative true,
         :bodies (setlib/union #{:main} (when (getopt :wrist-rest :include)
                                          #{:wrist-rest}))})
      (when (getopt :central-housing :derived :include-main)
        (flange/union getopt
          {:include-bottom true, :include-negative true,
           :bodies #{:central-housing}})))))
