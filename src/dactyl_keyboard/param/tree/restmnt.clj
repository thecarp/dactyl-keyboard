;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Parameter Specification – Wrist Rest Fastener Posts                 ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.param.tree.restmnt
  (:require [clojure.spec.alpha :as spec]
            [dactyl-keyboard.param.schema.anch :as anch]
            [dactyl-keyboard.param.schema.parse :as parse]
            [dactyl-keyboard.param.schema.valid :as valid]
            [dactyl-keyboard.param.stock :as stock]))


(def raws
  "A flat version of a special part of a user configuration.
  Default values and parsers here are secondary. Validators are used."
  [["# Options for connecting a wrist rest\n\n"
    "Each heading in this document represents a recognized configuration key "
    "in [YAML files for the DMOTE application](configuration.md).\n\n"
    "This specific document describes options for each “mount”, a pair of "
    "cuboid blocks used to anchor threaded fasteners for the `threaded` style "
    "of wrist rest, a parameter documented [here](options-main.md). "
    "Each block in the pair is part of a different body: One is part of the "
    "wrist rest, and the other a “partner” body."]
   [[:fasteners]
    "Threaded fasteners in the mount."]
   [[:fasteners :amount]
    {:default 1 :parse-fn int}
    "The number of vertically stacked screws in the mount. 1 by default."]
   [[:fasteners :bolt-properties]
    stock/implicit-threaded-bolt-metadata
    stock/threaded-bolt-documentation]
   [[:fasteners :heights]
    {:default [] :parse-fn vec :validate [(spec/coll-of number?)]}
    "A list of heights in mm, above the ground level. "
    "Each number describes the level of a set of fasteners: The centre of one "
    "threaded rod and any nuts etc. attaching it."]
   [[:authority]
    {:default :partner-side
     :parse-fn keyword
     :validate [::valid/wrist-position-style]}
    "One of:\n\n"
    "- `partner-side`: The `angle` parameter in this section determines the "
    "angle of the blocks and threaded fasteners in the mount. In effect, the "
    "wrist-side block is placed by `angle` and `distance`, while its own "
    "explicit `anchoring` section of parameters is ignored.\n"
    "- `mutual`: The `angle` and `distance` parameters are ignored. Each "
    "block is anchored independently. The angle and distance between the "
    "blocks determines the angle of the fasteners."]
   [[:angle]
    {:default 0 :parse-fn parse/compass-compatible-angle}
    "The angle in radians of the mount, on the xy plane, counter-clockwise "
    "from the y axis. This parameter is only used with `partner-side` anchoring."]
   [[:blocks]
    "Blocks for anchoring threaded fasteners."]
   [[:blocks :distance]
    {:default 0 :parse-fn num}
    "The distance in mm between the two posts in a mount. "
    "This parameter is only used with `partner-side` authority."]
   [[:blocks :width]
    {:default 1 :parse-fn num}
    "The width in mm of each block that will hold a fastener."]
   [[:blocks :partner-side]
    "A block on the side of the partner body is mandatory."]
   [[:blocks :partner-side :body]
    {:default :auto :parse-fn keyword :validate [::valid/body]}
    "A code identifying the [body](configuration.md) that houses the block."]
   [[:blocks :partner-side :anchoring]
    anch/anchoring-metadata
    "Where on the ground to place the block. "
    stock/anchoring-documentation]
   [[:blocks :partner-side :depth]
    {:default 1 :parse-fn num}
    "The thickness of the block in mm along the axis of the fastener(s)."]
   [[:blocks :wrist-side]
    "A block on the side of the wrist rest."]
   [[:blocks :wrist-side :anchoring]
    anch/anchoring-metadata
    "Where on the ground to place the block, as for the partner side. "
    "This entire section is ignored with `partner-side` authority."]
   [[:blocks :wrist-side :depth]
    {:default 1 :parse-fn num}
    "The thickness of the mount in mm along the axis of the fastener(s)."]
   [[:aliases]
    "Short names for different parts of the mount, for use elsewhere in "
    "the application."]
   [[:aliases :blocks]
    {:default {}
     :parse-fn (parse/map-of keyword keyword)
     :validate [(spec/map-of ::valid/alias ::valid/wrist-block)]}
    "A map of short names to specific blocks as such, i.e. `partner-side` or "
    "`wrist-side`."]
   [[:aliases :nuts]
    {:default {}
     :parse-fn (parse/map-of keyword (parse/tuple-of parse/keyword-or-integer))
     :validate [(spec/map-of ::valid/alias
                             (spec/tuple ::valid/wrist-block integer?))]}
    "A map of short names to nuts. Nuts are identified by tuples "
    "(lists of two items) where each tuple names a block, i.e. "
    "`partner-side` or `wrist-side`, and indexes a fastener in the `heights` "
    "list above. Indexing starts from zero.\n"
    "\n"
    "This parameter is used to name nuts to go on each end of each threaded rod. "
    "The intended use for this is with negative-space `tweaks`, where you target "
    "each nut by its name and supply `cut: true`. Some recipes:\n"
    "\n"
    "- To get a cavity for a nut wholly inside a block, just target the nut "
    "for a tweak without an offset or other special arguments. "
    "It will be necessary to pause printing in order to insert "
    "the nut in such a cavity.\n"
    "- To get a pocket for sliding in a nut from the top of the mount, "
    "hull a nut in its place with the same nut, offset higher on the z axis. "
    "Design the pad of the wrist rest to cover the pocket.\n"
    "- To get a similar pocket that opens from the bottom, target a "
    "nut in place with `at-ground`. Use a bottom plate to hide the pocket.\n"
    "- To get a nut boss instead of a pocket, offset a nut on the y axis. "
    "This is also useful with hex-head bolts."]])
