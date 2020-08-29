<!--This document was generated and is intended for rendering to HTML on GitHub. Edit the source files, not this file.-->

# Nestable key configuration options

This document describes all those settings which can be made at any level of specificity, from the entire keyboard down to one side of one key. These settings all go under `by-key` in a YAML file, as indicated [here](options-main.md).

## Conceptual overview

The `by-key` section contains a map of up to five items:

- `parameters`, where you put your settings. Sections described in this document all pertain to this map.
- `clusters`, starting a nested map for specific clusters only, keyed by their names.
- `columns` and/or `rows`, each starting a nested map for specific columns or rows only, keyed either by their indices (ordinal integers) or by the special words `first` or `last`. Due to a peculiarity of YAML (and JSON), **numeric indices must appear in quotation marks** as in the example below.
- `sides`, starting a nested map for specific sides only, keyed by the long-form cardinal points of the compass, i.e. the words `north`, `east`, `south` or `west`.

Each of the nested maps have the same structure as this root-level map. Specificity is accomplished by nesting a series of these maps, so that a nested set of `parameters` comes to refer to an intersection of more than one selection criterion.

### Example

In the following example, the parameter `key-style` is set three times: Once at the root level and twice with enough selection criteria to limit the effect to two individual keys.

```by-key:
  parameters:
    key-style: plump
  clusters:
    C:
      columns:
        "1":
          rows:
            first:
              parameters:
                key-style: svelte
            "3":
              parameters:
                key-style: svelte
```

In this example, `key-style` will have the value `plump` for all keys except two. It will have the value `svelte` for the key closest to the user (first row) and the key three steps above the home row (row 3), both in the second column from the left (i.e. column 1) of key cluster `C`.

Key cluster `C` and the two key styles must be defined elsewhere. Also, if the keyboard uses reflection, notice that the descriptions given in the previous paragraph would be incorrect for the left-hand side of the keyboard, because that side would be mirrored.

### Comparison to anchoring

The `by-key` section can place any key anywhere. However, please don’t. Look first to the `key-clusters` section, which is intended to define the most basic shape and position of each cluster. `key-clusters` allows for standard [anchoring](options-anchoring.md), while `by-key` does not.

Please use `key-clusters` to translate and rotate each cluster. More concretely, prefer `key-clusters` over `translation` and over base `pitch`, `roll` and `yaw` at a global or cluster level of `by-key`. Doing so is easier, because standard anchoring allows you to describe rotation on all three axes together, while keeping values in `by-key` smaller and neatly unentangled.

### Unique selection criteria

The nested maps named `clusters`, `columns`, `rows` and `sides`, together with the identifiers they take as keys, are selection criteria. The order in which these criteria appear is significant, but only under the following circumstances.

- The special words `first` and `last` can only be used when nested inside a cluster-specific configuration, because they are informed by the cluster matrix specification.
- As a further requirement for rows only, `first` and `last` can only be used when nested inside a column-specific configuration, as in the example above.

Each set of criteria must be unique. That is to say, each set of `parameters` must describe some new part of the keyboard. For instance, you can have either `by-key` → `rows` → `0` → `columns`→ `0` → `parameters` *or* `by-key` → `columns` → `0` → `rows` → `0`→ `parameters`, but you cannot have both. They are equivalent because they amount to identical selection criteria.

This restriction applies even to the use of `first` and `last` with numeric indices. If column 8 is the last column, you can refer to it either way, but there cannot be two exactly equivalent sets of criteria, even if one uses `8` and the other uses the special keyword `last` to select column 8.

### Specificity

The application picks the most specific value available. Specificity is determined by permutations of selection criteria. More specifically, each criterion is switched on and off, starting with the least significant. Rows are considered less significant than columns for this purpose. The complete hiearchy is clusters > colums > rows > sides.

An example: To find the right `key-style` for a key at coordinates `[0, -1]` in a cluster named `C`, the application checks for `parameters` → `key-style` with the following criteria, using the first setting it finds.

- Cluster `C`, column 0, row -1.
- Cluster `C`, column 0, without any specific row selector.
- Cluster `C`, row -1, without any specific column.
- Cluster `C`, without any specific column or row.
- No specific cluster, column 0, row -1.
- No specific cluster, column 0, no specific row.
- No specific cluster or column, row -1.
- No specific cluster, column or row.

The first item in this example requires a setting specific to the individual key under consideration. The last item is at the opposite end of the spectrum of specificity: It reaches the root level, specifically looking at the setting `by-key` → `parameters` → `key-style`. This root level serves as a global fallback. It’s the only level with any default values.

Notice that `sides` are ignored in the example above, because setting a `key-style` for just one side of a key, though it is permitted, is overly specific and therefore meaningless. You can compare it to a decision to paint one atom.

Here follows the complete order of resolution in an extended example, for a `wall` of the same key as above. Where walls are concerned, the side of the key *would* be relevant, so its gets included in the permutations, from most to least specific.

