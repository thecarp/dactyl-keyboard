;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Body Assembly                                                       ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.cad.body.assembly
  (:require [clojure.set :as setlib]
            [scad-clj.model :as model]
            [scad-tarmi.maybe :as maybe]
            [dactyl-keyboard.cad.auxf :as auxf]
            [dactyl-keyboard.cad.body.central :as central]
            [dactyl-keyboard.cad.body.main :as main-body]
            [dactyl-keyboard.cad.body.wrist :as wrist]
            [dactyl-keyboard.cad.bottom :as bottom]
            [dactyl-keyboard.cad.flange :as flange]
            [dactyl-keyboard.cad.key :as key]
            [dactyl-keyboard.cad.key.wall :as wall]
            [dactyl-keyboard.cad.key.web :as web]
            [dactyl-keyboard.cad.mask :as mask]
            [dactyl-keyboard.cad.mcu :as mcu]
            [dactyl-keyboard.cad.misc :refer [flip-x reflect-x]]
            [dactyl-keyboard.cad.place :as place]
            [dactyl-keyboard.cad.tweak :as tweak]
            [dactyl-keyboard.sandbox :as sandbox]))


(defn- twanges
  "Tweaks and flanges with identical selection criteria."
  ;; This is enabled by tweaks and flanges having very similar interfaces,
  ;; despite being somewhat different semantically.
  [getopt selection]
  (maybe/union
    (tweak/union-3d getopt selection)
    (flange/union getopt selection)))

