;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Keyboard Case Model                                                 ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.body
  (:require [scad-clj.model :as model]
            [scad-tarmi.core :refer [mean]]
            [scad-tarmi.maybe :as maybe]
            [scad-tarmi.threaded :as threaded]
            [scad-tarmi.util :refer [loft]]
            [dactyl-keyboard.cad.central :as central]
            [dactyl-keyboard.cad.mcu :as mcu]
            [dactyl-keyboard.cad.misc :as misc]
            [dactyl-keyboard.cad.matrix :as matrix]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.key :as key]
            [dactyl-keyboard.compass :as compass :refer [sharp-left sharp-right]]
            [dactyl-keyboard.param.access :as access :refer [most-specific
                                                             tweak-data]]))


;;;;;;;;;;;;;
;; Masking ;;
;;;;;;;;;;;;;

(defn mask
  "Implement overall limits on passed shapes."
  [getopt with-plate & shapes]
  (let [plate (if with-plate (getopt :case :bottom-plate :thickness) 0)]
    (model/intersection
      (maybe/translate [0 0 plate]
        (model/translate (getopt :mask :center)
          (apply model/cube (getopt :mask :size))))
      (apply model/union shapes))))


;;;;;;;;;;;;;;;;;;;;;;;
;; Key Mount Webbing ;;
;;;;;;;;;;;;;;;;;;;;;;;

;; This connects switch mount plates to one another.

(defn web-shapes
  "A vector of shapes covering the interstices between points in a matrix."
  [coordinate-sequence spotter placer corner-finder]
  (loop [remaining-coordinates coordinate-sequence
         shapes []]
    (if (empty? remaining-coordinates)
      shapes
      (let [coord-here (first remaining-coordinates)
            coord-north (matrix/walk coord-here :N)
            coord-east (matrix/walk coord-here :E)
            coord-northeast (matrix/walk coord-here :N :E)
            fill-here (spotter coord-here)
            fill-north (spotter coord-north)
            fill-east (spotter coord-east)
            fill-northeast (spotter coord-northeast)]
       (recur
         (rest remaining-coordinates)
         (conj
           shapes
           ;; Connecting columns.
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
              (when fill-northeast (placer coord-northeast (corner-finder :WSW)))])))))))

(defn walk-and-web [columns rows spotter placer corner-finder]
  (web-shapes (matrix/coordinate-pairs columns rows) spotter placer corner-finder))

(defn cluster-web [getopt cluster]
  (apply model/union
    (walk-and-web
      (getopt :key-clusters :derived :by-cluster cluster :column-range)
      (getopt :key-clusters :derived :by-cluster cluster :row-range)
      (getopt :key-clusters :derived :by-cluster cluster :key-requested?)
      (partial place/cluster-place getopt cluster)
      (fn [corner]  ; A corner finder.
        {:pre [(compass/intermediates corner)]}
        (let [directions (compass/intermediate-to-tuple corner)
              key-style (most-specific getopt [:key-style] cluster directions)]
           (key/mount-corner-post getopt key-style corner))))))


;;;;;;;;;;;;;;;;;;;
;; Wall-Building ;;
;;;;;;;;;;;;;;;;;;;

;; Functions for specifying parts of a perimeter wall. These all take the
;; edge-walking algorithm’s map output with position and direction, upon
;; seeing the need for each part.

(defn wall-straight-body
  "The part of a case wall that runs along the side of a key mount on the
  edge of the board."
  [{:keys [coordinates direction]}]
  (let [facing (sharp-left direction)]
    [[coordinates facing sharp-right]
     [coordinates facing sharp-left]]))

(defn wall-straight-join
  "The part of a case wall that runs between two key mounts in a straight line."
  [{:keys [coordinates direction]}]
  (let [next-coord (matrix/walk coordinates direction)
        facing (sharp-left direction)]
    [[coordinates facing sharp-right]
     [next-coord facing sharp-left]]))

(defn wall-outer-corner
  "The part of a case wall that smooths out an outer, sharp corner."
  [{:keys [coordinates direction]}]
  (let [original-facing (sharp-left direction)]
    [[coordinates original-facing sharp-right]
     [coordinates direction sharp-left]]))

