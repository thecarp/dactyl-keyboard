;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Parameter Specification – Nestables                                 ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.param.tree.nested
  (:require [scad-tarmi.core :as tarmi-core]
            [dactyl-keyboard.param.schema.parse :as parse]))

(def raws
  "A flat version of a special part of a user configuration."
  [["# Nestable configuration options\n\n"
    "This document describes all those settings which can be made at any "
    "level of specificity, from the entire keyboard down to one side of one "
    "key. These settings all go under `by-key` in a YAML file, as indicated "
    "[here](options-main.md).\n"
    "\n"
    "## Conceptual overview\n"
    "\n"
    "The `by-key` section contains a map of up to five items:\n"
    "\n"
    "- `parameters`, where you put actual settings for the entire keyboard. "
    "Sections described in this document all pertain to this map.\n"
    "- `clusters`, starting a nested map for specific clusters only, "
    "keyed by their names.\n"
    "- `columns` and/or `rows`, each starting a nested map for specific "
    "columns or rows only, keyed either by their indices (ordinal integers) "
    "or by the special words `first` or `last`. Due to a peculiarity of the "
    "YAML parser, **numeric indices must appear in quotation marks** as in the "
    "example below.\n"
    "- `sides`, starting a nested map for specific sides only, "
    "keyed by the long-form cardinal points of the compass, i.e. the words "
    "`north`, `east`, `south` or `west`.\n"
    "\n"
    "Each of the nested maps have the same structure as this root-level map. "
    "Greater specificity is accomplished by nesting a series of these maps.\n"
    "\n"
    "### Example\n"
    "\n"
    "In the following example, the parameter `key-style` is set three times: "
    "Once at the root level and twice with enough selection criteria to limit "
    "the effect to two individual keys.\n"
    "\n"
    "```by-key:\n"
    "  parameters:\n"
    "    key-style: plump\n"
    "  clusters:\n"
    "    C:\n"
    "      columns:\n"
    "        \"1\":\n"
    "          rows:\n"
    "            first:\n"
    "              parameters:\n"
    "                key-style: svelte\n"
    "            \"3\":\n"
    "              parameters:\n"
    "                key-style: svelte\n```"
    "\n\n"
    "In this example, `key-style` will have the value `plump` for all keys "
    "except two. It will have the value `svelte` for the key closest to the "
    "user (first row) and the key three steps above the home row (row 3), both "
    "in the second column from the left (column 1) of key cluster `C`.\n"
    "\n"
    "Key cluster `C` and the two key styles must be defined elsewhere. Also, "
    "if the keyboard uses reflection, notice that the descriptions given in "
    "the previous paragraph must be mirrored for the left-hand side of the "
    "keyboard.\n"
    "\n"
    "### Unique selection criteria\n"
    "\n"
    "The nested maps named `clusters`, `columns`, `rows` and `sides`, "
    "together with the identifiers they take as keys, are selection criteria. "
    "The order in which these criteria "
    "appear is significant, but only under the following circumstances.\n"
    "\n"
    "- The special words `first` and `last` can only be used when nested "
    "inside a cluster-specific configuration, because they are informed by "
    "the cluster matrix specification.\n"
    "- As a further requirement for rows only, `first` and `last` can only "
    "be used when nested inside a column-specific configuration, as in the "
    "example above.\n"
    "\n"
    "Each set of criteria must be unique. That is to say, each set of "
    "`parameters` must describe some new part of the keyboard. "
    "For instance, you can have either `by-key` → `rows` → `"0"` → `columns`"
    "→ `"0"` → `parameters` *or* `by-key` → `columns` → `"0"` → `rows` → `"0"`"
    "→ `parameters`, but you cannot have both. They are equivalent because "
    "they amount to identical selection criteria.\n"
    "\n"
    "This restriction applies even to the use of `first` and `last` with "
    "numeric indices. If column 8 is the last column, you can refer to it "
    "either way, but there cannot be two exactly equivalent sets of criteria, "
    "even if one uses `"8"` and the other uses the special keyword `last` to "
    "select column 8.\n"
    "\n"
    "### Specificity\n"
    "\n"
    "The application picks the most specific value available. "
    "Specificity is determined by permutations of selection criteria. "
    "More specifically, each criterion is switched on and off, starting "
    "with the least significant. Rows are considered less "
    "significant than columns for this purpose. The complete hiearchy is "
    "clusters > colums > rows > sides.\n"
    "\n"
    "An example: To find the right `key-style` "
    "for a key at coordinates `[0, -1]` in a cluster named `C`, "
    "the application checks for `parameters` → `key-style` with the "
    "following criteria, using the first setting it finds.\n"
    "\n"
    "- Cluster `C`, column 0, row -1.\n"
    "- Cluster `C`, column 0, without any specific row selector.\n"
    "- Cluster `C`, row -1, without any specific column.\n"
    "- Cluster `C`, without any specific column or row.\n"
    "- No specific cluster, column 0, row -1.\n"
    "- No specific cluster, column 0, no specific row.\n"
    "- No specific cluster or column, row -1.\n"
    "- No specific cluster, column or row.\n"
    "\n"
    "The first item in this example requires a setting specific to the "
    "individual key under consideration. "
    "The last item is at the opposite end of the spectrum of specificity: "
    "It reaches the root level, specifically looking at the "
    "setting `by-key` → `parameters` → `key-style`. This root level serves "
    "as a global fallback. It’s the only level with any default values.\n"
    "\n"
    "Notice that `sides` are ignored in the example above, because "
    "setting a `key-style` for just one side of a key, though it is permitted, "
    "is overly specific and therefore meaningless. You can compare it to "
    "a decision to paint one atom.\n"
    "\n"
    "Here follows the complete order of resolution in an extended example, "
    "for a `wall` of the same key as above. Where walls are concerned, the "
    "side of the key *would* be relevant, so its gets included in the "
    "permutations, from most to least specific.\n"
    "\n"
    "- Cluster `C`, column 0, row -1, west side.\n"
    "- Cluster `C`, column 0, row -1, no side.\n"
    "- Cluster `C`, column 0, no row, west side.\n"
    "- Cluster `C`, column 0, no row, no side.\n"
    "- Cluster `C`, no column, row -1, west side.\n"
    "- Cluster `C`, no column, row -1, no side.\n"
    "- Cluster `C`, no column, no row, west side.\n"
    "- Cluster `C`, no column, no row, no side.\n"
    "- No cluster, column 0, row -1, west side.\n"
    "- No cluster, column 0, row -1, no side.\n"
    "- No cluster, column 0, no row, west side.\n"
    "- No cluster, column 0, no row, no side.\n"
    "- No cluster, no column, row -1, west side.\n"
    "- No cluster, no column, row -1, no side.\n"
    "- No cluster, no column, no row, west side.\n"
    "- No cluster, no column, no row, no side.\n"]
   [:section [:layout]
    "Settings for how to place keys."]
   [:section [:layout :matrix]
    "Roughly how keys are spaced out to form a matrix."]
   [:section [:layout :matrix :neutral]
    "The neutral point in a column or row is where any progressive curvature "
    "both starts and has no effect."]
   [:parameter [:layout :matrix :neutral :column]
    {:default 0 :parse-fn int}
    "An integer column ID."]
   [:parameter [:layout :matrix :neutral :row]
    {:default 0 :parse-fn int}
    "An integer row ID."]
   [:section [:layout :matrix :separation]
    "Tweaks to control the systematic separation of keys. The parameters in "
    "this section will be multiplied by the difference between each affected "
    "key’s coordinates and the neutral column and row."]
   [:parameter [:layout :matrix :separation :column]
    {:default 0 :parse-fn num}
    "A distance in mm."]
   [:parameter [:layout :matrix :separation :row]
    {:default 0 :parse-fn num}
    "A distance in mm."]
   [:section [:layout :pitch]
    "Tait-Bryan pitch, meaning the rotation of keys around the x axis."]
   [:parameter [:layout :pitch :base]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians. Set at cluster level, this controls the general "
    "front-to-back incline of the key cluster."]
   [:parameter [:layout :pitch :intrinsic]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians. Intrinsic pitching occurs early in key placement. "
    "It is typically intended to produce a tactile break between two rows of "
    "keys, as in the typewriter-like terracing common on flat keyboards with "
    "OEM-profile or similarly angled caps."]
   [:parameter [:layout :pitch :progressive]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians. This progressive pitch factor bends columns "
    "lengthwise. If set to zero, columns are flat."]
   [:section [:layout :roll]
    "Tait-Bryan roll, meaning the rotation of keys around the y axis."]
   [:parameter [:layout :roll :base]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians. This is the “tenting” angle. Applied to your main "
    "cluster on a split keyboard, it controls the overall left-to-right tilt "
    "of each half."]
   [:parameter [:layout :roll :intrinsic]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians, analogous to intrinsic pitching. Where more than "
    "one column of keys is devoted to a single finger at the edge of the "
    "keyboard, this can help make the edge column easier to reach, reducing "
    "the need to bend the finger (or thumb) sideways."]
   [:parameter [:layout :roll :progressive]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians. This progressive roll factor bends rows "
    "lengthwise, which also gives the columns a lateral curvature."]
   [:section [:layout :yaw]
    "Tait-Bryan yaw, meaning the rotation of keys around the z axis."]
   [:parameter [:layout :yaw :base]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians. Applied to your main key cluster, this serves the "
    "purpose of allowing the user to keep their wrists straight even if the "
    "two halves of the keyboard are closer together than the user’s shoulders."]
   [:parameter [:layout :yaw :intrinsic]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians, analogous to intrinsic pitching."]
   [:section [:layout :translation]
    "Translation in the geometric sense, displacing keys in relation to each "
    "other. Depending on when this translation takes places, it may have a "
    "a cascading effect on other aspects of key placement. All measurements "
    "are three-dimensional vectors in mm."]
   [:parameter [:layout :translation :early]
    {:default [0 0 0] :parse-fn vec :validate [::tarmi-core/point-3d]}
    "”Early” translation happens before other operations in key placement and "
    "therefore has the biggest knock-on effects."]
   [:parameter [:layout :translation :mid]
    {:default [0 0 0] :parse-fn vec :validate [::tarmi-core/point-3d]}
    "This happens after columns are styled but before base pitch and roll. "
    "As such it is a good place to adjust whole columns for relative finger "
    "length."]
   [:parameter [:layout :translation :late]
    {:default [0 0 0] :parse-fn vec :validate [::tarmi-core/point-3d]}
    "“Late” translation is the last step in key placement and therefore "
    "interacts very little with other steps."]
   [:parameter [:key-style]
    {:default :default :parse-fn keyword}
    "The name of a key style defined in the [global](options-main.md) `keys` "
    "section. The default setting is the name `default`."]
   [:section [:channel]
    "Above each switch mount, there is a channel of negative space for the "
    "user’s finger and the keycap to move inside. This is only useful in those "
    "cases where nearby walls or webbing between mounts on the keyboard would "
    "otherwise obstruct movement."]
   [:parameter [:channel :height]
    {:default 1 :parse-fn num}
    "The height in mm of the negative space, starting from the "
    "bottom edge of each keycap in its pressed (active) state."]
   [:parameter [:channel :top-width]
    {:default 0 :parse-fn num}
    "The width in mm of the negative space at its top. Its width at the "
    "bottom is defined by keycap geometry."]
   [:parameter [:channel :margin]
    {:default 0 :parse-fn num}
    "The width in mm of extra negative space around the edges of a keycap, on "
    "all sides. This is applied before the `error-general` DFM compensator."]
   [:section [:wall]
    "Properties of a wall built around the edge of the cluster.\n"
    "\n"
    "The walls of the keyboard case support the key mounts and protect the "
    "electronics. They are generated by an algorithm that walks around each "
    "key cluster, optionally complemented by `tweaks`.\n"
    "\n"
    "The `wall` section determines the shape of the case wall, specifically "
    "the skirt around each key mount along the edges of the board. These "
    "skirts are made up of convex hulls wrapping sets of corner posts.\n"
    "\n"
    "There is one corner post at each actual corner of every key mount "
    "(segment 0). "
    "More posts are displaced from it, going down the sides. Their placement "
    "is affected by the way the key mounts are rotated etc.\n"]
   [:parameter [:wall :thickness]
    {:default [1 1 1] :parse-fn parse/pad-to-3-tuple
     :validate [::tarmi-core/point-3d]}
    "The size in mm of the key mount and each wall post.\n\n"
    "Notice that the unit size of each key, and therefore the horizontal "
    "extent of each key mounting plate, is a function of `key-style`, not of "
    "this parameter.\n\n"
    "The `thickness` parameter instead controls three other aspects of the "
    "keyboard case:\n\n"
    "- The thickness of the key mounting plate itself. When specifying "
    "`thickness` as a list of three dimensions (`[1, 2, 3]`), mounting plate "
    "thickness is governed solely by the z-axis dimension (`3`) and the other "
    "figures are ignored.\n"
    "- The thickness of the walls as drawn automatically and as targeted by "
    "`tweaks`. Whereas mounting-plate thickness ignores the x- and y-axis "
    "dimensions of this parameter, wall posts are cuboids that use all three "
    "dimensions."]
   [:parameter [:wall :extent]
    {:default 0 :parse-fn num :validate [(set (range 4))]}
    "A segment ID describing how far away from the key mount to extend its "
    "wall. Note that even if this is set low, you can still use `tweaks` to "
    "target other segments."]
   [:parameter [:wall :to-ground]
    {:default false :parse-fn boolean}
    "If `true`, draw one extra, vertical section of wall between the segment "
    "identified in `extent` and the ground beneath the key."]
   [:parameter [:wall :bevel]
    {:default 0 :parse-fn num}
    "A distance in mm, describing where to place some vertical segments.\n"
    "\n"
    "The `bevel` is applied at the top of a wall, making up the difference "
    "between wall segments 0 and 1. It is applied again at the bottom, making "
    "up the difference between segments 2 and 3. It affects all "
    "coordinates. The mathematical operation by which it is applied to the z "
    "coordinate is determined by the sign of `perpendicular`."]
   [:parameter [:wall :parallel]
    {:default 0 :parse-fn num}
    "A distance in mm. Wall segments 2 and 3 extend this far "
    "away from the corners of their key mount, on its plane."]
   [:parameter [:wall :perpendicular]
    {:default 0 :parse-fn num}
    "A distance in mm. Wall segments 2 and 3 extend this far "
    "away from the corners of their key mount, along its normal."]])
