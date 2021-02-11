;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Parameter Specification – Flanges                                   ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.param.tree.flange
  (:require [clojure.spec.alpha :as spec]
            [scad-tarmi.core :as tarmi-core]
            [dactyl-keyboard.param.base :as base]
            [dactyl-keyboard.param.schema.anch :as anch]
            [dactyl-keyboard.param.schema.parse :as parse]
            [dactyl-keyboard.param.schema.valid :as valid]
            [dactyl-keyboard.param.stock :as stock]))


(def raws
  "A flat version of the specification for configuring one flange."
  [["# Flange configuration options\n\n"
    "Each heading in this document represents a recognized configuration key "
    "in [YAML files for the DMOTE application](configuration.md).\n\n"
    "This specific document describes options for any individual flange. "
    "One set of such options will exist for each entry in `flanges`, a section "
    "whose place in the larger hierarchy can be seen [here](options-main.md).\n"
    "\n"
    "Example uses for flanges are:\n"
    "\n"
    "- Connecting a custom body to its parent body.\n"
    "- Connecting a bottom plate to its parent body.\n"
    "- Providing hardpoints for attaching other stuff, inside or outside a keyboard.\n"
    "\n"
    "Flanges most often do this mainly by reserving negative space for "
    "threaded fasteners. The feature is named after pipe flanges joined by "
    "such fasteners."]
   [[:include]
    {:default false :parse-fn boolean :validate [::valid/include]}
    "If `true`, include those parts of the flange that are in turn marked with "
    "`include` in their own subsections."]
   [[:reflect]
    {:default false :parse-fn boolean}
    "If `true`, mirror every part of the flange around x = 0. For example, "
    "you might use this to get identical fasteners on both sides of a central
    housing."]
   [[:body]
    {:default :auto :parse-fn keyword :validate [::valid/body]}
    "A code identifying the [body](configuration.md) to which the flange "
    "belongs.\n\n"
    "It is not necessary for an entire flange to belong to a single body. "
    "With the default value (`auto`) for this parameter, the body membership "
    "of each part of the flange is determined separately, based on its "
    "individual anchoring."]
   [[:bottom]
    {:default false :parse-fn boolean}
    "If `true`, treat this flange as connecting its parent body to that "
    "body’s bottom plate. This has the following side effects:\n"
    "\n"
    "- The flange will be disabled if its parent body is configured to have "
    "no bottom plate, even if the flange itself has `include: true`.\n"
    "- Each part of the flange is turned to face straight up, because all the "
    "bolts in an ordinary bottom flange enter straight through the bottom "
    "plate from below.\n"
    "- Anchoring becomes two-dimensional, so that, in the absence of any "
    "explicit offset you specify, each part of the flange sits at floor level "
    "beneath its `anchor`.\n"
    "- Bottom flanges, when rendered as part of bottom plates but not when "
    "rendered as part of the case, are affected by `fastener-plate-offset`, "
    "a DFM parameter described [here](options-main.md).\n"
    "- For performance reasons, the application will select whether to "
    "include each flange based on the `bottom` attribute so that, for example, "
    "top flanges will not affect a bottom plate even if their position "
    "intersects the plate."]
   [[:bolts]
    "Flanges typically connect two parts of a keyboard by means of threaded "
    "fasteners."]
   [[:bolts :include]
    {:default false :parse-fn boolean}
    "If `true`, reserve negative space for fasteners."]
   [[:bolts :bolt-properties]
    stock/implicit-threaded-bolt-metadata
    stock/threaded-bolt-documentation]
   [[:inserts]
    "If you are not printing or tapping threads, consider cylindrical
    heat-set inserts to receive bolts, as used in the original
    Dactyl-ManuForm."]
   [[:inserts :include]
    {:default false :parse-fn boolean}
    "If `true`, reserve negative space for inserts."
    "This is a subsection with two parameters: `top` and `bottom`."]
   [[:inserts :height]
    {:default 0 :parse-fn num}
    "The distance in mm between the flange’s position and the bottom of "
    "the insert."]
   [[:inserts :length]
    {:default 1 :parse-fn num}
    "The length in mm of each insert."]
   [[:inserts :diameter]
    "The diameter in mm of each insert. "]
   [[:inserts :diameter :top]
    {:default 1 :parse-fn num}
    "Top diameter. Consider making this slightly smaller than the real "
    "diameter to ensure there`s enough material to secure the insert."]
   [[:inserts :diameter :bottom]
    {:default 1 :parse-fn num}
    "Bottom diameter. This is the diameter of the insert at the side closest "
    "to the flange’s stated position."]
   [[:bosses]
    "Both bolts and heat-set inserts for bolts require positive space to "
    "hold on to. Where there is enough material around them, extra space can "
    "be created using `bosses`."]
   [[:bosses :include]
    {:default false :parse-fn boolean}
    "If `true`, automatically add bosses in the form of loft sequences "
    "encompassing all of the defined segments of each boss.\n"
    "\n"
    "If `false` (default), you can still get bosses, but you will need to "
    "give each position of the flange an alias and then target it with "
    "`tweaks`."]
   [[:bosses :segments]
    (let [local
          [[]  ; This header will not be rendered and is therefore empty.
           [[:style]
            {:default :cylinder
             :parse-fn keyword
             :validate [#{:cube :cylinder :sphere}]}]
           [[:size]
            {:default 1
             :parse-fn parse/pad-to-3-tuple
             :validate [::tarmi-core/point-3d]}]
           [[:intrinsic-offset]
            ;; Future development: Consider adding support for z-axis formulae
            ;; referencing different parts of the screw (head-to-body
            ;; transition, threaded-to-unthreaded, tip). That was default
            ;; behaviour in v0.6.0.
            {:default [0 0 0]
             :parse-fn (parse/tuple-of num)
             :validate [::tarmi-core/point-3d]}]]]
      {:default {0 (base/extract-defaults local)}
       :parse-fn (parse/map-of parse/integer (base/parser-with-defaults local))
       :validate [(spec/map-of integer?  (base/delegated-validation local))]})
    "A map of numeric segment IDs to segment properties. Numbering starts at "
    "zero and in YAML, segment IDs that are map keys must be enclosed in quotes.\n"
    "\n"
    "The recognized properties of a segment are:\n"
    "- `style`: The shape of the segment. "
    "One of `cylinder` (default), `cube` or `sphere`.\n"
    "- `size`: The measurements of the segment, in mm.\n"
    "- `intrinsic-offset`: An xyz-offset in mm from the previous segment or, "
    "in the case of segment zero, from the flange position itself.\n"]
   [[:positions]
    (let [local
          [[]  ; This header will not be rendered and is therefore empty.
           [[:boss-alias]
            stock/alias-metadata]
           [[:reflect]
            {:default false :parse-fn boolean}]
           [[:anchoring]
            anch/anchoring-metadata]]]
      {:default []
       :parse-fn (parse/tuple-of (base/parser-with-defaults local))
       :validate [(spec/coll-of (base/delegated-validation local))]})
    "A list of all the places on the keyboard where the parts of the flange "
    "will appear.\n"
    "\n"
    "A position is a map recognizing the following properties:\n"
    "\n"
    "- `boss-alias`: A name for the position’s boss, for use in `tweaks`. "
    "In the case of reflected positions, this alias refers to the original, "
    "not the mirror image.\n"  ; So named because an insert-alias may be added later.
    "- `reflect`: If `true`, mirror this part of the flange around x = 0.\n"
    "- `anchoring`: Room for standard anchoring parameters."]])
