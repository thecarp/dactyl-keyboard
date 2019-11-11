<!--This document was generated and is intended for rendering to HTML on GitHub. Edit the source files, not this file.-->

# Nestable configuration options

This document describes all those settings which can be made at any level of specificity, from the entire keyboard down to an individual key. These settings all go under `by-key` in a YAML file.

## Conceptual overview

Specificity is accomplished by nesting. The following levels of specificity are currently available. Each one branches out, containing the next:

- The global level, directly under `by-key` (cf. the [main document](options-main.md)).
- The key cluster level, at `by-key` → `clusters` → your cluster.
- The column level, nested still further under your cluster → `columns` → column index.
- The row level, nested at the bottom, under column index → `rows` → row index.

A setting at the row level will only affect keys in the specific cluster and column selected along the way, i.e. only one key per row. Therefore, the row level is effectively the key level.

At each level, two subsections are permitted: `parameters`, where you put the settings themselves, and a section for the next level of nesting: `clusters`, then `columns`, then `rows`. More specific settings take precedence.

In the following hypothetical example, the parameter `P`, which is not really supported, is defined three times: Once at the global level and twice at the row (key) level.

```by-key:
  parameters:
    P: true
  clusters:
    C:
      columns:
        "1":
          rows:
            first:
              parameters:
                P: false
            "3":
              parameters:
                P: false
```

In this example, `P` will have the value “true” for all keys except two on each half of the keyboard. On the right-hand side, `P` will be false for the key closest to the user (“first” row) in the second column from the left (column “1”) in a cluster of keys here named `C`. `P` will also be false for the fourth key from the user (row “3”) in the same column.

Columns and rows are indexed by their ordinal integers or the words “first” or “last”, which take priority.

WARNING: Due to a peculiarity of the YAML parser, take care to quote your numeric column and row indices as strings. This is why there are quotation marks around column index 1 and row index 3 in the example.

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
    - Parameter <a href="#user-content-wall-bevel">`bevel`</a>
    - Section <a href="#user-content-wall-north">`north`</a>
        - Parameter <a href="#user-content-wall-north-extent">`extent`</a>
        - Parameter <a href="#user-content-wall-north-parallel">`parallel`</a>
        - Parameter <a href="#user-content-wall-north-perpendicular">`perpendicular`</a>
    - Section <a href="#user-content-wall-east">`east`</a>
        - Parameter <a href="#user-content-wall-east-extent">`extent`</a>
        - Parameter <a href="#user-content-wall-east-parallel">`parallel`</a>
        - Parameter <a href="#user-content-wall-east-perpendicular">`perpendicular`</a>
    - Section <a href="#user-content-wall-south">`south`</a>
        - Parameter <a href="#user-content-wall-south-extent">`extent`</a>
        - Parameter <a href="#user-content-wall-south-parallel">`parallel`</a>
        - Parameter <a href="#user-content-wall-south-perpendicular">`perpendicular`</a>
    - Section <a href="#user-content-wall-west">`west`</a>
        - Parameter <a href="#user-content-wall-west-extent">`extent`</a>
        - Parameter <a href="#user-content-wall-west-parallel">`parallel`</a>
        - Parameter <a href="#user-content-wall-west-perpendicular">`perpendicular`</a>

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

An angle in radians. Set at a high level, this controls the general front-to-back incline of a key cluster.

#### Parameter <a id="layout-pitch-intrinsic">`intrinsic`</a>

An angle in radians. Intrinsic pitching occurs early in key placement. It is typically intended to produce a tactile break between two rows of keys, as in the typewriter-like terracing common on flat keyboards with OEM-profile or similarly angled caps.

The term “intrinsic” is used here because the key spins roughly around its own center. The term should not be confused with intrinsic rotations in the sense that each step is performed on a coordinate system resulting from previous operations.

#### Parameter <a id="layout-pitch-progressive">`progressive`</a>

An angle in radians. This progressive pitch factor bends columns lengthwise. If set to zero, columns are flat.

### Section <a id="layout-roll">`roll`</a>

Tait-Bryan roll, meaning the rotation of keys around the y axis.

#### Parameter <a id="layout-roll-base">`base`</a>

An angle in radians. This is the “tenting” angle. Applied to your main cluster, it controls the overall left-to-right tilt of each half of the keyboard.

#### Parameter <a id="layout-roll-intrinsic">`intrinsic`</a>

