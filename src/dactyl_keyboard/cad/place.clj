;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Placement Utilities                                                 ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; This module consolidates functions on the basis that some minor features,
;;; including foot plates and bottom-plate anchors, can be positioned in
;;; relation to multiple other types of features, creating the need for a
;;; a high-level, delegating placement utility that builds on the rest.

(ns dactyl-keyboard.cad.place
  (:require [clojure.spec.alpha :as spec]
            [thi.ng.geom.vector :refer [vec3]]
            [thi.ng.geom.core :as geom]
            [thi.ng.math.core :as math]
            [scad-clj.model :as model]
            [scad-tarmi.core :refer [abs π] :as tarmi-core]
            [scad-tarmi.maybe :as maybe]
            [scad-tarmi.flex :as flex]
            [dmote-keycap.data :as capdata]
            [dmote-keycap.measure :as measure]
            [dactyl-keyboard.cots :as cots]
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.cad.matrix :as matrix]
            [dactyl-keyboard.cad.misc :as misc]
            [dactyl-keyboard.param.access
             :refer [most-specific resolve-anchor key-properties]]
            [dactyl-keyboard.param.proc.anch :as anch]))


;;;;;;;;;;;;;;;
;; Functions ;;
;;;;;;;;;;;;;;;

;; Primitives.

(defn- grid-factors
  "Find a pair of [x y] unit particles for movement on a grid."
  [direction]
  (if (nil? direction)
    [0 0]
    (compass/to-grid (compass/convert-to-cardinal direction))))

(defn- *xy
  "Produce a vector for moving something laterally on a grid."
  ([direction offset]
   (*xy 1 direction offset))
  ([coefficient direction offset]
   {:pre [(spec/valid? ::tarmi-core/point-2-3d offset)]}
   (let [[dx dy] (grid-factors direction)]
     (-> offset
       (update 0 (partial * coefficient dx))
       (update 1 (partial * coefficient dy))))))

(defn- *z
  "Produce a vector for moving something vertically on a grid.
  This is based on a convention for cuboid models where segment
  0 is “up”, 1 is the middle or current location, 2 is “down”."
  ([segment offset]
   (*z 1 segment offset))
  ([coefficient segment offset]
   {:pre [(spec/valid? ::tarmi-core/point-2-3d offset)]}
   (-> offset
     (update 2 (partial * coefficient (case segment 0 1, 1 0, 2 -1))))))

(defn- cube-corner-xy
  [direction size wall-thickness]
  (mapv +
    (*xy 0.5 direction size)
    (*xy 0.5 (compass/reverse direction) [wall-thickness wall-thickness 0])))

(defn- cube-corner-z
  [segment size wall-thickness]
  (mapv +
    (*z 0.5 segment size)
    (*z 0.5 (abs (- 2 segment)) [0 0 wall-thickness])))

(defn- cube-corner-xyz
  [direction segment size wall-thickness]
  (assoc
    (cube-corner-xy direction size wall-thickness)
    2
    (last (cube-corner-z segment size wall-thickness))))

(declare reckon-from-anchor)
(declare reckon-with-anchor)

;; Key mounts.

(defn mount-corner-offset
  "Produce a mm coordinate offset for a corner of a switch mount."
  [getopt key-style side]
  {:pre [(compass/intermediates side)]}
  (let [style-data (getopt :keys :derived key-style)
        [subject-x subject-y] (map measure/key-length
                                (get style-data :unit-size [1 1]))
        m (getopt :case :key-mount-corner-margin)
        directions (side compass/intermediate-to-tuple)]
    [(* (apply compass/delta-x directions) (- (/ subject-x 2) (/ m 2)))
     (* (apply compass/delta-y directions) (- (/ subject-y 2) (/ m 2)))
     (/ (getopt :case :web-thickness) -2)]))

