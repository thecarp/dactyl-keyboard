;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Auxiliary Features                                                  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.auxf
  (:require [scad-clj.model :as model]
            [scad-tarmi.core :refer [π]]
            [scad-tarmi.maybe :as maybe]
            [scad-tarmi.threaded :as threaded]
            [dactyl-keyboard.compass :as compass]
            [dactyl-keyboard.cad.misc :as misc]
            [dactyl-keyboard.cad.matrix :as matrix]
            [dactyl-keyboard.cad.place :as place]))


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
  "A mounting plate for a connecting beam."
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
  (let [d (getopt :case :back-plate :fasteners :diameter)
        D (getopt :case :back-plate :fasteners :distance)
        hole (fn [x-offset]
               (->>
                 (model/union
                   (model/cylinder (/ d 2) 25)
                   (if (getopt :case :back-plate :fasteners :bosses)
                     (model/translate [0 0 10]
                       (threaded/nut :iso-size d :height 10 :negative true))))
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
    (for [row rows, corner [:WSW :WNW]]
     (let [[x y _] (place/wall-corner-place
                     getopt cluster [column row] {:directions corner})]
      [(+ x (getopt :by-key :parameters :wall :thickness)) y]))))

(defn- west-wall-east-points [getopt]
  (map (fn [[x y]] [(+ x 10) y]) (west-wall-west-points getopt)))

(defn west-wall-led-channel [getopt]
  (let [west-points (west-wall-west-points getopt)
        east-points (west-wall-east-points getopt)]
    (model/extrude-linear {:height 50}
      (model/polygon (concat west-points (reverse east-points))))))

(defn led-hole-position [getopt ordinal]
  (let [cluster (getopt :case :leds :position :cluster)
        column 0
        rows (getopt :key-clusters :derived :by-cluster cluster
                 :row-indices-by-column column)
        row (first rows)
        [x0 y0 _] (place/wall-corner-place
                    getopt cluster [column row] {:directions [:W :N]})
        h (+ 5 (/ (getopt :case :leds :housing-size) 2))]
   [x0 (+ y0 (* (getopt :case :leds :interval) ordinal)) h]))

(defn led-emitter-channel [getopt ordinal]
  (->> (model/cylinder (/ (getopt :case :leds :emitter-diameter) 2) 20)
       (model/rotate [0 (/ π 2) 0])
       (model/translate (led-hole-position getopt ordinal))))

(defn lhousing-channel [getopt ordinal]
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


;;;;;;;;;;;;;;;;
;; Signalling ;;
;;;;;;;;;;;;;;;;

(defn connection-position
  "Move the negative or positive of the connection metasocket into place."
  [getopt shape]
  (let [corner (getopt :connection :position :corner)
        directions (compass/keyword-to-tuple corner)
        use-housing (and = (getopt :connection :position :anchor) :rear-housing)
        socket-size (getopt :connection :socket-size)
        socket-depth-offset (/ (second socket-size) -2)
        socket-height-offset (/ (nth socket-size 2) 2)
        socket-thickness (getopt :connection :socket-thickness)
        vertical-alignment
          ;; Line up with a wall and a metasocket base plate.
          (if (and use-housing (getopt :connection :position :raise))
            ;; Raise socket to just below roof.
            (- (getopt :case :rear-housing :height)
               (max socket-thickness
                    (getopt :case :rear-housing :roof-thickness))
               socket-height-offset)
            ;; Raise socket to just above floor.
            (+ socket-thickness socket-height-offset))
        shim
          (if use-housing
            (place/lateral-offset getopt (second directions)
              (/ (first socket-size) -2))
            [0 0 0])]
   (->> shape
        ;; Bring the face of the socket to the origin, at the right height.
        (model/translate [0 socket-depth-offset vertical-alignment])
        ;; Rotate as specified and to face out from the anchor.
        (maybe/rotate
          (mapv +
            (getopt :connection :position :rotation)
            [0 0 (- (compass/radians (first directions)))]))
        ;; Bring snugly to the requested corner.
        (model/translate (mapv + shim (place/into-nook getopt :connection))))))

(defn connection-metasocket
  "The shape of a holder in the case to receive a signalling socket component.
  Here, the shape nominally faces north."
  [getopt]
  ;; TODO: Generalize this a bit to also provide a full-size USB socket
  ;; as a less fragile alternative or complement to a USB connector built into
  ;; the MCU.
  (let [socket-size (getopt :connection :socket-size)
        thickness (getopt :connection :socket-thickness)
        double (* thickness 2)]
   (model/translate [0 (/ thickness -2) 0]
     (apply model/cube (mapv + socket-size [double thickness double])))))

(defn connection-socket
  "Negative space for a port, with a hole for wires leading out of the port and
  into the interior of the keyboard. The hole is in the negative-y-side wall,
  based on the assumption that the socket is pointing “north” and the wires
  come out to the “south”. The hole is slightly thicker than the wall for
  cleaner rendering."
  [getopt]
  (let [socket-size (getopt :connection :socket-size)
        thickness (getopt :case :web-thickness)]
   (model/union
     (apply model/cube socket-size)
     (model/translate [0 (/ (+ (second socket-size) thickness) -2) 0]
       (model/cube (dec (first socket-size))
                   (inc thickness)
                   (dec (last socket-size)))))))

(defn connection-positive [getopt]
  (connection-position getopt (connection-metasocket getopt)))

(defn connection-negative [getopt]
  (connection-position getopt (connection-socket getopt)))


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