(defn wall-inner-corner
  "The part of a case wall that covers any gap in an inner corner.
  In this case, it is import to pick not only the right corner but the right
  direction moving out from that corner."
  [{:keys [coordinates direction]}]
  (let [opposite (matrix/walk coordinates (sharp-left direction) direction)]
    [[coordinates (sharp-left direction) (constantly direction)]
     [opposite (compass/reverse direction) sharp-left]]))

(defn connecting-wall
  [{:keys [corner] :as position}]
  (case corner
    :outer (wall-outer-corner position)
    nil (wall-straight-join position)
    :inner (wall-inner-corner position)))

;; Edge walking.

(defn wall-edge-post
  "Run wall-edge-sequence with a web post as its subject."
  [getopt cluster upper edge]
  (place/wall-edge-sequence getopt cluster upper edge (key/web-post getopt)))

(defn wall-slab
  "Produce a single shape joining some (two) edges."
  [getopt cluster edges]
  (let [upper (map (partial wall-edge-post getopt cluster true) edges)
        lower (map (partial wall-edge-post getopt cluster false) edges)]
   (model/union
     (apply model/hull upper)
     (apply misc/bottom-hull lower))))

(defn cluster-wall
  "Walk the edge of a key cluster, walling it in."
  [getopt cluster]
  (apply model/union
    (reduce
      (fn [coll position]
        (conj coll
          (wall-slab getopt cluster (wall-straight-body position))
          (wall-slab getopt cluster (connecting-wall position))))
      []
      (matrix/trace-between
        (getopt :key-clusters :derived :by-cluster cluster :key-requested?)))))


;;;;;;;;;;;;;;;;;;
;; Rear Housing ;;
;;;;;;;;;;;;;;;;;;

(defn- rhousing-post [getopt]
  (let [xy (getopt :case :rear-housing :wall-thickness)]
    (model/cube xy xy (getopt :case :rear-housing :roof-thickness))))

(defn- rhousing-height
  "The precise height of (the center of) each top-level rhousing-post."
  [getopt]
  (- (getopt :case :rear-housing :height)
     (/ (getopt :case :rear-housing :roof-thickness) 2)))

