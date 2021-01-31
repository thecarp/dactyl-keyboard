;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Microcontrollers                                                    ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.mcu
  (:require [scad-clj.model :as model]
            [scad-tarmi.core :refer [π]]
            [scad-tarmi.maybe :as maybe]
            [scad-klupe.iso :refer [head-length]]
            [scad-tarmi.util :refer [loft]]
            [dactyl-keyboard.misc :refer [colours]]
            [dactyl-keyboard.cots :as cots]
            [dactyl-keyboard.cad.misc :refer [map-to-3d-vec merge-bolt wafer]]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.param.proc.anch :refer [resolve-body]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Configuration Interface ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn derive-properties
  "Derive secondary properties of the MCU."
  [getopt]
  (let [mcu-type (getopt :mcu :type)
        pcb-base (merge (::cots/default cots/mcu-facts)
                        (mcu-type cots/mcu-facts))
        [xₚ yₚ zₚ] (map-to-3d-vec pcb-base)
        pcb-sw [(/ xₚ -2) (- yₚ) 0]
        pcb-corners {:NW (mapv + pcb-sw [0 yₚ 0])
                     :NE (mapv + pcb-sw [xₚ yₚ 0])
                     :SE (mapv + pcb-sw [xₚ 0 0])
                     :SW pcb-sw}
        xₜ (* (getopt :mcu :support :lock :width-factor) xₚ)
        yₜ (+ yₚ (getopt :mcu :support :lock :bolt :mount-length))
        plate-sw [(/ xₜ -2) (- yₜ) 0]
        plate-corners {:NW (mapv + plate-sw [0 yₜ 0])
                       :NE (mapv + plate-sw [xₜ yₜ 0])
                       :SE (mapv + plate-sw [xₜ 0 0])
                       :SW plate-sw}
        body (resolve-body getopt
               (getopt :mcu :body) (getopt :mcu :anchoring :anchor))]
   {:include-centrally (and (getopt :mcu :include) (= body :central-housing))
    :include-mainly (and (getopt :mcu :include) (= body :main))
    ;; Add [x y z] coordinates of the four corners of the PCB. No DFM.
    :pcb (merge pcb-base pcb-corners)
    :connector (get cots/port-facts (:port-type pcb-base))
    :plate (merge
             {:width xₜ
              :length yₜ
              :thickness (getopt :mcu :support :lock :plate :base-thickness)
              :transition (- (+ (getopt :mcu :support :lock :plate :clearance)
                                (/ zₚ 2)))}
             plate-corners)}))


;;;;;;;;;;;;
;; Models ;;
;;;;;;;;;;;;

;; Mostly internal but with a couple of functions exposed for use in
;; tweaks.