An angle in radians, analogous to intrinsic pitching. Where more than one column of keys is devoted to a single finger at the edge of the keyboard, this can help make the edge column easier to reach, reducing the need to bend the finger (or thumb) sideways.

#### Parameter <a id="layout-roll-progressive">`progressive`</a>

An angle in radians. This progressive roll factor bends rows lengthwise, which also gives the columns a lateral curvature.

### Section <a id="layout-yaw">`yaw`</a>

Tait-Bryan yaw, meaning the rotation of keys around the z axis.

#### Parameter <a id="layout-yaw-base">`base`</a>

An angle in radians. Applied to your main key cluster, this serves the purpose of allowing the user to keep their wrists straight even if the two halves of the keyboard are closer together than the user’s shoulders.

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

The name of a key style defined in the [global](options-main.md) `keys` section. The default value for this setting is the name `default`.

## Section <a id="channel">`channel`</a>

Above each switch mount, there is a channel of negative space for the user’s finger and the keycap to move inside. This is only useful in those cases where nearby walls or webbing between mounts on the keyboard would otherwise obstruct movement.

### Parameter <a id="channel-height">`height`</a>

The height in mm of the negative space, starting from the bottom edge of each keycap in its pressed (active) state.

### Parameter <a id="channel-top-width">`top-width`</a>

The width in mm of the negative space at its top. Its width at the bottom is defined by keycap geometry.

### Parameter <a id="channel-margin">`margin`</a>

The width in mm of extra negative space around the edges of a keycap, on all sides. This is applied before the `error-general` DFM compensator.

## Section <a id="wall">`wall`</a>

The walls of the keyboard case support the key mounts and protect the electronics. They are generated by an algorithm that walks around each key cluster.

This section determines the shape of the case wall, specifically the skirt around each key mount along the edges of the board. These skirts are made up of convex hulls wrapping sets of corner posts.

There is one corner post at each actual corner of every key mount. More posts are displaced from it, going down the sides. Their placement is affected by the way the key mounts are rotated etc.

### Parameter <a id="wall-thickness">`thickness`</a>

A distance in mm.

This is actually the distance between some pairs of corner posts (cf. `key-mount-corner-margin`), in the key mount’s frame of reference. It is therefore inaccurate as a measure of wall thickness on the x-y plane.

### Parameter <a id="wall-bevel">`bevel`</a>

A distance in mm.

This is applied at the very top of a wall, making up the difference between wall segments 0 and 1. It is applied again at the bottom, making up the difference between segments 3 and 4.

### Section <a id="wall-north">`north`</a>

As explained [elsewhere](intro.md), “north” refers to the side facing away from the user, barring yaw.

This section describes the shape of the wall on the north side of the keyboard. There are identical sections for the other cardinal directions.

#### Parameter <a id="wall-north-extent">`extent`</a>

Two types of values are permitted here:

- The keyword `full`. This means a complete wall extending from the key mount all the way down to the ground via segments numbered 0 through 4 and a vertical drop thereafter.
- An integer corresponding to the last wall segment to be included. A zero means there will be no wall. No matter the number, there will be no vertical drop to the floor.

#### Parameter <a id="wall-north-parallel">`parallel`</a>

A distance in mm. The later wall segments extend this far away from the corners of their key mount, on its plane.

#### Parameter <a id="wall-north-perpendicular">`perpendicular`</a>

A distance in mm. The later wall segments extend this far away from the corners of their key mount, away from its plane.

### Section <a id="wall-east">`east`</a>

See `north`.

#### Parameter <a id="wall-east-extent">`extent`</a>



#### Parameter <a id="wall-east-parallel">`parallel`</a>



#### Parameter <a id="wall-east-perpendicular">`perpendicular`</a>



### Section <a id="wall-south">`south`</a>

See `north`.

#### Parameter <a id="wall-south-extent">`extent`</a>



#### Parameter <a id="wall-south-parallel">`parallel`</a>



#### Parameter <a id="wall-south-perpendicular">`perpendicular`</a>



### Section <a id="wall-west">`west`</a>

See `north`.

#### Parameter <a id="wall-west-extent">`extent`</a>



#### Parameter <a id="wall-west-parallel">`parallel`</a>



#### Parameter <a id="wall-west-perpendicular">`perpendicular`</a>



⸻

This document was generated from the application CLI.