(defn rhousing-properties
  "Derive characteristics from parameters for the rear housing."
  [getopt]
  (let [cluster (getopt :case :rear-housing :position :cluster)
        key-style (fn [coord] (most-specific getopt [:key-style] cluster coord))
        row (last (getopt :key-clusters :derived :by-cluster cluster :row-range))
        coords (getopt :key-clusters :derived :by-cluster cluster :coordinates-by-row row)
        pairs (into [] (for [coord coords, corner [:NNW :NNE]] [coord corner]))
        getpos (fn [[coord corner]]
                 (place/cluster-place getopt cluster coord
                   (place/mount-corner-offset getopt (key-style coord) corner)))
        y-max (apply max (map #(second (getpos %)) pairs))
        getoffset (partial getopt :case :rear-housing :position :offsets)
        y-roof-s (+ y-max (getoffset :south))
        y-roof-n (+ y-roof-s (getoffset :north))
        z (rhousing-height getopt)
        roof-sw [(- (first (getpos (first pairs))) (getoffset :west)) y-roof-s z]
        roof-se [(+ (first (getpos (last pairs))) (getoffset :east)) y-roof-s z]
        roof-nw [(first roof-sw) y-roof-n z]
        roof-ne [(first roof-se) y-roof-n z]]
   {:west-end-coord (first coords)
    :east-end-coord (last coords)
    :coordinate-corner-pairs pairs
    ;; [x y z] coordinates of the corners of the topmost part of the roof:
    :SW roof-sw
    :SE roof-se
    :NW roof-nw
    :NE roof-ne}))

(defn- rhousing-roof
  "A cuboid shape between the four corners of the rear housing’s roof."
  [getopt]
  (let [getcorner (partial getopt :case :rear-housing :derived)]
    (apply model/hull
      (map #(maybe/translate (getcorner %) (rhousing-post getopt))
           [:NW :NE :SE :SW]))))

(defn rhousing-pillar-functions
  "Make functions that determine the exact positions of rear housing walls.
  This is an awkward combination of reckoning functions for building the
  bottom plate in 2D and placement functions for building the case walls in
  3D. Because they’re specialized, the ultimate return values are disturbingly
  different."
  [getopt]
  (let [cluster (getopt :case :rear-housing :position :cluster)
        cluster-pillar
          (fn [coord-key cardinal rhousing-turning-fn cluster-turning-fn]
            ;; Make a function for a part of the cluster wall.
            (fn [reckon upper]
              (let [coord (getopt :case :rear-housing :derived coord-key)
                    subject (if reckon [0 0 0] (key/web-post getopt))
                    ;; For reckoning, return a 3D coordinate vector.
                    ;; For building, return a sequence of web posts.
                    picker (if reckon #(first (take-last 2 %)) identity)]
                (picker
                  (place/wall-edge-sequence getopt cluster upper
                    [coord cardinal rhousing-turning-fn] subject)))))
        rhousing-pillar
          (fn [opposite corner]
            ;; Make a function for a part of the rear housing.
            ;; For reckoning, return a 3D coordinate vector.
            ;; For building, return a hull of housing cubes.
            {:pre [(compass/intermediates corner)]}
            (fn [reckon upper]
              (let [subject (if reckon
                              (place/rhousing-vertex-offset getopt
                                (if opposite (compass/reverse corner) corner))
                              (rhousing-post getopt))]
                (apply (if reckon mean model/hull)
                  (map #(place/rhousing-place getopt corner % subject)
                       (if upper [0 1] [1]))))))]
    [(cluster-pillar :west-end-coord :W sharp-right sharp-left)
     (rhousing-pillar true :WSW)
     (rhousing-pillar false :WNW)
     (rhousing-pillar false :NNW)
     (rhousing-pillar false :NNE)
     (rhousing-pillar false :ENE)
     (rhousing-pillar true :ESE)
     (cluster-pillar :east-end-coord :E sharp-left sharp-right)]))

(defn- rhousing-wall-shape-level
  "The west, north and east walls of the rear housing with connections to the
  ordinary case wall."
  [getopt is-upper-level joiner]
  (loft
    (reduce
      (fn [coll function] (conj coll (joiner (function false is-upper-level))))
      []
      (rhousing-pillar-functions getopt))))

(defn- rhousing-outer-wall
  "The complete walls of the rear housing: Vertical walls and a bevelled upper
  level that meets the roof."
  [getopt]
  (model/union
    (rhousing-wall-shape-level getopt true identity)
    (rhousing-wall-shape-level getopt false misc/bottom-hull)))

(defn- rhousing-web
  "An extension of a key cluster’s webbing onto the roof of the rear housing."
  [getopt]
  (let [cluster (getopt :case :rear-housing :position :cluster)
        key-style (fn [coord] (most-specific getopt [:key-style] cluster coord))
        pos-corner (fn [coord corner]
                     (place/cluster-place getopt cluster coord
                       (place/mount-corner-offset getopt (key-style coord) corner)))
        sw (getopt :case :rear-housing :derived :SW)
        se (getopt :case :rear-housing :derived :SE)
        x (fn [coord corner]
            (max (first sw)
                 (min (first (pos-corner coord corner))
                      (first se))))
        y (second sw)
        z (rhousing-height getopt)]
   (loft
     (reduce
       (fn [coll [coord corner]]
         (conj coll
           (model/hull
             (place/cluster-place getopt cluster coord
               (key/mount-corner-post getopt (key-style coord) corner))
             (model/translate [(x coord corner) y z]
               (rhousing-post getopt)))))
       []
       (getopt :case :rear-housing :derived :coordinate-corner-pairs)))))

(defn- rhousing-mount-place [getopt side shape]
  {:pre [(compass/cardinals side)]}
  (let [d (getopt :case :rear-housing :fasteners :diameter)
        offset (getopt :case :rear-housing :fasteners
                 (side compass/short-to-long) :offset)
        n (getopt :case :rear-housing :position :offsets :north)
        t (getopt :case :rear-housing :roof-thickness)
        h (threaded/datum d :hex-nut-height)
        [sign base] (case side
                      :W [+ (getopt :case :rear-housing :derived :SW)]
                      :E [- (getopt :case :rear-housing :derived :SE)])
        near (mapv + base [(+ (- (sign offset)) (sign d)) d (/ (+ t h) -2)])
        far (mapv + near [0 (- n d d) 0])]
   (model/hull
     (model/translate near shape)
     (model/translate far shape))))

(defn- rhousing-mount-positive [getopt side]
  {:pre [(compass/cardinals side)]}
  (let [d (getopt :case :rear-housing :fasteners :diameter)
        w (* 2.2 d)]
   (rhousing-mount-place getopt side
     (model/cube w w (threaded/datum d :hex-nut-height)))))

(defn- rhousing-mount-negative [getopt side]
  {:pre [(compass/cardinals side)]}
  (let [d (getopt :case :rear-housing :fasteners :diameter)
        compensator (getopt :dfm :derived :compensator)]
   (model/union
     (rhousing-mount-place getopt side
       (model/cylinder (/ d 2) 20))
     (if (getopt :case :rear-housing :fasteners :bosses)
       (rhousing-mount-place getopt side
         (threaded/nut :iso-size d :compensator compensator :negative true))))))

(defn rear-housing
  "A squarish box at the far end of a key cluster."
  [getopt]
  (let [prop (partial getopt :case :rear-housing :fasteners)
        pair (fn [function]
               (model/union
                 (if (prop :west :include) (function getopt :W))
                 (if (prop :east :include) (function getopt :E))))]
   (model/difference
     (model/union
       (rhousing-roof getopt)
       (rhousing-web getopt)
       (rhousing-outer-wall getopt)
       (if (prop :bosses) (pair rhousing-mount-positive)))
     (pair rhousing-mount-negative))))


;;;;;;;;;;;;;;;;;;;
;; Tweak Plating ;;
;;;;;;;;;;;;;;;;;;;


(defn- tweak-posts
  "(The hull of) one or more corner posts for a case tweak.
  This function both picks the shape of the post and positions it.
  For a tweak anchored to the central housing, defer to the central module
  for both shape and position, because they are closely related in that case.
  Otherwise use the most specific dimensions available for the post, defaulting
  to a web post."
  [getopt anchor corner first-segment last-segment]
  (if (= first-segment last-segment)
    (let [type (:type (access/resolve-anchor getopt anchor))
          post (case type
                 :central-housing (central/tweak-post getopt anchor)
                 :rear-housing (rhousing-post getopt)
                 :mcu-grip (apply model/cube (getopt :mcu :support :grip :size))
                 ;; If a corner of the MCU plate is specifed, put a nodule there,
                 ;; else use the entire base of the plate.
                 :mcu-lock-plate (if corner
                                   misc/nodule
                                   (mcu/lock-plate-base getopt false))
                 (key/web-post getopt))]
      (if (= type :central-housing)
        ;; High-precision anchor; reckon-from-anchor is inadequate.
        post
        ;; Low-precision anchor.
        (place/reckon-from-anchor getopt anchor
          {:subject post, :corner corner, :segment first-segment})))
    (apply model/hull (map #(tweak-posts getopt anchor corner %1 %1)
                           (range first-segment (inc last-segment))))))

(declare tweak-plating)

(defn- tweak-map
  "Treat a map-type node in the configuration."
  [getopt node]
  (let [parts (get node :chunk-size)
        at-ground (get node :at-ground false)
        prefix (if (get node :highlight) model/-# identity)
        shapes (reduce (partial tweak-plating getopt) [] (:hull-around node))
        hull (if at-ground misc/bottom-hull model/hull)]
    (when (get node :above-ground true)
      (prefix
        (apply (if parts model/union hull)
          (if parts
            (map (partial apply hull) (partition parts 1 shapes))
            shapes))))))

(defn- tweak-plating
  "A reducer."
  [getopt coll node]
  (conj coll
    (if (map? node)
      (tweak-map getopt node)
      (apply (partial tweak-posts getopt) node))))

(defn wall-tweaks
  "User-requested additional shapes."
  [getopt]
  (apply model/union
    (reduce (partial tweak-plating getopt) [] (tweak-data getopt))))
