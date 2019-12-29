;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Auxiliary Features                                                  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.auxf
  (:require [scad-clj.model :as model]
            [scad-tarmi.core :refer [π]]
            [scad-tarmi.maybe :as maybe]
            [scad-tarmi.threaded :as threaded]
            [scad-tarmi.util :refer [loft]]
            [dactyl-keyboard.generics :as generics]
            [dactyl-keyboard.cad.misc :as misc]
            [dactyl-keyboard.cad.matrix :as matrix]
            [dactyl-keyboard.cad.place :as place]))


;;;;;;;;;;;;;;;;;;;
;; Multiple Uses ;;
;;;;;;;;;;;;;;;;;;;


(def usb-a-female-dimensions
  "This assumes the flat orientation common in laptops.
  In a DMOTE, USB connector width would typically go on the z axis, etc."
  {:full {:width 10.0 :length 13.6 :height 6.5}
   :micro {:width 7.5 :length 5.9 :height 2.55}})


;;;;;;;;;;;;;;;;;;;;;
;; Microcontroller ;;
;;;;;;;;;;;;;;;;;;;;;

(defn- descriptor-vec
  [{:keys [width length thickness height]}]
  [(or width 1) (or length 1) (or thickness height 1)])

(defn derive-mcu-properties
  "Derive secondary properties of the MCU."
  [getopt]
  (let [mcu-type (getopt :mcu :type)
        pcb-base {:thickness 1.57 :connector-overshoot 1.9}
        pcb-model (case mcu-type
                    :promicro {:width 18 :length 33}
                    :teensy {:width 17.78 :length 35.56}
                    :teensy++ {:width 17.78 :length 53})
        [x y z] (descriptor-vec (merge pcb-base pcb-model))
        sw [(/ x -2) (- y) 0]
        pcb-corners {:nw (mapv + sw [0 y 0])
                     :ne (mapv + sw [x y 0])
                     :se (mapv + sw [x 0 0])
                     :sw sw}
        plate-transition (- (+ (getopt :mcu :support :lock :plate :clearance)
                               (/ z 2)))]
   {:include-centrally (and (getopt :mcu :include)
                            (getopt :mcu :position :central))
    :include-laterally (and (getopt :mcu :include)
                            (not (and (getopt :reflect)
                                      (getopt :case :central-housing :include)
                                      (getopt :mcu :position :central))))
    ;; Add [x y z] coordinates of the four corners of the PCB. No DFM.
    :pcb (merge pcb-base pcb-model pcb-corners)
    :connector (:micro usb-a-female-dimensions)
    :lock-width (* (getopt :mcu :support :lock :width-factor) x)}))

(defn collect-mcu-grip-aliases
  "Collect the names of MCU grip anchors. Expand 2D offsets to 3D."
  [getopt]
  (reduce
    (fn [coll {:keys [corner offset alias] :or {offset [0 0]}}]
      (assoc coll alias
        {:type :mcu-grip,
         :corner (generics/directions-to-unordered-corner corner),
         :offset (subvec (conj offset 0) 0 3)}))
    {}
    (getopt :mcu :support :grip :anchors)))