(defn- custom-negatives
  "Assorted negative space for a predefined body.
  This includes negative sandbox shapes, applied to all bodies."
  [getopt body bottom]
  (maybe/union
    (twanges getopt {:include-negative true, :include-bottom bottom,
                     :include-top true, :bodies #{body}})
    (sandbox/negative getopt)))

(defn wrist-rest-plinth-right
  "Right-hand-side non-preview wrist-rest plinth model."
  [getopt]
  (maybe/difference
    (maybe/union
      (mask/above-wrist-bottom-plate getopt
        (wrist/plinth-plastic getopt)
        (twanges getopt {:include-positive true, :include-top true,
                         :bodies #{:wrist-rest}}))
      (when (and (getopt :wrist-rest :preview)
                 (getopt :wrist-rest :bottom-plate :include))
        (bottom/wrist-positive getopt)))
    (custom-negatives getopt :wrist-rest
      (getopt :wrist-rest :bottom-plate :include))))

(defn wrist-rest-rubber-casting-mould-right
  "A thin shell that fits on top of the right-hand-side wrist-rest model.
  This is for casting silicone into, “in place”. If the wrist rest has
  180° rotational symmetry around the z axis, one mould should
  be enough for both halves’ wrist rests. To be printed upside down."
  [getopt]
  (place/wrist-undo getopt
    (model/difference
      (wrist/mould-polyhedron getopt)
      (wrist/projection-maquette getopt)
      (twanges getopt
        {:include-positive true, :include-top true,
         :bodies (setlib/union
                   #{:wrist-rest}
                   (when (= (getopt :wrist-rest :style) :solid) #{:main}))}))))

(defn wrist-rest-rubber-pad-right
  "Right-hand-side wrist-rest pad model. Useful in visualization and
  prototyping, but you would not normally include a print of this in your
  final product, at least not in a hard plastic."
  [getopt]
  (place/wrist-undo getopt
    (maybe/difference
      (mask/above-wrist-bottom-plate getopt
        (wrist/rubber-insert-positive getopt))
      (twanges getopt
        {:include-positive true, :include-top true,
         :bodies (setlib/union
                   #{:wrist-rest}
                   (when (= (getopt :wrist-rest :style) :solid) #{:main}))}))))

(defn- wrist-rest-preview
  "Right-hand-side preview wrist-rest model."
  [getopt]
  (maybe/union
    (wrist-rest-plinth-right getopt)
    (mask/above-wrist-bottom-plate getopt
      (wrist/rubber-insert-positive getopt))))

(defn central-housing
  "The body of the central housing. Not subject to reflection, but generally
  bilaterally symmetrical. Chiral modules are an exception to this symmetry:
  Their positions are mirrored on the left-hand side, but their individual
  models are mirrored again to “undo” the local effects at each new position.
  There is no separate bottom-plate inclusion parameter for the central
  housing; it uses the main body’s parameter for that."
  [getopt]
  (let [bilateral
        (fn ([subject]  ; Non-chiral.
             (reflect-x subject))
            ([subject-right subject-left]  ; Chiral.
             (maybe/union subject-right (flip-x subject-left))))]
    (maybe/union
      (maybe/difference
        (mask/above-main-bottom-plate getopt
          (maybe/difference
            (maybe/union
              (central/main-shell getopt)
              (when (getopt :central-housing :derived :include-lip)
                (bilateral (central/lip-body-right getopt)))
              (when (getopt :central-housing :derived :include-adapter)
                (bilateral (central/adapter-fastener-receivers getopt)))
              (auxf/ports-positive getopt #{:central-housing})
              (twanges getopt {:include-positive true, :include-top true,
                               :bodies #{:central-housing}}))
            (when (getopt :central-housing :derived :include-adapter)
              (bilateral
                (central/adapter-right-fasteners getopt)
                (central/adapter-left-fasteners getopt)))
            (when (getopt :mcu :derived :include-centrally)
              (mcu/negative-composite getopt))
            (auxf/ports-negative getopt #{:central-housing}))
          (when (and (getopt :mcu :derived :include-centrally)
                     (getopt :mcu :support :shelf :include))
            (mcu/shelf-in-place getopt))
          (when (and (getopt :mcu :derived :include-centrally)
                     (getopt :mcu :support :lock :include))
            (mcu/lock-fixture-composite getopt))
          (sandbox/positive getopt))
        (when (getopt :central-housing :derived :include-adapter)
          (bilateral (central/dfm-shim getopt)))
        (custom-negatives getopt :central-housing
          (getopt :main-body :bottom-plate :include)))
      (when (and (getopt :main-body :bottom-plate :include)
                 (getopt :bottom-plates :preview))
        (if (and (getopt :wrist-rest :include)
                 (getopt :wrist-rest :preview)
                 (getopt :wrist-rest :bottom-plate :include))
          (bottom/combined-complete getopt)
          (bottom/case-complete getopt)))
      (when (and (getopt :mcu :derived :include-centrally)
                 (getopt :mcu :preview))
        (mcu/preview-composite getopt)))))

(defn- masked-inner-positive
  "Parts of the keyboard that are subject to a mask and all negatives."
  [getopt]
  (mask/above-main-bottom-plate getopt
    (key/metacluster key/cluster-plates getopt)
    (key/metacluster web/cluster getopt)
    (key/metacluster wall/cluster getopt)
    (auxf/ports-positive getopt #{:main})
    (when (and (getopt :mcu :derived :include-mainly)
               (getopt :mcu :support :shelf :include))
      (mcu/shelf-in-place getopt))
    (when (and (getopt :wrist-rest :include)
               (= (getopt :wrist-rest :style) :threaded))
      (wrist/all-partner-side-blocks getopt))
    (when (getopt :main-body :back-plate :include)
      (auxf/backplate-block getopt))
    (when (getopt :main-body :rear-housing :include)
      (main-body/rear-housing-positive getopt))
    (twanges getopt {:include-positive true, :include-top true,
                     :bodies #{:main}})
    (when (getopt :central-housing :derived :include-adapter)
      (central/adapter-shell getopt))))

(defn- midlevel-positive
  "Parts of the keyboard that go outside the mask but should still be subject
  to all negatives."
  [getopt]
  (maybe/union
    (masked-inner-positive getopt)
    (when (and (getopt :wrist-rest :include)
               (getopt :wrist-rest :preview))
      (mask/above-wrist-bottom-plate getopt
        (wrist-rest-preview getopt)))
    (when (and (getopt :wrist-rest :include)
               (not (getopt :wrist-rest :preview))
               (= (getopt :wrist-rest :style) :solid))
      (wrist-rest-plinth-right getopt))
    (when (and (getopt :main-body :bottom-plate :include)
               (getopt :bottom-plates :preview))
      (if (and (getopt :wrist-rest :include)
               (getopt :wrist-rest :bottom-plate :include)
               (getopt :bottom-plates :combine))
        (bottom/combined-positive getopt)
        (maybe/union
          (bottom/case-positive getopt)
          (when (and (getopt :wrist-rest :include)
                     (getopt :wrist-rest :preview)
                     (getopt :wrist-rest :bottom-plate :include))
            (bottom/wrist-positive getopt)))))
    (sandbox/positive getopt)))

(defn main-body-right
  "Right-hand-side view of the complete main body."
  [getopt]
  (maybe/union
    (maybe/difference
      (maybe/union
        (maybe/difference
          (midlevel-positive getopt)
          ;; First-level negatives:
          (key/metacluster key/cluster-cutouts getopt)
          (key/metacluster key/cluster-channels getopt)
          (when (getopt :central-housing :derived :include-lip)
            ;; Space for an adapter lip, in case the adapter itself is too
            ;; thin.
            (central/lip-body-right getopt))
          (auxf/ports-negative getopt #{:main})
          (when (getopt :mcu :derived :include-mainly)
            (mcu/negative-composite getopt))
          (when (getopt :main-body :leds :include)
            (auxf/led-holes getopt))
          (when (getopt :main-body :back-plate :include)
            (auxf/backplate-fastener-holes getopt))
          (when (and (getopt :wrist-rest :include)
                     (= (getopt :wrist-rest :style) :threaded))
            (wrist/all-fasteners getopt))
          (custom-negatives getopt :main false))
        ;; Outer positives, subject only to outer negatives:
        (when (and (getopt :mcu :derived :include-mainly)
                   (getopt :mcu :support :lock :include))
          (mcu/lock-fixture-composite getopt)))
      ;; Outer negatives:
      (when (getopt :main-body :rear-housing :include)
        (main-body/rear-housing-mount-negatives getopt))
      (when (getopt :central-housing :derived :include-adapter)
        (central/negatives getopt))
      (when (and (getopt :wrist-rest :include)
                 (getopt :wrist-rest :preview)
                 (getopt :wrist-rest :bottom-plate :include))
        (twanges getopt {:include-top true, :include-negative true,
                         :bodies #{:wrist-rest}})))
    ;; The remaining elements are visualizations for use in development.
    (when (getopt :keys :preview)
      (key/metacluster key/cluster-keycaps getopt))
    (when (and (getopt :mcu :derived :include-mainly)
               (getopt :mcu :preview))
      (mcu/preview-composite getopt))
    (when (and (getopt :central-housing :derived :include-main)
               (getopt :central-housing :preview))
      (central-housing getopt))))
