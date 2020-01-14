;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Central Housing                                                     ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.central
  (:require [scad-clj.model :as model]
            [scad-tarmi.core :refer [π]]
            [scad-tarmi.maybe :as maybe]
            [scad-tarmi.threaded :as threaded]
            [thi.ng.geom.vector :refer [vec3]]
            [thi.ng.geom.core :as geom]
            [thi.ng.math.core :as math]
            [dactyl-keyboard.param.access :as access]
            [dactyl-keyboard.cad.misc :refer [wafer]]
            [dactyl-keyboard.cad.poly :as poly]
            [dactyl-keyboard.cad.place :as place]))


;;;;;;;;;;;;;;;
;; Internals ;;
;;;;;;;;;;;;;;;

(defn- outline-back-to-3d
  [base-3d outline]
  (map (fn [[x _ _] [y1 z1]] [x y1 z1]) base-3d outline))

(defn- horizontal-shifter [x-fn] (fn [[x y z]] [(x-fn x) y z]))

(defn- coord-from-index
  [getopt index]
  ;; TODO: Wrap around if index is bad.
  ;; TODO: Handle left/right side of housing.
  (let [prop (partial getopt :case :central-housing :shape)]
    (mapv + [(/ (prop :width) 2) 0 0] (prop :interface index :base :offset))))

(defn- fastener-landmark
  [getopt name base-index distance]
  (vec3 (if name
          (place/reckon-from-anchor getopt name {})
          (coord-from-index getopt (+ base-index (math/sign distance))))))

