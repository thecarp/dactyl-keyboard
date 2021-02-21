;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Miscellaneous CAD Utilities                                         ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; Functions useful in more than one scad-clj project.

(ns dactyl-keyboard.cad.misc
  (:require [clojure.spec.alpha :as spec]
            [scad-clj.model :as model]
            [scad-tarmi.core :as tarmi]
            [scad-tarmi.maybe :as maybe]
            [scad-klupe.iso :refer [bolt]]
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.cad.poly :as poly]
            [dactyl-keyboard.param.access :refer [compensator]]))


(def wafer 0.001)  ; Generic insignificant feature size.
(def nodule (apply model/cube (repeat 3 wafer)))

(defn map-to-3d-vec
  "Turn a descriptive hash map into an x-y-z vector."
  {:post [#(spec/valid? ::tarmi/point-3d %)]}
  [{:keys [width depth length thickness height]}]
  [(or width 1) (or depth length 1) (or thickness height 1)])

(defn shallow-wrap
  "Return a permissible index to a list-like collection.
  This function does not wrap around completely so that e.g. -2 indexes
  the second-to-last-element, but it does pick the opposite end of the
  collection when out of bounds."
  [array index]
  (let [n (count array)]
    (cond (< index 0) (dec n), (>= index n) 0, :default index)))

(defn pad-to-3d
  "Pad a coordinate vector to three dimensions."
  ([coordinates] (pad-to-3d coordinates 0))
  ([coordinates padding]
   {:pre [(vector? coordinates)
          (<= (count coordinates) 3)]}
   (if (= (count coordinates) 3)
     coordinates
     (pad-to-3d (conj coordinates padding) padding))))

(defn limit-d
  "Nullify coordinates for an arbitrary dimensionality."
  [n-dimensions coordinates]
  (pad-to-3d (subvec coordinates 0 n-dimensions) 0))

(defn bottom-extrusion [height p]
  (model/extrude-linear {:height height, :twist 0, :convexity 0, :center false}
    (model/project p)))

(defn bottom-hull [& p]
  (model/hull p (bottom-extrusion wafer p)))

(defn swing-callables
  "Rotate passed object with passed radius, not around its own axes.
  The ‘translator’ function receives a vector based on the radius, in the z
  axis only, and an object to translate.
  If ‘rotator’ is a 3-vector of angles or a 2-vector of an angle and an axial
  filter, a rotation function will be created based on that."
  [translator radius rotator obj]
  (if (vector? rotator)
    (if (= (count rotator) 3)
      (swing-callables translator radius (partial maybe/rotate rotator) obj)
      (swing-callables translator radius
        (partial maybe/rotate (first rotator) (second rotator))
        obj))
    ;; Else assume the rotator is usable as a function and apply it.
    (->> obj
      (translator [0 0 (- radius)])
      rotator
      (translator [0 0 radius]))))

(defn merge-bolt
  "Wrap scad-klupe.iso/bolt for multiple sources of parameters.
  Assume a negative-space bolt with threads to be tapped manually, and
  with standard DFM compensation. Allow overrides, including a built-in
  override for low resolution."
  [getopt & option-maps]
  (bolt (merge {:negative true
                :compensator (compensator getopt)
                :include-threading false}
               (apply merge option-maps)
               (when (getopt :resolution :exclude-all-threading)
                 {:include-threading false}))))

(defn grid-factors
  "Find a pair of [x y] unit particles for movement on a grid."
  ([direction]
   (grid-factors direction false))
  ([direction box]
   (if (nil? direction) [0 0] (compass/to-grid direction box))))

(defn- op-xy
  "Update the first two values of a vector with the same operator."
  [op coll]
  (-> coll (update 0 op) (update 1 op)))

(defn- *xy
  "Produce a vector for moving something laterally on a grid."
  ([direction offset]
   (*xy 1 direction offset))
  ([coefficient direction offset]
   {:pre [(number? coefficient)
          (or (nil? direction) (direction compass/all))
          (spec/valid? ::tarmi/point-2-3d offset)]}
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
   {:pre [(spec/valid? ::tarmi/point-2-3d offset)]}
   (-> offset
     (update 2 (partial * coefficient (case segment 0 1, 1 0, 2 -1))))))

(defn walled-corner-xy
  [direction size wall-thickness]
  (let [rev (when direction (compass/reverse direction))]
    (mapv +
      (*xy 0.5 direction size)
      (*xy 0.5 rev [wall-thickness wall-thickness 0]))))

(defn walled-corner-z
  [segment size wall-thickness]
  (mapv +
    (*z 0.5 segment size)
    (*z 0.5 (tarmi/abs (- 2 segment)) [0 0 wall-thickness])))

(defn walled-corner-xyz
  [direction segment size wall-thickness]
  (assoc
    (walled-corner-xy direction size wall-thickness)
    2
    (last (walled-corner-z segment size wall-thickness))))

(defn align-to-bevel
  [coll cardinal offsets]
  (let [index (compass/to-index cardinal)]
    (assoc coll index (nth offsets index))))

(defn- edge-of-bevel
  [direction size bevel-inset]
  {:pre [(spec/valid? ::tarmi/point-2-3d size)
         (direction compass/intermediates)]
   :post [(spec/valid? ::tarmi/point-2-3d %)]}
  (let [[primary secondary] (direction compass/keyword-to-tuple)]
    (-> size
      (align-to-bevel primary (*xy 0.5 primary size))
      (align-to-bevel secondary (*xy 0.5 secondary (op-xy #(- % (* 2 bevel-inset)) size))))))

(defn- bevelled-corner-xy
  "Compute a horizontal offset from the center of a rectangle, the
  corners of which are bevelled."
  [direction size bevel-inset]
  {:pre [(spec/valid? ::tarmi/point-2-3d size)
         (or (nil? direction) (direction compass/all))]
   :post [(spec/valid? ::tarmi/point-2-3d %)]}
  (if direction
    (case (compass/classify direction)
      ::compass/intermediate (edge-of-bevel direction size bevel-inset)
      (*xy 0.5 direction size))
    (vec (repeat (count size) 0.0))))

(defn bevelled-corner-xyz
  "Compute a 3D offset from the center of a cuboid, the corners of which are
  bevelled by a given inset on each side and the top.
  The segmentation is a bit different from *z: 0 is the top, 1 is the bottom
  edge of the top bevel, 2 is the middle, 3 the bottom."
  [direction segment size bevel-inset]
  {:pre [(spec/valid? ::tarmi/point-3d size)
         (or (nil? direction) (direction compass/all))
         (or (nil? segment) (integer? segment))]
   :post [(spec/valid? ::tarmi/point-3d %)]}
  (let [z (last size)
        segment (or segment 2)]
    (if (zero? segment)
      (assoc (*xy 0.5 direction (op-xy #(- % (* 2 bevel-inset)) size))
             2 (* 0.5 z))
      (assoc (bevelled-corner-xy direction size bevel-inset)
             2 (case segment
                 1 (- (* 0.5 z) bevel-inset)
                 2 0.0
                 (* -0.5 z))))))

(defn bevelled-cuboid
  "A polyhedron model of a complete bevelled cuboid."
  [size bevel-inset]
  (let [corner (fn [direction segment]
                 (bevelled-corner-xyz direction segment size bevel-inset))
        edge (fn [turn] [(corner turn 1) (corner turn 3)])]
   (poly/bevelled-cuboid
     (map (fn [direction]
            [(corner direction 0)
             [(edge (compass/gentle-left direction))
              (edge (compass/gentle-right direction))]])
          [:NE :SE :SW :NW]))))

(defn flip-x
  "Mirror on the x-axis, discarding the original."
  [shape]
  (if (some? shape) (model/mirror [-1 0 0] shape)))

(defn reflect-x
  "Mirror on the x-axis, keeping the original."
  [shape]
  (if (some? shape) (model/union shape (flip-x shape))))
