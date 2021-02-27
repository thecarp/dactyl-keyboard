;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Dactyl-ManuForm Keyboard — Opposable Thumb Edition              ;;
;; Parameter Specification – Nestables                                 ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns dactyl-keyboard.param.tree.nested
  (:require [clojure.spec.alpha :as spec]
            [scad-tarmi.core :as tarmi-core]
            [dactyl-keyboard.param.base :as base]
            [dactyl-keyboard.param.schema.parse :as parse]))

(def raws
  "A flat version of a special part of a user configuration."
  [["# Nestable key configuration options\n\n"
    "This document describes all those settings which can be made at any "
    "level of specificity, from the entire keyboard down to one side of one "
    "key. These settings all go under `by-key` in a YAML file, as indicated "
    "[here](options-main.md).\n"
    "\n"
    "## Conceptual overview\n"
    "\n"
    "The `by-key` section contains a map of up to five items:\n"
    "\n"
    "- `parameters`, where you put your settings. "
    "Sections described in this document all pertain to this map.\n"
    "- `clusters`, starting a nested map for specific clusters only, "
    "keyed by their names.\n"
    "- `columns` and/or `rows`, each starting a nested map for specific "
    "columns or rows only, keyed either by their indices (ordinal integers) "
    "or by the special words `first` or `last`. Due to a peculiarity of "
    "YAML (and JSON), **numeric indices must appear in quotation marks** "
    "as in the example below.\n"
    "- `sides`, starting a nested map for points of the compass, such as "
    "`NNE` for the north by northeast corner. Sides are only relevant for "
    "the periphery of a key, including details of the wall surrounding a "
    "key mount at the edge of a cluster, and the webbing between keys.\n"
    "\n"
    "Each of the nested maps have the same structure as this root-level map. "
    "Specificity is accomplished by nesting a series of these maps, so that "
    "a nested set of `parameters` comes to refer to an intersection of more "
    "than one selection criterion.\n"
    "\n"
    "### Example\n"
    "\n"
    "In the following example, the parameter `key-style` is set three times: "
    "Once at the root level and twice with enough selection criteria to limit "
    "the effect to two individual keys.\n"
    "\n"
    "```\n"
    "by-key:\n"
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
    "in the second column from the left (i.e. column 1) of key cluster `C`.\n"
    "\n"
    "Key cluster `C` and the two key styles must be defined elsewhere. Also, "
    "if the keyboard uses reflection, notice that the descriptions given in "
    "the previous paragraph would be incorrect for the left-hand side of the "
    "keyboard, because that side would be mirrored.\n"
    "\n"
    "### Comparison to anchoring\n"
    "\n"
    "The `by-key` section can place any key anywhere. However, please don’t. "
    "Look first to the `key-clusters` section, which is intended to define "
    "the most basic shape and position of each cluster. "
    "`key-clusters` allows for standard [anchoring](options-anchoring.md), "
    "while `by-key` does not.\n"
    "\n"
    "Please use `key-clusters` to translate and rotate each cluster. "
    "More concretely, prefer `key-clusters` over `translation` and over "
    "base `pitch`, `roll` and `yaw` at a global or cluster level of `by-key`. "
    "Doing so is easier, because standard anchoring allows you to describe "
    "rotation on all three axes together, while keeping values in `by-key` "
    "smaller and neatly unentangled.\n"
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
   [[:key-style]
    {:default :default :parse-fn keyword}
    "The name of a key style defined in the [global](options-main.md) `keys` "
    "section. The default setting is the name `default`."]
   [[:layout]
    "Settings for how to place keys."]
   [[:layout :matrix]
    "Roughly how keys are spaced out to form a matrix."]
   [[:layout :matrix :neutral]
    "The neutral point in a column or row is where any progressive curvature "
    "both starts and has no effect."]
   [[:layout :matrix :neutral :column]
    {:default 0 :parse-fn int}
    "An integer column ID."]
   [[:layout :matrix :neutral :row]
    {:default 0 :parse-fn int}
    "An integer row ID."]
   [[:layout :matrix :separation]
    "Tweaks to control the systematic separation of keys. The parameters in "
    "this section will be multiplied by the difference between each affected "
    "key’s coordinates and the neutral column and row."]
   [[:layout :matrix :separation :column]
    {:default 0 :parse-fn num}
    "A distance in mm."]
   [[:layout :matrix :separation :row]
    {:default 0 :parse-fn num}
    "A distance in mm."]
   [[:layout :pitch]
    "Tait-Bryan pitch, meaning the rotation of keys around the x axis."]
   [[:layout :pitch :base]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians, controlling a uniform front-to-back incline."]
   [[:layout :pitch :intrinsic]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians. Intrinsic pitching occurs early in key placement. "
    "It is typically intended to produce a tactile break between two rows of "
    "keys, as in the typewriter-like terracing common on flat keyboards with "
    "OEM-profile or similarly angled caps."]
   [[:layout :pitch :progressive]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians. This progressive pitch factor bends columns "
    "lengthwise. If set to zero, columns are flat."]
   [[:layout :roll]
    "Tait-Bryan roll, meaning the rotation of keys around the y axis."]
   [[:layout :roll :base]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians, controlling a uniform right-to-left incline, also "
    "known as tenting."]
   [[:layout :roll :intrinsic]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians, analogous to intrinsic pitching. Where more than "
    "one column of keys is devoted to a single finger at the edge of the "
    "keyboard, this can help make the edge column easier to reach, reducing "
    "the need to bend the finger (or thumb) sideways."]
   [[:layout :roll :progressive]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians. This progressive roll factor bends rows "
    "lengthwise, which also gives the columns a lateral curvature."]
   [[:layout :yaw]
    "Tait-Bryan yaw, meaning the rotation of keys around the z axis."]
   [[:layout :yaw :base]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians, corresponding to the way your hand naturally "
    "describes an arc as you rotate your arm horizontally at the elbow. "
    "Yawing columns of keys can allow the user to keep their wrists straight "
    "even on a keyboard shorter than the width of the user’s own shoulders."]
   [[:layout :yaw :intrinsic]
    {:default 0 :parse-fn parse/compass-incompatible-angle}
    "An angle in radians, analogous to intrinsic pitching."]
   [[:layout :translation]
    "Translation in the geometric sense, displacing keys in relation to each "
    "other. Depending on when this translation takes places, it may have a "
    "a cascading effect on other aspects of key placement. All measurements "
    "are three-dimensional vectors in mm."]
   [[:layout :translation :early]
    {:default [0 0 0] :parse-fn vec :validate [::tarmi-core/point-3d]}
    "”Early” translation happens before other operations in key placement and "
    "therefore has the biggest knock-on effects."]
   [[:layout :translation :mid]
    {:default [0 0 0] :parse-fn vec :validate [::tarmi-core/point-3d]}
    "This happens after columns are styled but before base pitch and roll. "
    "As such it is a good place to adjust whole columns for relative finger "
    "length."]
   [[:layout :translation :late]
    {:default [0 0 0] :parse-fn vec :validate [::tarmi-core/point-3d]}
    "“Late” translation is the last step in key placement and therefore "
    "interacts very little with other steps."]
   [[:layout :clearance]
    "The height of each key above its mounting plate can be included when "
    "calculating the effective radius of key cluster curvature for a layout. "
    "This height is defined as the distance between the lower edge of a keycap "
    "and the top of the mounting plate, when the switch is at rest. Clearance "
    "is defined this way because caps are presumed to be widest at the lower "
    "edge and therefore most likely to collide at this height."]
   [[:layout :clearance :use-key-style]
    {:default false :parse-fn boolean}
    "If `true`, predict clearance based on `key-style`, including switch "
    "travel and configured keycap skirt length. "
    "Ironically, this is most practical when the keyboard design "
    "is closely tied to the key style. Depending on how other features of the "
    "design are anchored, clearance based on key style can introduce knock-on "
    "effects that make it difficult to adapt the design to other switches."]
   [[:layout :clearance :nominal]
    {:default 0 :parse-fn num}
    "Nominal clearance in mm. This is only used without `use-key-style`."]
   [[:channel]
    "Above each switch mount, there is a channel of negative space for the "
    "user’s finger and the keycap to move inside. This is only useful in those "
    "cases where nearby walls or webbing between mounts on the keyboard would "
    "otherwise obstruct movement."]
   [[:channel :height]
    {:default 1 :parse-fn num}
    "The height in mm of the negative space, starting from the "
    "bottom edge of each keycap in its pressed (active) state."]
   [[:channel :top-width]
    {:default 0 :parse-fn num}
    "The width in mm of the negative space at its top. Its width at the "
    "bottom is defined by keycap geometry."]
   [[:channel :margin]
    {:default 0 :parse-fn num}
    "The width in mm of extra negative space around the edges of a keycap, on "
    "all sides. This is applied before the `error-general` DFM compensator."]
   [[:plate]
    "The properties of the flat mounting plate through which each switch is "
    "inserted."]
   [[:plate :use-key-style]
    {:default false :parse-fn boolean}
    "If `true`, base the size and position of the plate on `key-style`."]
   [[:plate :size]
    {:default [1 1 1] :parse-fn vec :validate [::tarmi-core/point-3d]}
    "Three measurements in mm. If `use-key-style` is `false`, this is the "
    "size of the mounting plate."]
   [[:plate :position]
    {:default [0 0 0] :parse-fn vec :validate [::tarmi-core/point-3d]}
    "A three-dimensional offset in mm. If `use-key-style` is `false`, the "
    "mounting plate is automatically lowered underneath "
    "the centre of the switch. This offset is applied at the same time.\n"
    "\n"
    "This parameter is intended for adapting the mounting plate as such to "
    "exotic human interfaces. To adjust the position of the entire key for "
    "other purposes, use the `translation` settings for `layout`, in this "
    "document."]
   [[:wall]
    "Properties of a wall built around the edge of the cluster.\n"
    "\n"
    "The walls of the keyboard case support the key mounts and protect the "
    "electronics. They are generated by an algorithm that walks around each "
    "key cluster, optionally complemented by `tweaks`.\n"
    "\n"
    "The `wall` section determines the shape of the case wall, specifically "
    "the skirt around each key mount along the edges of the board. These "
    "skirts are made up of convex hulls wrapping sets of corner posts."]
   [[:wall :extent]
    {:default 0 :parse-fn num :validate [(set (range 4))]}
    "A segment ID describing how far away from the key mount to extend its "
    "wall. Note that even if this is set lower than the number of segments "
    "you’ve defined, you can still use `tweaks` to target other segments."]
   [[:wall :to-ground]
    {:default false :parse-fn boolean}
    "If `true`, draw one extra, vertical section of wall between the segment "
    "identified by `extent` and the ground beneath it."]
   [[:wall :segments]
    (let [local
          [[]  ; This header will not be rendered and is therefore empty.
           [[:size]
            {:default [1 1 1]
             :parse-fn parse/pad-to-3-tuple
             :validate [::tarmi-core/point-3d]}]
           [[:intrinsic-offset]
            {:default [0 0 0]
             :parse-fn (parse/tuple-of num)
             :validate [::tarmi-core/point-3d]}]]]
      {:default {0 (base/extract-defaults local)}
       :parse-fn (parse/map-of parse/integer (base/parser-wo-defaults local))
       :validate [(spec/map-of integer?  (base/delegated-validation local))]})
    "A map describing the properties of the wall at each of its segments.\n"
    "\n"
    "This map is indexed by wall segment IDs, which are non-negative "
    "integers. These must be entered in YAML as strings, i.e. in quotes.\n"
    "\n"
    "For each wall segment, the following parameters are available:\n\n"
    "- `size`: The measurements of the segment, in mm.\n"
    "- `intrinsic-offset`: An xyz-offset in mm from the previous segment or, "
    "in the case of segment zero, from the corner of the switch mounting plate.\n"
    "\n"
    "As a side effect, the `size` of segment 0 determines the thickness of "
    "the key mount and the internal webbing between key mounts, as well as the "
    "size of each wall post. "
    "By contrast, the *unit* size of each *key*, and therefore the horizontal "
    "extent of each key mounting plate, is a function of `key-style`, not of "
    "any parameter under `segments`.\n"
    "\n"
    "`intrinsic-offset` is *cumulative*. Segments form a chain, each one "
    "positioned relative to the one before, as the building blocks of each "
    "wall. Consider this example:\n"
    "\n"
    "```\n"
    "by-key:\n"
    "  parameters:\n"
    "    wall:\n"
    "      extent: 2\n"
    "      segments:\n"
    "        "1":\n"
    "          intrinsic-offset: [0, 1, -0.5]\n"
    "        "2":\n"
    "          intrinsic-offset: [0, 0, -4]\n"
    "  sides\n"
    "    SSE:\n"
    "      parameters:\n"
    "        wall:\n"
    "          segments:\n"
    "            "2":\n"
    "              intrinsic-offset: [0, 0, -10]\n"
    "```\n"
    "\n"
    "With this configuration, walls will be built connecting segments 0, 1 "
    "and 2 on the edge of each key cluster. For the sake of illustration, "
    "Let’s say there’s only one cluster of three keys: A, B, and C, in one row. "
    "Imagine their corners radiating numbered wall segments.\n"
    "\n"
    "```\n"
    "  2–––2–2–––2–2–––2\n"
    " /1–––1–1–––1–1–––1\\\n"
    "210–––0–0–––0–0–––012\n"
    "||| A     B     C |||\n"
    "210–––0–0–––0–0–––012\n"
    " \\1–––1–1–––1–1–––1/\n"
    "  2–––2–2–––2–2–––2\n```"
    "\n\n"
    "A more detailed ASCII diagram of the B key names the sides from which "
    "its wall segments radiate:\n"
    "\n"
    "```\n"
    "–2–––––2–\n"
    "–1–––––1–\n"
    "–0–––––0–\n"
    "NNW   NNE\n"
    "\n"
    "    B\n"
    "\n"
    "SSW   SSE\n"
    "–0–––––0–\n"
    "–1–––––1–\n"
    "–2–––––2–\n```"
    "\n\n"
    "To understand the effect of the example configuration, it is simplest to "
    "begin thinking about the `NNE` side of the B key in this image. `NNE` "
    "here does not mean “facing 22½° east of nominal north from the middle of "
    "the mounting plate”. It means “facing nominal north from the `NE` corner”. "
    "Segment 0 on the `NNE` side of the B key is positioned precisely at the "
    "`NE` corner because the example configuration leaves segment 0 at its "
    "default offset: `[0, 0, 0]`.\n"
    "\n"
    "In the vector space of key B, segment 1 is located at `[0, 1, -0.5]` away "
    "from segment 0. This is 1 mm to the north and ½ mm closer to the ground: "
    "A minor bevel.\n"
    "\n"
    "In the vector space of key B, segment 2 is located at `[0, 1, -4.5]` "
    "away from segment 0. "
    "Its `-4` on the z-axis is added to the offset for segment 1, placing it "
    "directly below segment 1 rather than radiating on the xy-plane alone.\n"
    "\n"
    "For other sides of the B key, segment coordinates stated in the "
    "configuration are flipped and rotated. For example, segment 1 of the "
    "`SSE` corner is `[0, -1, -4.5]` away from segment 0 on the same corner. "
    "Segment 2 of the `SSW` corner has a side-specific override noted in the "
    "example configuration, dipping an extra 6 mm to `[0, -1, -10.5]` away "
    "from segment 0 on the `SSW` corner.\n"
    "\n"
    "The key idea here is that offsets are expressed in the vector space "
    "local to the key’s nominal *northeast quadrant*. They are used as given "
    "only for the `NNE` and `NE` sides of the key. Even then, they are later "
    "subjected to all the same transformations as the mounting plate itself.\n"
    "\n"
    "Offsets are *automatically adapted* to each side to make it easy to "
    "specify something like a bevel with a single value in the configuration, "
    "without having to state explicitly that the bevel should sweep around "
    "the key cluster, “turning” as the wall turns."]])