(defn- curver
  "Given an angle for progressive curvature, apply it. Else lay keys out flat."
  [subject dimension-n rotate-type delta-fn orthographic
   rot-ax-fn getopt cluster coord obj]
  (let [index (nth coord dimension-n)
        most #(most-specific getopt % cluster coord)
        angle-factor (most [:layout rotate-type :progressive])
        neutral (most [:layout :matrix :neutral subject])
        separation (most [:layout :matrix :separation subject])
        delta-f (delta-fn index neutral)
        delta-r (delta-fn neutral index)
        angle-product (* angle-factor delta-f)
        flat-distance (* capdata/mount-1u (- index neutral))
        key-prop (key-properties getopt cluster coord)
        {:keys [switch-type skirt-length]} key-prop
        radius (+ (measure/resting-clearance switch-type skirt-length)
                  (/ (/ (+ capdata/mount-1u separation) 2)
                     (Math/sin (/ angle-factor 2))))
        ortho-x (- (* delta-r (+ -1 (- (* radius (Math/sin angle-factor))))))
        ortho-z (* radius (- 1 (Math/cos angle-product)))]
   (if (zero? angle-factor)
     (flex/translate (assoc [0 0 0] dimension-n flat-distance) obj)
     (if orthographic
       (->> obj
            (rot-ax-fn angle-product)
            (flex/translate [ortho-x 0 ortho-z]))
       (misc/swing-callables flex/translate radius
                             (partial rot-ax-fn angle-product) obj)))))

(defn- put-in-column
  "Place a key in relation to its column."
  [rot-ax-fn getopt cluster coord obj]
  (curver :row 1 :pitch #(- %1 %2) false
          rot-ax-fn getopt cluster coord obj))

(defn- put-in-row
  "Place a key in relation to its row."
  [rot-ax-fn getopt cluster coord obj]
  (let [style (getopt :key-clusters :derived :by-cluster cluster :style)]
   (curver :column 0 :roll #(- %2 %1) (= style :orthographic)
           rot-ax-fn getopt cluster coord obj)))

(declare reckon-feature)

(defn- cluster-origin-finder
  "Compute 3D coordinates for the middle of a key cluster.
  Return a unary function: A partial translator."
  [getopt subject-cluster]
  (let [settings (getopt :key-clusters subject-cluster)
        {:keys [anchor offset] :or {offset [0 0 0]}} (:position settings)
        feature (reckon-feature getopt (resolve-anchor getopt anchor))]
   (partial flex/translate (mapv + feature offset))))

