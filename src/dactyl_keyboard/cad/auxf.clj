;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Auxiliary Features                                                  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.auxf
  (:require [scad-clj.model :as model]
            [scad-tarmi.core :refer [π]]
            [scad-tarmi.maybe :as maybe]
            [scad-klupe.iso :refer [nut]]
            [dactyl-keyboard.cad.misc :as misc]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.param.proc.anch :as anch]))


;;;;;;;;;;;;;;;;
;; Back Plate ;;
;;;;;;;;;;;;;;;;

;; Plate for a connecting beam, rod etc.

(defn backplate-place
  [getopt shape]
  (->>
    shape
    (model/translate
      (place/offset-from-anchor getopt (getopt :case :back-plate :position) 3))
    (model/translate [0 0 (/ (getopt :case :back-plate :beam-height) -2)])))

(defn backplate-shape
  "A mounting plate for a connecting bar/rod/beam."
  [getopt]
  (let [height (getopt :case :back-plate :beam-height)
        width (+ (getopt :case :back-plate :fasteners :distance) height)
        depth 3
        interior-protrusion 8
        exterior-bevel 1
        interior-bevel 7]
   (model/hull
     (model/translate [0 (- interior-protrusion) 0]
       (model/cube (- width interior-bevel) depth (- height interior-bevel)))
     (model/cube width depth height)
     (model/translate [0 exterior-bevel 0]
       (model/cube (dec width) depth (dec height))))))

(defn backplate-fastener-holes
  "Two holes for screws through the back plate."
  [getopt]
  (let [d (getopt :case :back-plate :fasteners :bolt-properties :m-diameter)
        D (getopt :case :back-plate :fasteners :distance)
        hole (fn [x-offset]
               (->>
                 (model/union
                   (model/cylinder (/ d 2) 25)
                   (if (getopt :case :back-plate :fasteners :bosses)
                     (model/translate [0 0 10]
                       (nut {:m-diameter d :height 10 :negative true}))))
                 (model/rotate [(/ π 2) 0 0])
                 (model/translate [x-offset 0 0])
                 (backplate-place getopt)))]
   (model/union
     (hole (/ D 2))
     (hole (/ D -2)))))

(defn backplate-block [getopt]
  (misc/bottom-hull (backplate-place getopt (backplate-shape getopt))))


;;;;;;;;;;;;;;;
;; LED Strip ;;
;;;;;;;;;;;;;;;

(defn- west-wall-west-points [getopt]
  (let [cluster (getopt :case :leds :position :cluster)
        column 0
        rows (getopt :key-clusters :derived :by-cluster cluster
               :row-indices-by-column column)]
    (for [row rows, side [:WSW :WNW]]
     (let [[x y _] (place/wall-corner-place
                     getopt cluster [column row] {:side side})]
      [(+ x (getopt :by-key :parameters :wall :thickness)) y]))))

(defn- west-wall-east-points [getopt]
  (map (fn [[x y]] [(+ x 10) y]) (west-wall-west-points getopt)))

(defn- west-wall-led-channel [getopt]
  (let [west-points (west-wall-west-points getopt)
        east-points (west-wall-east-points getopt)]
    (model/extrude-linear {:height 50}
      (model/polygon (concat west-points (reverse east-points))))))

(defn- led-hole-position [getopt ordinal]
  (let [cluster (getopt :case :leds :position :cluster)
        column 0
        rows (getopt :key-clusters :derived :by-cluster cluster
                 :row-indices-by-column column)
        row (first rows)
        [x0 y0 _] (place/wall-corner-place
                    getopt cluster [column row] {:side :WNW})
        h (+ 5 (/ (getopt :case :leds :housing-size) 2))]
   [x0 (+ y0 (* (getopt :case :leds :interval) ordinal)) h]))

(defn- led-emitter-channel [getopt ordinal]
  (->> (model/cylinder (/ (getopt :case :leds :emitter-diameter) 2) 20)
       (model/rotate [0 (/ π 2) 0])
       (model/translate (led-hole-position getopt ordinal))))

(defn- lhousing-channel [getopt ordinal]
  (let [h (getopt :case :leds :housing-size)]
   (->> (model/cube 50 h h)
        (model/translate (led-hole-position getopt ordinal)))))

(defn led-holes [getopt]
  (let [holes (range (getopt :case :leds :amount))
        group (fn [function]
                (apply model/union (map (partial function getopt) holes)))]
    (model/union
      (model/intersection
        (west-wall-led-channel getopt)
        (group lhousing-channel))
      (group led-emitter-channel))))


;;;;;;;;;;;
;; Ports ;;
;;;;;;;;;;;

(defn port-hole
  "Negative space for one port, in place.
  The upper middle edge of the face of the port is placed at the origin
  of the local coordinate system.
  This comes with a flared front plate for entry in case of imperfect alignment."
  [getopt id]
  (let [[[_ x] [_ y] z] (place/port-hole-size getopt id)]
    (maybe/translate (place/port-hole-offset getopt {:anchor id})
      (model/union
        (model/cube x y z)
        (model/translate [0 (/ y 2) 0]
          (model/hull
            (model/cube x misc/wafer z)
            (model/translate [0 1 0]
              (model/cube (inc x) misc/wafer (inc z)))))))))

(defn port-holder
  "Positive space for one port, in place.
  Take the ID of the port, not the holder."
  [getopt id]
  {:pre [(keyword? id)]}
  (let [[x y z] (place/port-holder-size getopt id)]
    (maybe/translate
      (place/port-holder-offset getopt {::anch/primary id})
      (model/cube x y z))))

(defn port-tweak-post
  "A cube the thickness of the wall around a specific holder."
  [getopt id]
  {:pre [(keyword? id)]}
  (apply model/cube (repeat 3 (getopt :ports id :holder :thickness))))

(defn- port-set
  "The positive or negative space for all ports."
  [getopt positive]
  (apply maybe/union
    (map (fn [id]
           (when (and (getopt :ports id :include)
                      (or (not positive)
                          (getopt :ports id :holder :include)))
             (place/port-place getopt id
               ((if positive port-holder port-hole) getopt id))))
         (keys (getopt :ports)))))

;; Unions of the positive and negative spaces for holding all ports, in place.
(defn ports-positive [getopt] (port-set getopt true))
(defn ports-negative [getopt] (port-set getopt false))


;;;;;;;;;;;;;;;;;;;;
;; Minor Features ;;
;;;;;;;;;;;;;;;;;;;;

(defn- foot-point
  [getopt point-spec]
  (place/offset-from-anchor getopt point-spec 2))

(defn- foot-plate
  [getopt polygon-spec]
  (model/extrude-linear
    {:height (getopt :case :foot-plates :height), :center false}
    (model/polygon (map (partial foot-point getopt) (:points polygon-spec)))))

(defn foot-plates
  "Model plates from polygons.
  Each vector specifying a point in a polygon must have an anchor (usually a
  key alias) and a corner thereof identified by a direction tuple. These can
  be followed by a two-dimensional offset for tweaking."
  [getopt]
  (apply maybe/union
    (map (partial foot-plate getopt) (getopt :case :foot-plates :polygons))))
