;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Parameter Specification – Wrist Rest Fastener Posts                 ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.param.tree.restmnt
  (:require [clojure.spec.alpha :as spec]
            [dactyl-keyboard.param.schema.parse :as parse]
            [dactyl-keyboard.param.schema.valid :as valid]
            [dactyl-keyboard.param.stock :as stock]))


(def raws
  "A flat version of a special part of a user configuration.
  Default values and parsers here are secondary. Validators are used."
  [["# Options for connecting a wrist rest\n\n"
    "Each heading in this document represents a recognized configuration key "
    "in YAML files for a DMOTE variant.\n\n"
    "This specific document describes options for each “mount”, a pair of "
    "cuboid blocks used to anchor threaded fasteners for the `threaded` style "
    "of wrist rest, a parameter documented [here](options-main.md)."]
   [:section [:fasteners]
    "Threaded fasteners in the mount."]
   [:parameter [:fasteners :amount]
    {:default 1 :parse-fn int}
    "The number of vertically stacked screws in the mount. 1 by default."]
   [:parameter [:fasteners :bolt-properties]
    stock/implicit-threaded-bolt-metadata
    stock/threaded-bolt-documentation]
   [:section [:fasteners :height]
    "The vertical level of the fasteners."]
   [:parameter [:fasteners :height :first]
    {:default 0 :parse-fn int}
    "The distance in mm from the bottom of the first fastener "
    "down to the ground level of the model."]
   [:parameter [:fasteners :height :increment]
    {:default 0 :parse-fn num}
    "The vertical distance in mm from the center of each fastener to the "
    "center of the next."]
   [:parameter [:anchoring]
    {:default :main-side
     :parse-fn keyword
     :validate [::valid/wrist-position-style]}
    "One of:\n\n"
    "- `main-side`: The `angle` parameter in this section determines the angle "
    "of the blocks and threaded fasteners in the mount. In effect, the "
    "plinth-side block is placed by `angle` and `distance`, while its own "
    "explicit `anchoring` section of parameters is ignored.\n"
    "- `mutual`: The `angle` and `distance` parameters are ignored. Each "
    "block is anchored to a separate and independent feature. The angle and "
    "distance between these two features determines the angle of the "
    "fasteners and the distance between the blocks."]
   [:parameter [:angle]
    {:default 0 :parse-fn num}
    "The angle in radians of the mount, on the xy plane, counter-clockwise "
    "from the y axis. This parameter is only used with `main-side` anchoring."]
   [:section [:blocks]
    "Blocks for anchoring threaded fasteners."]
   [:parameter [:blocks :distance]
    {:default 0 :parse-fn num}
    "The distance in mm between the two posts in a mount. "
    "This parameter is only used with `main-side` anchoring."]
   [:parameter [:blocks :width]
    {:default 1 :parse-fn num}
    "The width in mm of the face or front bezel on each "
    "block that will anchor a fastener."]
   [:section [:blocks :main-side]
    "A block on the side of the keyboard case is mandatory."]
   [:section [:blocks :main-side :anchoring]
    "Where to place the block."]
   [:parameter [:blocks :main-side :anchoring :anchor]
    {:default :origin :parse-fn keyword :validate [::valid/anchor]}
    "An alias referring to a feature that anchors the block."]
   [:parameter [:blocks :main-side :anchoring :side]
    stock/anchor-side-metadata
    stock/anchor-side-documentation]
   [:parameter [:blocks :main-side :anchoring :segment]
    stock/anchor-segment-metadata
    stock/anchor-segment-documentation]
   [:parameter [:blocks :main-side :anchoring :offset]
    stock/anchor-2d-vector-metadata
    stock/anchor-2d-offset-documentation]
   [:parameter [:blocks :main-side :depth]
    {:default 1 :parse-fn num}
    "The thickness of the block in mm along the axis of the fastener(s)."]
   [:section [:blocks :main-side :nuts]
    "Extra features for threaded nuts on the case side."]
   [:section [:blocks :main-side :nuts :bosses]
    "Nut bosses on the rear (interior) of the mount. You may want this if the "
    "distance between case and plinth is big enough for a nut. If that "
    "distance is too small, bosses can be counterproductive."]
   [:parameter [:blocks :main-side :nuts :bosses :include]
    {:default false :parse-fn boolean}
    "If `true`, include bosses."]
   [:section [:blocks :plinth-side]
    "A block on the side of the wrist rest."]
   [:section [:blocks :plinth-side :anchoring]
    "Where to place the block. This entire section is ignored in the "
    "`main-side` style of anchoring."]
   [:parameter [:blocks :plinth-side :anchoring :anchor]
    {:default :origin :parse-fn keyword :validate [::valid/anchor]}
    "An alias referring to a feature that anchors the block. Whereas the "
    "main-side mount is typically anchored to a key, the plinth-side mount "
    "is typically anchored to a named point on the plinth."]
   [:parameter [:blocks :plinth-side :anchoring :offset]
    {:default [0 0] :parse-fn vec}
    "An offset in mm from the named feature to the block."]
   [:parameter [:blocks :plinth-side :depth]
    {:default 1 :parse-fn num}
    "The thickness of the mount in mm along the axis of the fastener(s). "
    "This is typically larger than the main-side depth to allow adjustment."]
   [:parameter [:blocks :plinth-side :pocket-height]
    {:default 0 :parse-fn num}
    "The height of the nut pocket inside the mounting plate, in mm.\n\n"
    "With a large positive value, this will provide a chute for the nut(s) "
    "to go in from the top of the plinth, which allows you to hide the hole "
    "beneath the pad. With a large negative value, the pocket will "
    "instead open from the bottom, which is convenient if `depth` is small. "
    "With a small value or the default value of zero, it will be necessary to "
    "pause printing in order to insert the nut(s); this last option is "
    "therefore recommended for advanced users only."]
   [:parameter [:blocks :aliases]
    {:default {}
     :parse-fn (parse/map-of keyword keyword)
     :validate [(spec/map-of keyword? ::valid/wrist-block)]}
    "A map of short names to specific blocks, i.e. `main-side` or "
    "`plinth-side`. Such aliases are for use elsewhere in the configuration."]])