(defn cluster-place
  "Place and tilt passed ‘subject’ as if into a key cluster.
  This uses flex, so the ‘subject’ argument can be a
  single point in 3-dimensional space, typically an offset in mm from the
  middle of the indicated key, or a scad-clj object."
  [getopt cluster coord subject]
  (let [most #(most-specific getopt (concat [:layout] %) cluster coord)
        center (most [:matrix :neutral :row])
        bridge (cluster-origin-finder getopt cluster)]
    (->> subject
         (flex/translate (most [:translation :early]))
         (flex/rotate [(most [:pitch :intrinsic])
                       (most [:roll :intrinsic])
                       (most [:yaw :intrinsic])])
         (put-in-column #(flex/rotate [%1 0 0] %2) getopt cluster coord)
         (put-in-row #(flex/rotate [0 %1 0] %2) getopt cluster coord)
         (flex/translate (most [:translation :mid]))
         (flex/rotate [(most [:pitch :base])
                       (most [:roll :base])
                       (most [:yaw :base])])
         (flex/translate [0 (* capdata/mount-1u center) 0])
         (flex/translate (most [:translation :late]))
         (bridge))))


;; Case walls extending from key mounts.

(defn- wall-segment-offset
  "Compute a 3D offset from one corner of a switch mount to a part of its wall."
  [getopt cluster coord side segment]
  {:pre [(compass/cardinals side)]
   :post [(spec/valid? ::tarmi-core/point-3d %)]}
  (let [most #(most-specific getopt (concat [:wall] %) cluster coord)
        t (most [:thickness])
        bevel-factor (most [:bevel])
        long-dir (compass/short-to-long side)
        parallel (most [long-dir :parallel])
        perpendicular (most [long-dir :perpendicular])
        [dx dy] (side compass/to-grid)
        bevel
          (if (zero? perpendicular)
            bevel-factor
            (* bevel-factor
               (/ perpendicular (abs perpendicular))))]
   (case segment
     0 [0 0 0]
     1 [(* dx t) (* dy t) bevel]
     2 [(* dx parallel) (* dy parallel) perpendicular]
     3 [(* dx (+ parallel t)) (* dy (+ parallel t)) perpendicular]
     4 [(* dx parallel) (* dy parallel) (+ perpendicular bevel)])))

(defn- wall-vertex-offset
  "Compute a 3D offset from the center of a web post to a vertex on it."
  [getopt side keyopts]
  (let [xy (/ (getopt :case :key-mount-corner-margin) 2)
        z (/ (getopt :case :key-mount-thickness) 2)]
    (matrix/cube-vertex-offset side [xy xy z] keyopts)))

(defn wall-corner-offset
  "Combined [x y z] offset from the center of a switch mount.
  By default, this goes to one corner of the hem of the mount’s skirt of
  walling and therefore finds the base of full walls."
  [getopt cluster coordinates
   {:keys [side segment vertex]
    :or {segment 3, vertex false} :as keyopts}]
  {:pre [(or (nil? side) (compass/intermediates side))]}
  (mapv +
    (if side
      (mount-corner-offset getopt
        (most-specific getopt [:key-style] cluster coordinates)
        side)
      [0 0 0])
    (if side
      (wall-segment-offset getopt cluster coordinates
        (first (compass/intermediate-to-tuple side)) segment)
      [0 0 0])
    (if (and side vertex)
      (wall-vertex-offset getopt side keyopts)
      [0 0 0])))

(defn wall-corner-place
  "Absolute position of the lower wall around a key mount."
  ([getopt cluster coordinates]
   (wall-corner-place getopt cluster coordinates {}))
  ([getopt cluster coordinates keyopts]
   (wall-corner-place getopt cluster coordinates keyopts [0 0 0]))
  ([getopt cluster coordinates keyopts subject]
   (cluster-place getopt cluster coordinates
     (flex/translate
       (wall-corner-offset getopt cluster coordinates keyopts)
       subject))))

(defn wall-slab-center-offset
  "Combined [x y z] offset to the center of a vertical wall.
  Computed as the arithmetic average of its two corners."
  [getopt cluster coordinates direction]
  (let [c (fn [turning-fn]
            (wall-corner-offset getopt cluster coordinates
              {:side (compass/tuple-to-intermediate
                         [direction (turning-fn direction)])}))
        pair (map + (c compass/sharp-left) (c compass/sharp-right))]
    (vec (map / (vec pair) [2 2 2]))))

(defn wall-edge-sequence
  "Corner posts for the upper or lower part of the edge of one case wall slab.
  Return a sequence of transformations on the subject, or nil.
  Return nil when no wall is requested (extent zero) and when the lower portion
  of the wall is requested _and_ the wall in question is not full (i.e. should
  not reach the floor) _and_ the subject is not a coordinate."
  [getopt cluster upper [coord direction turning-fn] subject]
  {:pre [(compass/cardinals direction)]}
  (let [keyseq [:wall (compass/short-to-long direction) :extent]
        extent (most-specific getopt keyseq cluster coord)
        last-upper-segment (case extent :full 4, extent)
        place-segment
          (fn [segment]
            (->>
              subject
              (flex/translate
                (wall-corner-offset getopt cluster coord
                  {:side (compass/tuple-to-intermediate
                             [direction (turning-fn direction)])
                   :vertex (spec/valid? ::tarmi-core/point-2-3d subject)
                   :segment segment}))
              (cluster-place getopt cluster coord)))]
    (if-not (zero? last-upper-segment)
      (if upper
        ;; The part of the wall above the vertical drop to z = 0.
        (map place-segment (range (inc last-upper-segment)))
        ;; The image from which the vertical drop to z = 0 should be made.
        (cond
          ;; Use all the lower (post-perpendicular) segments for a full wall.
          (= extent :full) (map place-segment [2 3 4])
          ;; Make an exception for coordinate reckoning. This exception is
          ;; useful for drawing the bottom plate beneath the cluster.
          (spec/valid? ::tarmi-core/point-2-3d subject) (map place-segment [last-upper-segment]))))))


;; Central housing.

(defn- chousing-place
  "Place passed shape in relation to a vertex of the central housing.
  Pick the most useful precomputed 3D vertex, favouring actual vertices on
  the body of the central housing over more ethereral vertices that are not
  part of the body but correspond to its outer shell."
  [getopt index part side depth subject]
  {:pre [(nat-int? index), (keyword? part), (keyword? depth)]}
  (let [points (getopt :case :central-housing :derived :interface index :points)
        coord (or  ; Pick the first of a number of candidates.
               (get-in points [:above-ground part side depth])  ; Gabel.
               (get-in points [:above-ground part depth])  ; Adapter.
               (get-in points [:ethereal part]))]  ; Fallback even for at-ground.
    (flex/translate coord subject)))

(defn- chousing-fastener-landmark
  "Find a 3-tuple of coordinates for a fastener element for the central
  housing adapter."
  [getopt name base-index distance]
  {:pre [(or (keyword? name) (nil? name))]
   :post [(spec/valid? ::tarmi-core/point-3d %)]}
  (if name
    (reckon-from-anchor getopt name {})
    (let [prop (partial getopt :case :central-housing :shape)
          index (misc/shallow-wrap (prop :interface)
                                   (+ base-index (math/sign distance)))]
      (mapv + [(/ (prop :width) 2) 0 0] (prop :interface index :base :offset)))))

(defn chousing-fastener
  "Placement function for an arbitrary object in relation to the site of a
  fastener connecting the central housing to the main body on one side."
  ;; This assumes the wall is planar, and will therefore work poorly with
  ;; complex central-housing adapters and wall tweaks. Custom offsets and
  ;; angles may need to be added to the parameter set.
  [getopt {:keys [starting-point direction-point lateral-offset radial-offset]} subject]
  (let [pred (fn [{:keys [type part]}] (and (= type :central-housing)
                                            (= part :gabel)))
        anchor (resolve-anchor getopt starting-point pred)
        starting-coord (vec3 (reckon-from-anchor getopt starting-point {}))
        target-coord (chousing-fastener-landmark
                       getopt direction-point (:index anchor) radial-offset)
        nonlocal (math/- (vec3 target-coord) starting-coord)
        ;; There’s likely a simpler way to scale a thi.ng vector by a scalar.
        multiplier (* (math/sign radial-offset) (/ radial-offset (math/mag nonlocal)))
        displacement (geom/scale (vec3 (repeat 3 multiplier)) nonlocal)
        angle (- (geom/heading-yz nonlocal) (if (neg? radial-offset) π 0))]
    (flex/translate (mapv + starting-coord displacement [lateral-offset 0 0])
      (flex/rotate [angle 0 0]
        subject))))

;; Rear housing.

(defn- rhousing-segment-offset
  "Compute an [x y z] coordinate offset from a part of the rear housing.
  Here, segment 0 is the top surface, segment 1 is below the chamfer,
  segment 2 is halfway down the wall and 3 or more is all the way down."
  [getopt side segment]
  {:pre [(some? side)
         (compass/cardinals side)]}
  (let [cluster (getopt :case :rear-housing :position :cluster)
        wall (fn [key] (wall-segment-offset
                         getopt cluster
                         (getopt :case :rear-housing :derived :end-coord key)
                         side (min segment 1)))]
    (assoc  ; Replace the z coordinate.
       (case side
         :N [0 (if (zero? segment) 0 1) 0]
         :E (wall side)
         :S [0 (if (zero? segment) 0 -1) 0]
         :W (wall side))
       2
       (case segment
         0 0
         1 -1
         2 (/ (getopt :case :rear-housing :height) -2)
         (- (getopt :case :rear-housing :height))))))

(defn rhousing-vertex-offset
  [getopt side]
  {:pre [(compass/noncardinals side)]}
  (let [t (/ (getopt :case :web-thickness) 2)]
    (matrix/cube-vertex-offset side [t t t] {})))

(defn rhousing-place
  "Place passed shape in relation to a corner of the rear housing’s roof."
  [getopt side segment subject]
  {:pre [(compass/all-short side)]}
  (flex/translate
    (mapv +
      (getopt :case :rear-housing :derived :side
        (compass/convert-to-nonintermediate side))
      (rhousing-segment-offset
        getopt (compass/convert-to-cardinal side) segment))
    subject))


;; Microcontroller.

(defn mcu-place
  "Transform passed shape into the reference frame for an MCU PCB."
  ;; This function did a lot more tweaking for convenience in version 0.5.0.
  [getopt subject]
  (->> subject
    (flex/rotate (getopt :mcu :intrinsic-rotation))
    (flex/translate (reckon-with-anchor getopt (getopt :mcu :position)))))


;; Ports.

(defn port-hole-size
  "Compute the size of a port hole."
  [getopt id]
  {:pre [(= (getopt :derived :anchors id ::anch/type) :port-hole)]}
  (let [type (getopt :ports id :type)
        [xₛ yₛ zₛ] (if (= type :custom)
                     (getopt :ports id :size)
                     (misc/map-to-3d-vec (type cots/port-facts)))
        [xᵢ yᵢ] (map (getopt :dfm :derived :compensator) [xₛ yₛ])]
    [[xₛ xᵢ] [yₛ yᵢ] zₛ]))

(defn port-holder-size
  "Compute the size of a port holder."
  [getopt id]
  {:pre [(= (getopt :derived :anchors id ::anch/type) :port-hole)]}
  (let [[[x _] [y _] z] (port-hole-size getopt id)
        t (getopt :ports id :holder :thickness)]
    [(+ x t t) (+ y t) (+ z t t)]))

(defn port-hole-offset
  "Shift an offset for one part of a port hole.
  This is designed to hit a corner of the negative space."
  [getopt {:keys [anchor side segment offset]
           :or {segment 1, offset [0 0 0]}}]
  (when-not (#{0 1 2} segment)
    (throw (ex-info "Invalid segment ID specified for port hole."
              {:configured-segment segment
               :available-segments #{0 1 2}})))
  (let [[[_ x] [_ y] z] (port-hole-size getopt anchor)]
    (mapv + (cube-corner-xyz side segment [x y z] 0)
            offset)))

(defn- port-alignment-offset
  "Return a vector moving the centre of one port away from its anchor."
  [getopt id]
  (mapv -
    (port-hole-offset getopt
      {:anchor id
       :side (getopt :ports id :alignment :side)
       :segment (getopt :ports id :alignment :segment)})))

(defn port-holder-offset
  "Shift an offset for one part of a port holder.
  This is designed to hit inside the wall, not at a corner,
  on the assumption that a tweak post with the thickness of the
  wall is being placed."
  [getopt {:keys [parent side segment offset]
           :or {segment 1, offset [0 0 0]}}]
  {:pre [(keyword? parent)]}
  (when-not (#{0 1 2} segment)
    (throw (ex-info "Invalid segment ID specified for port holder."
              {:configured-segment segment
               :available-segments #{0 1 2}})))
  (let [t (getopt :ports parent :holder :thickness)
        [x y z] (port-holder-size getopt parent)]
    (mapv + (cube-corner-xyz side segment [x y z] t)
            [0 (/ t -2) 0]
            offset)))

(defn port-place
  "Place passed object as the indicated port."
  [getopt id obj]
  {:pre [(keyword? id)
         (= (getopt :derived :anchors id ::anch/type) :port-hole)]}
  (->> obj
    (flex/translate (port-alignment-offset getopt id))
    (flex/rotate (getopt :ports id :intrinsic-rotation))
    (flex/translate (reckon-with-anchor getopt (getopt :ports id :position)))))


;; Wrist rests.

(defn wrist-place
  "Place passed object like the plinth of the wrist rest."
  [getopt obj]
  (->>
    obj
    (flex/rotate [(getopt :wrist-rest :rotation :pitch)
                  (getopt :wrist-rest :rotation :roll)
                  0])
    (flex/translate (conj (getopt :wrist-rest :derived :center-2d)
                          (getopt :wrist-rest :plinth-height)))))

(defn wrist-undo
  "Reverse the rotation aspect of wrist-placement by repeating it in the negative.
  This is intended solely as a convenience to avoid having to rebalance models
  in the slicer."
  [getopt obj]
  (maybe/rotate [(- (getopt :wrist-rest :rotation :pitch))
                 (- (getopt :wrist-rest :rotation :roll))
                 0]
    obj))

(defn- remap-outline
  [getopt base-xy outline-key]
  (let [index (.indexOf (getopt :wrist-rest :derived :outline :base) base-xy)]
    (nth (getopt :wrist-rest :derived :outline outline-key) index)))

(defn- wrist-lip-coord
  [getopt xy outline-key]
  {:post [(spec/valid? ::tarmi-core/point-3d %)]}
  (let [nxy (remap-outline getopt xy outline-key)]
    (wrist-place getopt (conj nxy (getopt :wrist-rest :derived :z1)))))

(defn wrist-segment-coord
  "Take an xy coordinate pair as in the 2D wrist-rest spline outline and a
  segment ID number as for a case wall.
  Return vertex coordinates for the corresponding point on the plastic plinth
  of a wrist rest, in its final position.
  Segments extend outward and downward. Specifically, segment 0 is at
  the top of the lip, segment 1 is at the base of the lip, segment 2 is at
  global floor level, and all other segments are well below floor level to
  ensure that they fall below segment 1 even on a low and tilted rest."
  [getopt xy segment]
  {:pre [(vector? xy), (integer? segment)]
   :post [(spec/valid? ::tarmi-core/point-3d %)]}
  (case segment
    0 (wrist-place getopt (conj xy (getopt :wrist-rest :derived :z2)))
    1 (wrist-lip-coord getopt xy :lip)
    ; By default, recurse and override the z coordinate of segment 1.
    (assoc (wrist-segment-coord getopt xy 1) 2 (if (= segment 2) 0.0 -100.0))))

(defn wrist-segment-naive
  "Use wrist-segment-coord with a layer of translation from the naïve/relative
  coordinates initially supplied by the user to the derived base.
  Also support outline keys as an alternative to segment IDs, for bottom-plate
  fasteners."
  [getopt naive-xy outline-key segment]
  (let [translator (getopt :wrist-rest :derived :relative-to-base-fn)
        aware-xy (translator naive-xy)]
    (if (some? outline-key)
      (wrist-lip-coord getopt aware-xy outline-key)
      (wrist-segment-coord getopt aware-xy segment))))

(defn wrist-block-place
  "Place a block for a wrist-rest mount."
  ;; TODO: Rework the block model to provide meaningful support for side and
  ;; segment parameters. They are currently checked but otherwise ignored.
  [getopt mount-index side-key side segment obj]
  {:pre [(integer? mount-index)
         (keyword? side-key)
         (or (nil? side) (compass/intercardinals side))]}
  (let [prop (partial getopt :wrist-rest :mounts mount-index :derived)]
    (->>
      obj
      (flex/rotate [0 0 (prop :angle)])
      (flex/translate (prop side-key)))))


;; Polymorphic treatment of the properties of aliases.
;; The by-type multimethod dispatches placement of features in relation to
;; other features, on the basis of properties associated with each alias,
;; starting with its ::anch/type.

(defmulti by-type (fn [_ properties] (::anch/type properties)))

(defmethod by-type :origin
  [_ {:keys [initial]}]
  initial)

(defmethod by-type :central-housing
  [getopt {:keys [index initial part side depth] :or {depth :outer}}]
  (chousing-place getopt index part side depth initial))

(defmethod by-type :rear-housing
  [getopt {:keys [side segment initial] :or {segment 3}}]
  {:pre [(some? side)]}
  (rhousing-place getopt side segment initial))

(defmethod by-type :wr-perimeter
  [getopt {:keys [coordinates outline-key segment initial] :or {segment 3}}]
  (flex/translate
    (wrist-segment-naive getopt coordinates outline-key segment)
    initial))

(defmethod by-type :wr-block
  [getopt {:keys [mount-index side-key side segment initial]
           :or {segment 3}}]
  {:pre [(or (nil? side) (compass/noncardinals side))]}
  (wrist-block-place getopt mount-index side-key
    (compass/convert-to-intercardinal side) segment initial))

(defmethod by-type :key
  [getopt {:keys [cluster coordinates side segment initial]
           :or {segment 3} :as opts}]
  {:pre [(or (nil? side) (compass/all-short side))]}
  (when (and (some? side) (not (compass/intermediates side)))
    (throw (ex-info "Inapplicable side of key mount."
              {:configured-side side
               :available-sides compass/intermediates})))
  (cluster-place getopt cluster coordinates
    (if (some? side)
      ;; Corner named. By default, the target feature is the outermost wall.
      (flex/translate
        (wall-corner-offset getopt cluster coordinates
          (merge opts {:side side :segment segment}))
        initial)
      ;; Else no corner named.
      ;; The target feature is the middle of the key mounting plate.
      initial)))

(defmethod by-type :mcu-lock-plate
  [getopt {:keys [side segment initial] :or {segment 0}}]
  {:pre [(or (nil? side) (compass/noncardinals side))]}
  (mcu-place getopt
    (if side
      ;; One side of the lock plate.
      ;; Typically, this means that “initial” is either a nodule object
      ;; for a tweak or else some coordinate being used as an anchor.
      (let [side (compass/convert-to-intercardinal side)]
        ;; Here, segment 0 describes the plane of the PCB,
        ;; segment 1 the transition to the base of the lock plate,
        ;; and segment 2 the bottom of the lock plate.
        (flex/translate
          (conj (subvec (getopt :mcu :derived :plate side) 0 2)
                (case segment
                  0 0
                  1 (getopt :mcu :derived :plate :transition)
                  2 (- (getopt :mcu :derived :plate :transition)
                       (getopt :mcu :support :lock :plate :base-thickness))
                  (throw (ex-info "Invalid segment ID specified for lock plate."
                            {:configured-segment segment
                             :available-segments #{0 1 2}}))))
          initial))
      ;; Else the midpoint of the plate.
      ;; Typically, “initial” is the entire lock plate for a tweak.
      initial)))

(defmethod by-type :mcu-grip
  [getopt {:keys [side initial]}]
  {:pre [(compass/noncardinals side)]}
  (mcu-place getopt
    (flex/translate
      (getopt :mcu :derived :pcb (compass/convert-to-intercardinal side))
      initial)))

(defmethod by-type :port-hole
  [getopt {:keys [anchor initial]}]
  (port-place getopt anchor initial))

(defmethod by-type :port-holder
  [getopt {:keys [parent initial]}]
  (port-place getopt parent initial))

(defmethod by-type :secondary
  [getopt {:keys [anchor initial]}]
  {:pre [(keyword? anchor)]}
  (let [prop (partial getopt :derived :anchors anchor :position)]
    (->> initial
      (flex/translate (prop :translation))
      ;; Apply the override by walking across the primary anchor’s position,
      ;; picking coordinates from the override where not nil.
      (flex/translate (map-indexed #(or (get (prop :override) %1) %2)
                        (reckon-with-anchor getopt (prop :anchoring)))))))

;; Generalizations.

(defn- reckon-feature
  "A convenience for placing stuff in relation to other features.
  Differents parts of a feature can be targeted with keyword parameters.
  Return a scad-clj node or, by default, a vector of three numbers.
  Generally, the vector refers to what would be the middle of the outer wall
  of a feature. For keys, rear housing and wrist-rest mount blocks, this
  is the middle of a wall post. For central housing and the perimeter of the
  wrist rest, it’s a vertex on the surface.
  Any offset passed to this function will be interpreted in the native context
  of each feature placement function, with varying results."
  [getopt {:keys [offset subject] :or {offset [0 0 0], subject [0 0 0]}
           :as opts}]
  (by-type getopt (assoc opts :initial (flex/translate offset subject))))

(defn reckon-from-anchor
  "Find a position corresponding to a named point."
  [getopt anchor extras]
  {:pre [(keyword? anchor) (map? extras)]}
  (reckon-feature getopt (merge extras (resolve-anchor getopt anchor))))

(defn reckon-with-anchor
  "Produce coordinates for a specific feature using a single map that names
  an anchor."
  [getopt {:keys [anchor] :as opts}]
  {:pre [(keyword? anchor)]}
  (reckon-from-anchor getopt anchor opts))

(defn offset-from-anchor
  "Apply an offset from a user configuration to the output of
  reckon-with-anchor, instead of passing it as an input.
  The results are typically more predictable than passing the offset to
  reckon-with-anchor, being simple addition at a late stage.
  This function also supports explicit 2-dimensional inputs and outputs."
  [getopt opts dimensions]
  {:pre [(integer? dimensions)]}
  (let [base-3d (reckon-with-anchor getopt (dissoc opts :offset))
        base-nd (subvec base-3d 0 dimensions)
        offset-nd (get opts :offset (take dimensions (repeat 0)))]
    (mapv + base-nd offset-nd)))

(defn module-z0-2d-placer
  "Produce a function that places a named module in relation to an anchor.
  If “mirror”, a Boolean, is true, the module is mirrored on its own x axis,
  without affecting its position in relation to the anchor. This mirroring
  is intended to support chiral components of what are otherwise bilaterally
  symmetrical features of single program outputs.
  The returned function supports some general anchoring parameters but
  intercepts a numerical “direction” parameter and uses that to rotate the
  module on the z axis following any mirroring, instead of passing it on."
  [getopt module-name mirror]
  (let [prefix (if mirror (partial model/mirror [-1 0 0]) identity)]
    (fn [{:keys [direction] :or {direction 0} :as options}]
      {:pre [(number? direction)]}
      (let [anchor-map (dissoc options :direction)]
        (maybe/translate
          (misc/z0 (offset-from-anchor getopt anchor-map 2))
          (maybe/rotate [0 0 direction]
            (prefix (model/call-module module-name))))))))