(defn- pcba-model
  "A model of an MCU: PCB and integrated USB connector (if any). The
  orientation of the model is flat with the connector on top, facing “north”.
  The middle of that short edge of the PCB centers at the origin of the local
  cordinate system."
  [getopt include-margin port-elongation]
  (let [prop (partial getopt :mcu :derived)
        overshoot (prop :pcb :port-overshoot)
        [pcb-x pcb-y pcb-z] (map-to-3d-vec (prop :pcb))
        [usb-x usb-y-base usb-z] (map-to-3d-vec (prop :connector))
        usb-y (+ usb-y-base port-elongation)
        margin (if include-margin (getopt :dfm :error-general) 0)
        mcube (fn [& dimensions] (apply model/cube (map #(- % margin) dimensions)))]
    (model/union
      (model/translate [0 (/ pcb-y -2) 0]
        (model/color (:pcb colours)
          (mcube pcb-x pcb-y pcb-z)))
      (model/translate [0
                        (+ (/ usb-y -2) (/ port-elongation 2) overshoot)
                        (+ (/ pcb-z 2) (/ usb-z 2))]
        (model/color (:metal colours)
          (mcube usb-x usb-y usb-z))))))

(defn- pcba-negative [getopt] (pcba-model getopt true 10))

(defn- alcove-model
  "A blocky shape for the connector end of the MCU.
  For use as a complement to pcba-negative.
  This is provided because a negative of the MCU model itself digging into the
  inside of a wall would create only a narrow notch, which would require
  high printing accuracy or difficult cleanup."
  [getopt]
  (let [prop (partial getopt :mcu :derived)
        [pcb-x _ pcb-z] (map-to-3d-vec (prop :pcb))
        usb-z (prop :connector :height)
        error (getopt :dfm :error-general)
        x (- pcb-x error)]
    (model/hull
      (model/translate [0 (/ x -2) 0]
        (model/cube x x (- pcb-z error)))
      (model/translate [0 (/ x -2) (/ (+ pcb-z usb-z) 2)]
        (model/cube (dec x) x (- usb-z error))))))

(defn- shelf-model
  "An MCU shelf. This is intended primarily for use with
  a pigtail cable between the MCU itself and a primary USB connector
  in the case wall."
  [getopt]
  (let [[xₚ yₚ zₚ] (mapv + (map-to-3d-vec (getopt :mcu :derived :pcb))
                           (getopt :mcu :support :shelf :extra-space))
        {:keys [N E S W] :or {N 0, E 0, S 0, W 0}}
        (getopt :mcu :support :shelf :bevel)
        t0 (getopt :mcu :support :shelf :thickness)
        t1 (getopt :mcu :support :shelf :rim :lateral-thickness)
        t2 (getopt :mcu :support :shelf :rim :overhang-thickness)
        xₜ (+ xₚ (* 2 t1))  ; Total width of the shelf with its sides.
        [X Y Z] [(* 3 xₜ) (* 3 yₚ) 60]  ; Blown up for intersections.
        xₒ (+ t1 (getopt :mcu :support :shelf :rim :overhang-width))
        [off0 off1] (getopt :mcu :support :shelf :rim :offsets)
        tr (fn [p a s] (model/translate p (maybe/rotate a s)))
        tc (fn [p d] (model/translate p (apply model/cube d)))
        side (fn [x-op y-offset]
               (maybe/translate [(x-op (/ xₚ 2))
                                 (+ y-offset (/ yₚ -2))
                                 (/ zₚ -2)]
                 (model/union
                   ;; Wall.
                   (tc [(x-op (/ t1 2)) 0 (/ zₚ 2)]
                       [t1 yₚ zₚ])
                   ;; Overhang.
                   (tc [(+ (x-op (/ xₒ -2)) (x-op t1)) 0 (+ zₚ (/ t2 2))]
                       [xₒ yₚ t2]))))]
    (maybe/intersection
      ;; The positive body of the shelf.
      (model/union ;; The back plate and grips, without a bevel.
        (tc [0 (/ yₚ -2) (- 0 (/ zₚ 2) (/ t0 2))] [X Y t0])
        (model/union (side - off0) (side + off1)))
      ;; End bevel, rotating on the x axis.
      (model/translate [0 0 (/ zₚ -2)]
        (let [d [X (/ yₚ 2) Z]]
          (maybe/union
            (tc [0 (/ yₚ -2) 0] d)
            (tr [0 0 0] [N 0 0] (tc [0 (/ yₚ -4) 0] d))
            (tr [0 (- yₚ) 0] [(- S) 0 0] (tc [0 (/ yₚ 4) 0] d)))))
      ;; Side (lengthwise) bevel, rotating on the y axis.
      (model/translate [0 0 (/ zₚ -2)]
        (let [d [(/ xₜ 2) Y Z]]
          (maybe/union
            (tc [0 (/ yₚ -2) 0] d)
            (tr [(/ xₜ 2) 0 0] [0 (- E) 0] (tc [(/ xₜ -4) (/ yₚ -2) 0] d))
            (tr [(/ xₜ -2) 0 0] [0 W 0] (tc [(/ xₜ 4) (/ yₚ -2) 0] d))))))))

(defn lock-plate-base
  "The model of the plate upon which an MCU PCBA rests in a lock.
  This is intended for use in the lock model itself (complete)
  and in tweaks (include-clearance set to false)."
  [getopt include-clearance]
  (let [[plate-x plate-y plate-z] (map-to-3d-vec (getopt :mcu :derived :plate))
        transition (getopt :mcu :derived :plate :transition)
        full-z (- plate-z transition)
        main-z (if include-clearance full-z plate-z)]
    (model/translate [0 0 (+ (- full-z) (/ main-z 2))]
      (model/cube plate-x plate-y main-z))))

(defn- lock-fixture-positive
  "Parts of the lock-style MCU support that integrate with the case.
  These comprise a plate for the bare side of the PCB to lay against and a socket
  that encloses the USB connector on the MCU to stabilize it, since integrated
  USB connectors are usually surface-mounted and therefore fragile."
  [getopt]
  (let [prop (partial getopt :mcu :derived)
        pcb-z (prop :pcb :thickness)
        [usb-x usb-y usb-z] (map-to-3d-vec (prop :connector))
        thickness (getopt :mcu :support :lock :socket :thickness)
        socket-z-thickness (+ (/ usb-z 2) thickness)
        socket-z-offset (+ (/ pcb-z 2) (* 3/4 usb-z) (/ thickness 2))
        socket-x (+ usb-x (* 2 thickness))]
    (model/union
      (model/translate [0 (/ (prop :plate :length) -2) 0]
        (lock-plate-base getopt true))
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
          (model/cube (+ socket-x 6) 1 1))))))

(defn- lock-fasteners-model
  "Negative space for a threaded bolt fastening an MCU lock."
  [getopt]
  (let [p (getopt :mcu :derived :plate :thickness)
        c (getopt :mcu :support :lock :plate :clearance)
        b (getopt :mcu :support :lock :plate :backing-thickness)
        m (getopt :mcu :support :lock :bolt :mount-length)
        d (getopt :mcu :support :lock :fastener-properties :m-diameter)
        head-type (getopt :mcu :support :lock :fastener-properties :head-type)
        [_ pcb-y pcb-z] (map-to-3d-vec (getopt :mcu :derived :pcb))]
    (->>
      (merge-bolt getopt
        {:unthreaded-length (+ p b (max 0 (- c (head-length d head-type))))
         :threaded-length (getopt :mcu :support :lock :bolt :mount-thickness)}
        (getopt :mcu :support :lock :fastener-properties))
      (model/rotate [π 0 0])
      (model/translate [0 (- (+ pcb-y (/ m 2))) (- (+ (/ pcb-z 2) p b c))]))))

(defn lock-bolt-model
  "Parts of the lock-style MCU support that don’t integrate with the case.
  The bolt as such is supposed to clear PCB components and enter the socket to
  butt up against the USB connector. There are some margins here, intended for
  the user to file down the tip and finalize the fit."
  [getopt]
  (let [prop (partial getopt :mcu :derived)
        [_ pcb-y pcb-z] (map-to-3d-vec (prop :pcb))
        [usb-x usb-y usb-z] (map-to-3d-vec (prop :connector))
        mount-z (getopt :mcu :support :lock :bolt :mount-thickness)
        mount-overshoot (getopt :mcu :support :lock :bolt :overshoot)
        mount-y-base (getopt :mcu :support :lock :bolt :mount-length)
        clearance (getopt :mcu :support :lock :bolt :clearance)
        shave (/ clearance 2)
        contact-z (- usb-z shave)
        bolt-z-mount (- mount-z clearance pcb-z)
        mount-x (getopt :mcu :derived :plate :width)
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
          (model/translate [0 (- (prop :pcb :port-overshoot) usb-y) bolt-z1]
            (model/cube usb-x wafer contact-z))]))
     (pcba-model getopt true 0)  ; Notch the mount.
     (lock-fasteners-model getopt))))


(defn- at-pcba [getopt subject]
  (place/at-named getopt {:anchor :mcu-pcba}
    ;; Align against the far side of the PCBA.
    (maybe/translate [0 (/ (getopt :mcu :derived :pcb :length) 2) 0] subject)))


;;;;;;;;;;;;;;;;;;;;;
;; Model Interface ;;
;;;;;;;;;;;;;;;;;;;;;

;; Models in place.

(defn shelf-in-place [getopt] (at-pcba getopt (shelf-model getopt)))

(defn negative-composite [getopt]
  (at-pcba getopt
    (model/union
      (pcba-negative getopt)
      (alcove-model getopt)
      (when (getopt :mcu :support :lock :include)
        (lock-fasteners-model getopt)))))

(defn lock-fixture-composite
  "MCU support features outside the alcove."
  [getopt]
  (at-pcba getopt
    (model/difference
      (lock-fixture-positive getopt)
      (lock-bolt-model getopt)
      (pcba-negative getopt)
      (lock-fasteners-model getopt))))

(defn preview-composite
  [getopt]
  (at-pcba getopt
    (maybe/union
      (pcba-model getopt false 0)  ; Visualization.
      (when (getopt :mcu :support :lock :include)
        (lock-bolt-model getopt)))))