(defn- fastener
  "A fastener for attaching the central housing to the rest of the case.
  In place."
  [getopt {:keys [starting-point direction-point lateral-offset radial-offset]}]
  (let [prop (partial getopt :case :central-housing :adapter :fasteners)
        pred (fn [{:keys [type part]}]
               (and (= type :central-housing) (= part :gabel)))
        anchor (access/resolve-anchor getopt starting-point pred)
        starting-coord (vec3 (place/reckon-from-anchor getopt starting-point {}))
        target-coord (fastener-landmark getopt direction-point (:index anchor) radial-offset)
        nonlocal (math/- target-coord starting-coord)
        multiplier (/ radial-offset (math/mag nonlocal))
        ;; There’s likely a simpler way to scale a thi.ng vector by a scalar.
        displacement (geom/scale (vec3 (repeat 3 multiplier)) nonlocal)]
    (model/translate (vec (math/+ starting-coord displacement))
      (model/rotate [(geom/angle-between (vec3 [0 1 0]) nonlocal) 0 0]
        (threaded/bolt
         :iso-size (prop :diameter),
         :head-type :countersunk,
         :point-type :cone,
         :total-length (prop :length),
         :compensator (getopt :dfm :derived :compensator)
         :negative true)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Configuration Interface ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- collect-point-pair
  [idx {:keys [base adapter]}]
  (let [props {:type :central-housing, :index idx}
        pluck (fn [alias part extra]
                (when alias [alias (merge props {:part part} extra)]))]
    [(pluck (:alias base) :gabel {:side :right})
     (pluck (:alias adapter) :adapter {})]))

(defn collect-point-aliases
  "A map of aliases to corresponding indices in the interface array."
  [getopt]
  (->> (getopt :case :central-housing :shape :interface)
    (map-indexed collect-point-pair)
    (apply concat)
    (into {})))

(defn- shift-points
  "Manipulate a series of 3D points forming a perimeter.
  Inset (contract) the points in the yz plane (in 2D) and/or shift each point
  on the x axis (back in 3D). Return a vector for indexability."
  ([base]  ; Presumably called for vector conversion.
   (shift-points base 0))
  ([base inset]
   (shift-points base inset 0))
  ([base inset delta-x]
   (shift-points base inset + delta-x))
  ([base inset x-operator delta-x]
   (as-> base subject
     (mapv rest subject)
     (poly/from-outline subject inset)
     (outline-back-to-3d base subject)
     (mapv (horizontal-shifter #(x-operator % delta-x)) subject))))

(defn derive-properties
  "Derive certain properties from the base configuration."
  [getopt]
  (let [thickness (getopt :case :web-thickness)
        half-width (/ (getopt :case :central-housing :shape :width) 2)
        adapter-width (getopt :case :central-housing :adapter :width)
        interface (getopt :case :central-housing :shape :interface)
        base-points (map #(get-in % [:base :offset]) interface)
        gabel-out (shift-points base-points 0 half-width)
        gabel-in (shift-points base-points thickness half-width)
        adapter-points-3d (map #(get-in % [:adapter :offset] [0 0 0]) interface)
        adapter-intrinsic (shift-points adapter-points-3d)
        adapter-outer (mapv (partial map + [adapter-width 0 0])
                            gabel-out adapter-intrinsic)
        lip-t (getopt :case :central-housing :adapter :lip :thickness)
        lip-w (partial getopt :case :central-housing :adapter :lip :width)
        include-adapter (and (getopt :reflect)
                             (getopt :case :central-housing :include)
                             (getopt :case :central-housing :adapter :include))]
    {:include-adapter include-adapter
     :include-lip (and include-adapter
                       (getopt :case :central-housing :adapter :lip :include))
     :points
      {:gabel {:right {:outer gabel-out
                       :inner gabel-in}
               :left {:outer (shift-points base-points 0 - half-width)
                      :inner (shift-points base-points thickness - half-width)}}
       :adapter {:outer adapter-outer
                 :inner (shift-points adapter-outer thickness)}
       :lip {:outside {:outer (shift-points gabel-in 0 (lip-w :outer))
                       :inner (shift-points gabel-in lip-t (lip-w :outer))}
             :inside {:outer (shift-points gabel-in 0 - (+ (lip-w :taper)
                                                           (lip-w :inner)))
                      :inner (shift-points gabel-in lip-t - (lip-w :inner))}}}}))


;;;;;;;;;;;;;;;;;;;
;; Model Interop ;;
;;;;;;;;;;;;;;;;;;;

(defn tweak-post
  "Place an adapter between the housing polyhedron and a case wall."
  [getopt alias]
  {:pre [(keyword? alias)]}
  (let [shape (model/cube wafer wafer wafer)]
    (model/hull
      (place/reckon-from-anchor getopt alias {:depth :outer, :subject shape})
      (place/reckon-from-anchor getopt alias {:depth :inner, :subject shape}))))


;;;;;;;;;;;;;
;; Outputs ;;
;;;;;;;;;;;;;

(defn lip-body-right
  [getopt]
  (let [vertices (partial getopt :case :central-housing :derived :points :lip)]
    (poly/tuboid
      (vertices :outside :outer)
      (vertices :outside :inner)
      (vertices :inside :outer)
      (vertices :inside :inner))))

(defn fasteners
  [getopt]
  (let [positions (getopt :case :central-housing :adapter :fasteners :positions)]
    (apply model/union
      (map (partial fastener getopt) positions))))

(defn adapter
  "An OpenSCAD polyhedron describing an adapter for the central housing."
  [getopt]
  (let [vertices (partial getopt :case :central-housing :derived :points)]
    (poly/tuboid
      (vertices :gabel :right :outer)
      (vertices :gabel :right :inner)
      (vertices :adapter :outer)
      (vertices :adapter :inner))))

(defn main-body
  "An OpenSCAD polyhedron describing the body of the central housing."
  [getopt]
  (let [vertices (partial getopt :case :central-housing :derived :points)]
    (maybe/union
      (poly/tuboid
        (vertices :gabel :left :outer)
        (vertices :gabel :left :inner)
        (vertices :gabel :right :outer)
        (vertices :gabel :right :inner))
      (when (getopt :case :central-housing :derived :include-lip)
        (model/union
          (lip-body-right getopt)
          (model/mirror [-1 0 0] (lip-body-right getopt)))))))