(defn mcu-model
  "A model of an MCU: PCB and integrated USB connector (if any). The
  orientation of the model is flat with the connector on top, facing “north”.
  The middle of that short edge of the PCB centers at the origin of the local
  cordinate system."
  [getopt include-margin connector-elongation]
  (let [prop (partial getopt :mcu :derived)
        overshoot (prop :pcb :connector-overshoot)
        [pcb-x pcb-y pcb-z] (descriptor-vec (prop :pcb))
        [usb-x usb-y-base usb-z] (descriptor-vec (prop :connector))
        usb-y (+ usb-y-base connector-elongation)
        margin (if include-margin (getopt :dfm :error-general) 0)
        mcube (fn [& dimensions] (apply model/cube (map #(- % margin) dimensions)))]
    (model/union
      (model/translate [0 (/ pcb-y -2) 0]
        (model/color (:pcb generics/colours)
          (mcube pcb-x pcb-y pcb-z)))
      (model/translate [0
                        (+ (/ usb-y -2) (/ connector-elongation 2) overshoot)
                        (+ (/ pcb-z 2) (/ usb-z 2))]
        (model/color (:metal generics/colours)
          (mcube usb-x usb-y usb-z))))))

(defn mcu-visualization [getopt]
  (place/mcu-place getopt (mcu-model getopt false 0)))

(defn mcu-pcba-negative [getopt]
  (place/mcu-place getopt (mcu-model getopt true 10)))

(defn mcu-alcove
  "A blocky shape at the connector end of the MCU.
  For use as a complement to mcu-pcba-negative.
  This is provided because a negative of the MCU model itself digging into the
  inside of a wall would create only a narrow notch, which would require
  high printing accuracy or difficult cleanup."
  [getopt]
  (let [prop (partial getopt :mcu :derived)
        [pcb-x _ pcb-z] (descriptor-vec (prop :pcb))
        usb-z (prop :connector :height)
        error (getopt :dfm :error-general)
        x (- pcb-x error)]
    (place/mcu-place getopt
      (model/hull
        (model/translate [0 (/ x -2) 0]
          (model/cube x x (- pcb-z error)))
        (model/translate [0 (/ x -2) (/ (+ pcb-z usb-z) 2)]
          (model/cube (dec x) x (- usb-z error)))))))

(defn mcu-lock-plate-base
  "The model of the plate upon which an MCU PCBA rests in a lock.
  This is intended for use in the lock model itself (complete)
  and in tweaks (base only, not complete)."
  [getopt complete]
  (let [[_ pcb-y pcb-z] (descriptor-vec (getopt :mcu :derived :pcb))
        plate-y (+ pcb-y (getopt :mcu :support :lock :bolt :mount-length))
        clearance (getopt :mcu :support :lock :plate :clearance)
        base-thickness (getopt :mcu :support :lock :plate :base-thickness)
        full-z (+ (/ pcb-z 2) clearance base-thickness)]
    (model/translate [0
                      (/ plate-y -2)
                      (+ (- full-z) (/ (if complete full-z base-thickness) 2))]
      (model/cube (getopt :mcu :derived :lock-width)
                  plate-y
                  (if complete full-z base-thickness)))))

(defn mcu-lock-fixture-positive
  "Parts of the lock-style MCU support that integrate with the case.
  These comprise a plate for the bare side of the PCB to lay against and a socket
  that encloses the USB connector on the MCU to stabilize it, since integrated
  USB connectors are usually surface-mounted and therefore fragile."
  [getopt]
  (let [prop (partial getopt :mcu :derived)
        [_ pcb-y pcb-z] (descriptor-vec (prop :pcb))
        [usb-x usb-y usb-z] (descriptor-vec (prop :connector))
        plate-z (getopt :mcu :support :lock :plate :clearance)
        plate-y (+ pcb-y (getopt :mcu :support :lock :bolt :mount-length))
        thickness (getopt :mcu :support :lock :socket :thickness)
        socket-z-thickness (+ (/ usb-z 2) thickness)
        socket-z-offset (+ (/ pcb-z 2) (* 3/4 usb-z) (/ thickness 2))
        socket-x (+ usb-x (* 2 thickness))]
   (place/mcu-place getopt
     (model/union
       (mcu-lock-plate-base getopt true)
       ;; The socket:
       (model/hull
         ;; Purposely ignore connector overshoot in placing the socket.
         ;; This has the advantages that the lock itself can also be stabilized
         ;; by the socket, while the socket does not protrude outside the case.
         (model/translate [0 (/ usb-y -2) socket-z-offset]
           (model/cube socket-x usb-y socket-z-thickness))
         ;; Stabilizers for the socket:
         (model/translate [0 0 10]
           (model/cube socket-x 1 1))
         (model/translate [0 0 socket-z-offset]
           (model/cube (+ socket-x 6) 1 1)))))))

(defn mcu-lock-fasteners-model
  "Negative space for a bolt threading into an MCU lock."
  [getopt]
  (let [head-type (getopt :mcu :support :lock :fastener :style)
        d (getopt :mcu :support :lock :fastener :diameter)
        l0 (threaded/head-height d head-type)
        l1 (if (= (getopt :mcu :position :anchor) :rear-housing)
             (getopt :case :rear-housing :wall-thickness)
             (getopt :case :web-thickness))
        [_ pcb-y pcb-z] (descriptor-vec (getopt :mcu :derived :pcb))
        l2 (getopt :mcu :support :lock :plate :clearance)
        y1 (getopt :mcu :support :lock :bolt :mount-length)]
    (->>
      (threaded/bolt
          :iso-size d
          :head-type head-type
          :unthreaded-length (max 0 (- (+ l1 l2) l0))
          :threaded-length (getopt :mcu :support :lock :bolt :mount-thickness)
          :negative true)
      (model/rotate [π 0 0])
      (model/translate [0 (- (+ pcb-y (/ y1 2))) (- (+ (/ pcb-z 2) l1 l2))]))))

(defn mcu-lock-sink [getopt]
  (place/mcu-place getopt
    (mcu-lock-fasteners-model getopt)))

(defn mcu-lock-bolt-model
  "Parts of the lock-style MCU support that don’t integrate with the case.
  The bolt as such is supposed to clear PCB components and enter the socket to
  butt up against the USB connector. There are some margins here, intended for
  the user to file down the tip and finalize the fit."
  [getopt]
  (let [prop (partial getopt :mcu :derived)
        usb-overshoot (prop :pcb :connector-overshoot)
        [_ pcb-y pcb-z] (descriptor-vec (prop :pcb))
        [usb-x usb-y usb-z] (descriptor-vec (prop :connector))
        mount-z (getopt :mcu :support :lock :bolt :mount-thickness)
        mount-overshoot (getopt :mcu :support :lock :bolt :overshoot)
        mount-y-base (getopt :mcu :support :lock :bolt :mount-length)
        clearance (getopt :mcu :support :lock :bolt :clearance)
        shave (/ clearance 2)
        contact-z (- usb-z shave)
        bolt-z-mount (- mount-z clearance pcb-z)
        mount-x (getopt :mcu :derived :lock-width)
        bolt-z0 (+ (/ pcb-z 2) clearance (/ bolt-z-mount 2))
        bolt-z1 (+ (/ pcb-z 2) shave (/ contact-z 2))]
   (model/difference
     (model/union
       (model/translate [0
                         (- (/ mount-overshoot 2) pcb-y (/ mount-y-base 2))
                         (+ (/ pcb-z -2) (/ mount-z 2))]
         (model/cube mount-x (+ mount-overshoot mount-y-base) mount-z))
       (loft
         [(model/translate [0 (- pcb-y) bolt-z0]
            (model/cube usb-x 10 bolt-z-mount))
          (model/translate [0 (/ pcb-y -4) bolt-z0]
            (model/cube usb-x 1 bolt-z-mount))
          (model/translate [0 (- usb-overshoot usb-y) bolt-z1]
            (model/cube usb-x misc/wafer contact-z))]))
     (mcu-model getopt true 0)  ; Notch the mount.
     (mcu-lock-fasteners-model getopt))))

(defn mcu-lock-bolt-locked [getopt]
  (place/mcu-place getopt (mcu-lock-bolt-model getopt)))

(defn mcu-negative-composite [getopt]
  (model/union
    (mcu-pcba-negative getopt)
    (mcu-alcove getopt)
    (when (getopt :mcu :support :lock :include)
      (mcu-lock-sink getopt))))

(defn mcu-lock-fixture-composite
  "MCU support features outside the alcove."
  [getopt]
  (model/difference
    (mcu-lock-fixture-positive getopt)
    (mcu-lock-bolt-locked getopt)
    (mcu-pcba-negative getopt)
    (mcu-lock-sink getopt)))

(defn mcu-preview-composite
  [getopt]
  (maybe/union
    (mcu-visualization getopt)
    (when (getopt :mcu :support :lock :include)
      (mcu-lock-bolt-locked getopt))))


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
    (for [row rows, corner [generics/WSW generics/WNW]]
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
                    getopt cluster [column row] {:directions generics/WNW})
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
            (place/lateral-offset getopt (second corner)
              (/ (first socket-size) -2))
            [0 0 0])]
   (->> shape
        ;; Bring the face of the socket to the origin, at the right height.
        (model/translate [0 socket-depth-offset vertical-alignment])
        ;; Rotate as specified and to face out from the anchor.
        (maybe/rotate
          (mapv +
            (getopt :connection :position :rotation)
            [0 0 (- (matrix/compass-radians (first corner)))]))
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