- Cluster `C`, column 0, row -1, west side.
- Cluster `C`, column 0, row -1, no side.
- Cluster `C`, column 0, no row, west side.
- Cluster `C`, column 0, no row, no side.
- Cluster `C`, no column, row -1, west side.
- Cluster `C`, no column, row -1, no side.
- Cluster `C`, no column, no row, west side.
- Cluster `C`, no column, no row, no side.
- No cluster, column 0, row -1, west side.
- No cluster, column 0, row -1, no side.
- No cluster, column 0, no row, west side.
- No cluster, column 0, no row, no side.
- No cluster, no column, row -1, west side.
- No cluster, no column, row -1, no side.
- No cluster, no column, no row, west side.
- No cluster, no column, no row, no side.


## Table of contents
- Section <a href="#user-content-layout">`layout`</a>
    - Section <a href="#user-content-layout-matrix">`matrix`</a>
        - Section <a href="#user-content-layout-matrix-neutral">`neutral`</a>
            - Parameter <a href="#user-content-layout-matrix-neutral-column">`column`</a>
            - Parameter <a href="#user-content-layout-matrix-neutral-row">`row`</a>
        - Section <a href="#user-content-layout-matrix-separation">`separation`</a>
            - Parameter <a href="#user-content-layout-matrix-separation-column">`column`</a>
            - Parameter <a href="#user-content-layout-matrix-separation-row">`row`</a>
    - Section <a href="#user-content-layout-pitch">`pitch`</a>
        - Parameter <a href="#user-content-layout-pitch-base">`base`</a>
        - Parameter <a href="#user-content-layout-pitch-intrinsic">`intrinsic`</a>
        - Parameter <a href="#user-content-layout-pitch-progressive">`progressive`</a>
    - Section <a href="#user-content-layout-roll">`roll`</a>
        - Parameter <a href="#user-content-layout-roll-base">`base`</a>
        - Parameter <a href="#user-content-layout-roll-intrinsic">`intrinsic`</a>
        - Parameter <a href="#user-content-layout-roll-progressive">`progressive`</a>
    - Section <a href="#user-content-layout-yaw">`yaw`</a>
        - Parameter <a href="#user-content-layout-yaw-base">`base`</a>
        - Parameter <a href="#user-content-layout-yaw-intrinsic">`intrinsic`</a>
    - Section <a href="#user-content-layout-translation">`translation`</a>
        - Parameter <a href="#user-content-layout-translation-early">`early`</a>
        - Parameter <a href="#user-content-layout-translation-mid">`mid`</a>
        - Parameter <a href="#user-content-layout-translation-late">`late`</a>
- Parameter <a href="#user-content-key-style">`key-style`</a>
- Section <a href="#user-content-channel">`channel`</a>
    - Parameter <a href="#user-content-channel-height">`height`</a>
    - Parameter <a href="#user-content-channel-top-width">`top-width`</a>
    - Parameter <a href="#user-content-channel-margin">`margin`</a>
- Section <a href="#user-content-wall">`wall`</a>
    - Parameter <a href="#user-content-wall-thickness">`thickness`</a>
    - Parameter <a href="#user-content-wall-extent">`extent`</a>
    - Parameter <a href="#user-content-wall-to-ground">`to-ground`</a>
    - Parameter <a href="#user-content-wall-bevel">`bevel`</a>
    - Parameter <a href="#user-content-wall-parallel">`parallel`</a>
    - Parameter <a href="#user-content-wall-perpendicular">`perpendicular`</a>

## Section <a id="layout">`layout`</a>

Settings for how to place keys.

### Section <a id="layout-matrix">`matrix`</a>

Roughly how keys are spaced out to form a matrix.

#### Section <a id="layout-matrix-neutral">`neutral`</a>

The neutral point in a column or row is where any progressive curvature both starts and has no effect.

##### Parameter <a id="layout-matrix-neutral-column">`column`</a>

An integer column ID.

##### Parameter <a id="layout-matrix-neutral-row">`row`</a>

An integer row ID.

#### Section <a id="layout-matrix-separation">`separation`</a>

Tweaks to control the systematic separation of keys. The parameters in this section will be multiplied by the difference between each affected key’s coordinates and the neutral column and row.

##### Parameter <a id="layout-matrix-separation-column">`column`</a>

A distance in mm.

##### Parameter <a id="layout-matrix-separation-row">`row`</a>

A distance in mm.

### Section <a id="layout-pitch">`pitch`</a>

Tait-Bryan pitch, meaning the rotation of keys around the x axis.

#### Parameter <a id="layout-pitch-base">`base`</a>

An angle in radians, controlling a uniform front-to-back incline.

#### Parameter <a id="layout-pitch-intrinsic">`intrinsic`</a>

An angle in radians. Intrinsic pitching occurs early in key placement. It is typically intended to produce a tactile break between two rows of keys, as in the typewriter-like terracing common on flat keyboards with OEM-profile or similarly angled caps.

#### Parameter <a id="layout-pitch-progressive">`progressive`</a>

An angle in radians. This progressive pitch factor bends columns lengthwise. If set to zero, columns are flat.

### Section <a id="layout-roll">`roll`</a>

Tait-Bryan roll, meaning the rotation of keys around the y axis.

#### Parameter <a id="layout-roll-base">`base`</a>

An angle in radians, controlling a uniform right-to-left incline, also known as tenting.

#### Parameter <a id="layout-roll-intrinsic">`intrinsic`</a>

An angle in radians, analogous to intrinsic pitching. Where more than one column of keys is devoted to a single finger at the edge of the keyboard, this can help make the edge column easier to reach, reducing the need to bend the finger (or thumb) sideways.

#### Parameter <a id="layout-roll-progressive">`progressive`</a>

An angle in radians. This progressive roll factor bends rows lengthwise, which also gives the columns a lateral curvature.

### Section <a id="layout-yaw">`yaw`</a>

Tait-Bryan yaw, meaning the rotation of keys around the z axis.

#### Parameter <a id="layout-yaw-base">`base`</a>

An angle in radians, corresponding to the way your hand naturally describes an arc as you rotate your arm horizontally at the elbow. Yawing columns of keys can allow the user to keep their wrists straight even on a keyboard shorter than the width of the user’s own shoulders.

#### Parameter <a id="layout-yaw-intrinsic">`intrinsic`</a>

An angle in radians, analogous to intrinsic pitching.

### Section <a id="layout-translation">`translation`</a>

Translation in the geometric sense, displacing keys in relation to each other. Depending on when this translation takes places, it may have a a cascading effect on other aspects of key placement. All measurements are three-dimensional vectors in mm.

#### Parameter <a id="layout-translation-early">`early`</a>

”Early” translation happens before other operations in key placement and therefore has the biggest knock-on effects.

#### Parameter <a id="layout-translation-mid">`mid`</a>

This happens after columns are styled but before base pitch and roll. As such it is a good place to adjust whole columns for relative finger length.

#### Parameter <a id="layout-translation-late">`late`</a>

“Late” translation is the last step in key placement and therefore interacts very little with other steps.

## Parameter <a id="key-style">`key-style`</a>

The name of a key style defined in the [global](options-main.md) `keys` section. The default setting is the name `default`.

## Section <a id="channel">`channel`</a>

Above each switch mount, there is a channel of negative space for the user’s finger and the keycap to move inside. This is only useful in those cases where nearby walls or webbing between mounts on the keyboard would otherwise obstruct movement.

### Parameter <a id="channel-height">`height`</a>

The height in mm of the negative space, starting from the bottom edge of each keycap in its pressed (active) state.

### Parameter <a id="channel-top-width">`top-width`</a>

The width in mm of the negative space at its top. Its width at the bottom is defined by keycap geometry.

### Parameter <a id="channel-margin">`margin`</a>

The width in mm of extra negative space around the edges of a keycap, on all sides. This is applied before the `error-general` DFM compensator.

## Section <a id="wall">`wall`</a>

Properties of a wall built around the edge of the cluster.

The walls of the keyboard case support the key mounts and protect the electronics. They are generated by an algorithm that walks around each key cluster, optionally complemented by `tweaks`.

The `wall` section determines the shape of the case wall, specifically the skirt around each key mount along the edges of the board. These skirts are made up of convex hulls wrapping sets of corner posts.

There is one corner post at each actual corner of every key mount (segment 0). More posts are displaced from it, going down the sides. Their placement is affected by the way the key mounts are rotated etc.


### Parameter <a id="wall-thickness">`thickness`</a>

The size in mm of the key mount and each wall post.

Notice that the unit size of each key, and therefore the horizontal extent of each key mounting plate, is a function of `key-style`, not of this parameter.

The `thickness` parameter instead controls three other aspects of the keyboard case:

- The thickness of the key mounting plate itself. When specifying `thickness` as a list of three dimensions (`[1, 2, 3]`), mounting plate thickness is governed solely by the z-axis dimension (`3`) and the other figures are ignored.
- The thickness of the walls as drawn automatically and as targeted by `tweaks`. Whereas mounting-plate thickness ignores the x- and y-axis dimensions of this parameter, wall posts are cuboids that use all three dimensions.

### Parameter <a id="wall-extent">`extent`</a>

A segment ID describing how far away from the key mount to extend its wall. Note that even if this is set low, you can still use `tweaks` to target other segments.

### Parameter <a id="wall-to-ground">`to-ground`</a>

If `true`, draw one extra, vertical section of wall between the segment identified in `extent` and the ground beneath the key.

### Parameter <a id="wall-bevel">`bevel`</a>

A distance in mm, describing where to place some vertical segments.

The `bevel` is applied at the top of a wall, making up the difference between wall segments 0 and 1. It is applied again at the bottom, making up the difference between segments 2 and 3. It affects all coordinates. The mathematical operation by which it is applied to the z coordinate is determined by the sign of `perpendicular`.

### Parameter <a id="wall-parallel">`parallel`</a>

A distance in mm. Wall segments 2 and 3 extend this far away from the corners of their key mount, on its plane.

### Parameter <a id="wall-perpendicular">`perpendicular`</a>

A distance in mm. Wall segments 2 and 3 extend this far away from the corners of their key mount, along its normal.

⸻

This document was generated from the application CLI.
