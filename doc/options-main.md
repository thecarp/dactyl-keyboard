<!--This document was generated and is intended for rendering to HTML on GitHub. Edit the source files, not this file.-->

# General configuration options

Each heading in this document represents a recognized configuration key in the main body of a YAML file for a DMOTE variant. Other documents cover special sections of this one in more detail.

## Table of contents
- Section <a href="#user-content-keys">`keys`</a>
    - Parameter <a href="#user-content-keys-preview">`preview`</a>
    - Parameter <a href="#user-content-keys-styles">`styles`</a>
- Freely keyed section <a href="#user-content-key-clusters">`key-clusters`</a>
- Special section <a href="#user-content-by-key">`by-key`</a>
- Parameter <a href="#user-content-secondaries">`secondaries`</a>
- Section <a href="#user-content-main-body">`main-body`</a>
    - Parameter <a href="#user-content-main-body-reflect">`reflect`</a>
    - Section <a href="#user-content-main-body-rear-housing">`rear-housing`</a>
        - Parameter <a href="#user-content-main-body-rear-housing-include">`include`</a>
        - Section <a href="#user-content-main-body-rear-housing-anchoring">`anchoring`</a>
        - Parameter <a href="#user-content-main-body-rear-housing-size">`size`</a>
        - Section <a href="#user-content-main-body-rear-housing-bevel">`bevel`</a>
            - Parameter <a href="#user-content-main-body-rear-housing-bevel-exterior">`exterior`</a>
            - Parameter <a href="#user-content-main-body-rear-housing-bevel-interior">`interior`</a>
        - Section <a href="#user-content-main-body-rear-housing-thickness">`thickness`</a>
            - Parameter <a href="#user-content-main-body-rear-housing-thickness-walls">`walls`</a>
            - Parameter <a href="#user-content-main-body-rear-housing-thickness-roof">`roof`</a>
        - Section <a href="#user-content-main-body-rear-housing-fasteners">`fasteners`</a>
            - Parameter <a href="#user-content-main-body-rear-housing-fasteners-bolt-properties">`bolt-properties`</a>
            - Parameter <a href="#user-content-main-body-rear-housing-fasteners-bosses">`bosses`</a>
            - Section <a href="#user-content-main-body-rear-housing-fasteners-sides">`sides`</a>
                - Section <a href="#user-content-main-body-rear-housing-fasteners-sides-W">`W`</a>
                    - Parameter <a href="#user-content-main-body-rear-housing-fasteners-sides-W-include">`include`</a>
                    - Parameter <a href="#user-content-main-body-rear-housing-fasteners-sides-W-offset">`offset`</a>
                - Section <a href="#user-content-main-body-rear-housing-fasteners-sides-E">`E`</a>
                    - Parameter <a href="#user-content-main-body-rear-housing-fasteners-sides-E-include">`include`</a>
                    - Parameter <a href="#user-content-main-body-rear-housing-fasteners-sides-E-offset">`offset`</a>
    - Section <a href="#user-content-main-body-back-plate">`back-plate`</a>
        - Parameter <a href="#user-content-main-body-back-plate-include">`include`</a>
        - Parameter <a href="#user-content-main-body-back-plate-beam-height">`beam-height`</a>
        - Section <a href="#user-content-main-body-back-plate-fasteners">`fasteners`</a>
            - Parameter <a href="#user-content-main-body-back-plate-fasteners-bolt-properties">`bolt-properties`</a>
            - Parameter <a href="#user-content-main-body-back-plate-fasteners-distance">`distance`</a>
            - Parameter <a href="#user-content-main-body-back-plate-fasteners-bosses">`bosses`</a>
        - Section <a href="#user-content-main-body-back-plate-anchoring">`anchoring`</a>
    - Section <a href="#user-content-main-body-bottom-plate">`bottom-plate`</a>
        - Parameter <a href="#user-content-main-body-bottom-plate-include">`include`</a>
        - Parameter <a href="#user-content-main-body-bottom-plate-preview">`preview`</a>
        - Parameter <a href="#user-content-main-body-bottom-plate-combine">`combine`</a>
        - Parameter <a href="#user-content-main-body-bottom-plate-thickness">`thickness`</a>
        - Section <a href="#user-content-main-body-bottom-plate-installation">`installation`</a>
            - Parameter <a href="#user-content-main-body-bottom-plate-installation-style">`style`</a>
            - Parameter <a href="#user-content-main-body-bottom-plate-installation-dome-caps">`dome-caps`</a>
            - Parameter <a href="#user-content-main-body-bottom-plate-installation-thickness">`thickness`</a>
            - Section <a href="#user-content-main-body-bottom-plate-installation-inserts">`inserts`</a>
                - Parameter <a href="#user-content-main-body-bottom-plate-installation-inserts-include">`include`</a>
                - Parameter <a href="#user-content-main-body-bottom-plate-installation-inserts-length">`length`</a>
                - Section <a href="#user-content-main-body-bottom-plate-installation-inserts-diameter">`diameter`</a>
                    - Parameter <a href="#user-content-main-body-bottom-plate-installation-inserts-diameter-top">`top`</a>
                    - Parameter <a href="#user-content-main-body-bottom-plate-installation-inserts-diameter-bottom">`bottom`</a>
            - Section <a href="#user-content-main-body-bottom-plate-installation-fasteners">`fasteners`</a>
                - Parameter <a href="#user-content-main-body-bottom-plate-installation-fasteners-bolt-properties">`bolt-properties`</a>
                - Parameter <a href="#user-content-main-body-bottom-plate-installation-fasteners-positions">`positions`</a>
    - Section <a href="#user-content-main-body-leds">`leds`</a>
        - Parameter <a href="#user-content-main-body-leds-include">`include`</a>
        - Section <a href="#user-content-main-body-leds-position">`position`</a>
            - Parameter <a href="#user-content-main-body-leds-position-cluster">`cluster`</a>
        - Parameter <a href="#user-content-main-body-leds-amount">`amount`</a>
        - Parameter <a href="#user-content-main-body-leds-housing-size">`housing-size`</a>
        - Parameter <a href="#user-content-main-body-leds-emitter-diameter">`emitter-diameter`</a>
        - Parameter <a href="#user-content-main-body-leds-interval">`interval`</a>
- Section <a href="#user-content-central-housing">`central-housing`</a>
- Freely keyed section <a href="#user-content-custom-bodies">`custom-bodies`</a>
- Freely keyed section <a href="#user-content-flanges">`flanges`</a>
- Freely keyed section <a href="#user-content-tweaks">`tweaks`</a>
- Section <a href="#user-content-mcu">`mcu`</a>
    - Parameter <a href="#user-content-mcu-include">`include`</a>
    - Parameter <a href="#user-content-mcu-preview">`preview`</a>
    - Parameter <a href="#user-content-mcu-body">`body`</a>
    - Parameter <a href="#user-content-mcu-type">`type`</a>
    - Section <a href="#user-content-mcu-anchoring">`anchoring`</a>
    - Section <a href="#user-content-mcu-support">`support`</a>
        - Parameter <a href="#user-content-mcu-support-preview">`preview`</a>
        - Section <a href="#user-content-mcu-support-shelf">`shelf`</a>
            - Parameter <a href="#user-content-mcu-support-shelf-include">`include`</a>
            - Parameter <a href="#user-content-mcu-support-shelf-extra-space">`extra-space`</a>
            - Parameter <a href="#user-content-mcu-support-shelf-thickness">`thickness`</a>
            - Parameter <a href="#user-content-mcu-support-shelf-bevel">`bevel`</a>
            - Section <a href="#user-content-mcu-support-shelf-rim">`rim`</a>
                - Parameter <a href="#user-content-mcu-support-shelf-rim-lateral-thickness">`lateral-thickness`</a>
                - Parameter <a href="#user-content-mcu-support-shelf-rim-overhang-thickness">`overhang-thickness`</a>
                - Parameter <a href="#user-content-mcu-support-shelf-rim-overhang-width">`overhang-width`</a>
                - Parameter <a href="#user-content-mcu-support-shelf-rim-offsets">`offsets`</a>
        - Section <a href="#user-content-mcu-support-lock">`lock`</a>
            - Parameter <a href="#user-content-mcu-support-lock-include">`include`</a>
            - Parameter <a href="#user-content-mcu-support-lock-width-factor">`width-factor`</a>
            - Parameter <a href="#user-content-mcu-support-lock-fastener-properties">`fastener-properties`</a>
            - Section <a href="#user-content-mcu-support-lock-plate">`plate`</a>
                - Parameter <a href="#user-content-mcu-support-lock-plate-alias">`alias`</a>
                - Parameter <a href="#user-content-mcu-support-lock-plate-base-thickness">`base-thickness`</a>
                - Parameter <a href="#user-content-mcu-support-lock-plate-backing-thickness">`backing-thickness`</a>
                - Parameter <a href="#user-content-mcu-support-lock-plate-clearance">`clearance`</a>
            - Section <a href="#user-content-mcu-support-lock-socket">`socket`</a>
                - Parameter <a href="#user-content-mcu-support-lock-socket-thickness">`thickness`</a>
            - Section <a href="#user-content-mcu-support-lock-bolt">`bolt`</a>
                - Parameter <a href="#user-content-mcu-support-lock-bolt-clearance">`clearance`</a>
                - Parameter <a href="#user-content-mcu-support-lock-bolt-overshoot">`overshoot`</a>
                - Parameter <a href="#user-content-mcu-support-lock-bolt-mount-length">`mount-length`</a>
                - Parameter <a href="#user-content-mcu-support-lock-bolt-mount-thickness">`mount-thickness`</a>
- Freely keyed section <a href="#user-content-ports">`ports`</a>
- Section <a href="#user-content-wrist-rest">`wrist-rest`</a>
    - Parameter <a href="#user-content-wrist-rest-include">`include`</a>
    - Parameter <a href="#user-content-wrist-rest-style">`style`</a>
    - Parameter <a href="#user-content-wrist-rest-preview">`preview`</a>
    - Section <a href="#user-content-wrist-rest-anchoring">`anchoring`</a>
    - Parameter <a href="#user-content-wrist-rest-plinth-height">`plinth-height`</a>
    - Section <a href="#user-content-wrist-rest-shape">`shape`</a>
        - Section <a href="#user-content-wrist-rest-shape-spline">`spline`</a>
            - Parameter <a href="#user-content-wrist-rest-shape-spline-main-points">`main-points`</a>
            - Parameter <a href="#user-content-wrist-rest-shape-spline-resolution">`resolution`</a>
        - Section <a href="#user-content-wrist-rest-shape-lip">`lip`</a>
            - Parameter <a href="#user-content-wrist-rest-shape-lip-height">`height`</a>
            - Parameter <a href="#user-content-wrist-rest-shape-lip-width">`width`</a>
            - Parameter <a href="#user-content-wrist-rest-shape-lip-inset">`inset`</a>
        - Section <a href="#user-content-wrist-rest-shape-pad">`pad`</a>
            - Section <a href="#user-content-wrist-rest-shape-pad-surface">`surface`</a>
                - Section <a href="#user-content-wrist-rest-shape-pad-surface-edge">`edge`</a>
                    - Parameter <a href="#user-content-wrist-rest-shape-pad-surface-edge-inset">`inset`</a>
                    - Parameter <a href="#user-content-wrist-rest-shape-pad-surface-edge-resolution">`resolution`</a>
                - Section <a href="#user-content-wrist-rest-shape-pad-surface-heightmap">`heightmap`</a>
                    - Parameter <a href="#user-content-wrist-rest-shape-pad-surface-heightmap-include">`include`</a>
                    - Parameter <a href="#user-content-wrist-rest-shape-pad-surface-heightmap-filepath">`filepath`</a>
            - Section <a href="#user-content-wrist-rest-shape-pad-height">`height`</a>
                - Parameter <a href="#user-content-wrist-rest-shape-pad-height-surface-range">`surface-range`</a>
                - Parameter <a href="#user-content-wrist-rest-shape-pad-height-lip-to-surface">`lip-to-surface`</a>
                - Parameter <a href="#user-content-wrist-rest-shape-pad-height-below-lip">`below-lip`</a>
    - Section <a href="#user-content-wrist-rest-rotation">`rotation`</a>
        - Parameter <a href="#user-content-wrist-rest-rotation-pitch">`pitch`</a>
        - Parameter <a href="#user-content-wrist-rest-rotation-roll">`roll`</a>
    - Special section <a href="#user-content-wrist-rest-mounts">`mounts`</a>
    - Section <a href="#user-content-wrist-rest-sprues">`sprues`</a>
        - Parameter <a href="#user-content-wrist-rest-sprues-include">`include`</a>
        - Parameter <a href="#user-content-wrist-rest-sprues-inset">`inset`</a>
        - Parameter <a href="#user-content-wrist-rest-sprues-diameter">`diameter`</a>
        - Parameter <a href="#user-content-wrist-rest-sprues-positions">`positions`</a>
    - Section <a href="#user-content-wrist-rest-bottom-plate">`bottom-plate`</a>
        - Parameter <a href="#user-content-wrist-rest-bottom-plate-include">`include`</a>
        - Parameter <a href="#user-content-wrist-rest-bottom-plate-inset">`inset`</a>
        - Parameter <a href="#user-content-wrist-rest-bottom-plate-fastener-positions">`fastener-positions`</a>
    - Parameter <a href="#user-content-wrist-rest-mould-thickness">`mould-thickness`</a>
- Section <a href="#user-content-resolution">`resolution`</a>
    - Parameter <a href="#user-content-resolution-include">`include`</a>
    - Parameter <a href="#user-content-resolution-minimum-face-size">`minimum-face-size`</a>
- Section <a href="#user-content-dfm">`dfm`</a>
    - Parameter <a href="#user-content-dfm-error-general">`error-general`</a>
    - Section <a href="#user-content-dfm-keycaps">`keycaps`</a>
        - Parameter <a href="#user-content-dfm-keycaps-error-stem-positive">`error-stem-positive`</a>
        - Parameter <a href="#user-content-dfm-keycaps-error-stem-negative">`error-stem-negative`</a>
    - Section <a href="#user-content-dfm-bottom-plate">`bottom-plate`</a>
        - Parameter <a href="#user-content-dfm-bottom-plate-fastener-plate-offset">`fastener-plate-offset`</a>
- Section <a href="#user-content-mask">`mask`</a>
    - Parameter <a href="#user-content-mask-size">`size`</a>
    - Parameter <a href="#user-content-mask-center">`center`</a>

## Section <a id="keys">`keys`</a>

Keys, that is keycaps and electrical switches, are not the main focus of this application, but they influence the shape of the case.

### Parameter <a id="keys-preview">`preview`</a>

If `true`, include models of the keycaps in place on the keyboard. This is intended for illustration as you work on a design, not for printing.

### Parameter <a id="keys-styles">`styles`</a>

Here you name all the styles of keys on the keyboard and describe each style using parameters to the `keycap` function of the [`dmote-keycap`](https://github.com/veikman/dmote-keycap) library. Switch type is one aspect of key style.

These key styles determine the size of key mounting plates on the keyboard and what kind of holes are cut into those plates for the switches to fit inside. Negative space is also reserved above the plate for the movement of the keycap: A function of switch height, switch travel, and keycap shape. In addition, if the keyboard is curved, key styles help determine the spacing between key mounts.

In options by key, documented [here](options-nested.md), you specify which style of key is used for each position on the keyboard.

## Freely keyed section <a id="key-clusters">`key-clusters`</a>

This section describes the general size, shape and position of the clusters of keys on the keyboard, each in its own subsection. It is documented in detail [here](options-clusters.md).

## Special section <a id="by-key">`by-key`</a>

This section contains a nested structure of parameters. Each level within controls a smaller part of the keyboard, eventually reaching the level of specific sides of individual keys. It’s all documented [here](options-nested.md).

## Parameter <a id="secondaries">`secondaries`</a>

A map where each item provides a name for a position in space. Such positions exist in relation to other named features of the keyboard and their names can in turn be used as anchors, most typically as supplementary targets for `tweaks` (see below). The concept of anchoring is explained [here](options-anchoring.md), along with the parameters available in this section.

An example:

```secondaries:
  s0:
    anchoring:
      anchor: f0
      side: SE
    override [null, null, 2]
    size: 4
```

This example gives the name `s0` to a point near some feature named `f0`. Populated coordinates in `override` replace corresponding coordinates given by the anchor, so in the example, `s0` is 2 mm above the floor and any distance down from the southeast corner of `f0` in a straight vertical line. In other words, the position of that corner of `f0` is projected onto the global xy plane at z = 2 to make `s0`.

The `size` parameter is relevant only as a fallback for `tweaks` that target the position and do not specify a size in turn. If both are omitted, tweaks fall back still further, to a tiny point.

All of these properties are optional.

## Section <a id="main-body">`main-body`</a>

The main body of the keyboard is the main output of this application. It may be the only body. Much of this part of the case is generated from the `wall` parameters described [here](options-nested.md). This section deals with lesser features of the main body.

### Parameter <a id="main-body-reflect">`reflect`</a>

If `true`, mirror the main body, producing one version for the right hand and another for the left. The two halves will be almost identical: Only chiral parts, such as threaded holes, are exempt from mirroring with `main-body` → `reflect`.

You can use this option to make a ‘split’ keyboard, though the two halves are typically connected by a signalling cable, by a rigid `central-housing`, or by one or more rods anchored to some feature such as `rear-housing` or `back-plate`.

### Section <a id="main-body-rear-housing">`rear-housing`</a>

The furthest row of a key cluster can be extended into a rear housing for the MCU and various other features.

#### Parameter <a id="main-body-rear-housing-include">`include`</a>

If `true`, add a rear housing to the main body.

#### Section <a id="main-body-rear-housing-anchoring">`anchoring`</a>

Where to place the middle of the rear housing. The concept of anchoring is explained [here](options-anchoring.md), along with the parameters available in this section.

#### Parameter <a id="main-body-rear-housing-size">`size`</a>

The exterior measurements of the rear housing, in mm.

#### Section <a id="main-body-rear-housing-bevel">`bevel`</a>

The rear housing can be bevelled.

##### Parameter <a id="main-body-rear-housing-bevel-exterior">`exterior`</a>

Insets from the specified `size` in mm for the exterior of the housing.

##### Parameter <a id="main-body-rear-housing-bevel-interior">`interior`</a>

Insets from the specified `size`, minus thickness, in mm, for the interior of the housing.

This is separate from the exterior bevel parameter because it can help with DFM. A higher setting here can reduce the need for internal print supports.

#### Section <a id="main-body-rear-housing-thickness">`thickness`</a>

The thickness of the rear housing does not influence its external dimensions. It grows inward.

##### Parameter <a id="main-body-rear-housing-thickness-walls">`walls`</a>

The horizontal thickness in mm of the walls.

##### Parameter <a id="main-body-rear-housing-thickness-roof">`roof`</a>

The vertical thickness in mm of the flat top, inside the insets for bevels.

#### Section <a id="main-body-rear-housing-fasteners">`fasteners`</a>

Threaded bolts can run through the roof of the rear housing, making it a hardpoint for attachments like a stabilizer to connect the two halves of a split keyboard.

##### Parameter <a id="main-body-rear-housing-fasteners-bolt-properties">`bolt-properties`</a>

This parameter describes the properties of a screw or bolt. It takes a mapping appropriate for the `bolt` function in the [`scad-klupe.iso`](https://github.com/veikman/scad-klupe) library.

The following describes only a subset of what you can include here:

* `m-diameter`: The ISO metric diameter of a bolt, e.g. `6` for M6.
* `head-type`: A keyword describing the head of the bolt, such as `hex` or `countersunk`.
* `total-length`: The length of the threaded part of the bolt, in mm.

Default values provided by the application are bare minima. More usefully, the application injects DFM functions and flags negative space for specific uses.

##### Parameter <a id="main-body-rear-housing-fasteners-bosses">`bosses`</a>

If `true`, add nut bosses to the ceiling of the rear housing for each fastener. Space permitting, these bosses will have some play on the north-south axis, to permit adjustment of the angle of the keyboard halves under a stabilizer.

##### Section <a id="main-body-rear-housing-fasteners-sides">`sides`</a>

Analogous but independent parameters for the west and east sides.

###### Section <a id="main-body-rear-housing-fasteners-sides-W">`W`</a>

The west: A fastener on the inward-facing end of the rear housing.

###### Parameter <a id="main-body-rear-housing-fasteners-sides-W-include">`include`</a> at level 7

If `true`, include this fastener.

###### Parameter <a id="main-body-rear-housing-fasteners-sides-W-offset">`offset`</a> at level 7

A one-dimensional offset in mm from the inward edge of the rear housing to the fastener. You probably want a negative number if any.

###### Section <a id="main-body-rear-housing-fasteners-sides-E">`E`</a>

The east: A fastener on the outward-facing end of the rear housing.

###### Parameter <a id="main-body-rear-housing-fasteners-sides-E-include">`include`</a> at level 7

###### Parameter <a id="main-body-rear-housing-fasteners-sides-E-offset">`offset`</a> at level 7

### Section <a id="main-body-back-plate">`back-plate`</a>

Given that independent movement of each half of a split keyboard is not useful, each half can include a mounting plate for a stabilizing rod. That is a straight piece of wood, aluminium, rigid plastic etc. to connect the two halves mechanically and possibly carry the wire that connects them electrically.

This option is similar to rear housing, but the back plate block provides no interior space for an MCU etc. It is solid, with holes for threaded fasteners including the option of nut bosses. Its footprint is not part of a `bottom-plate`.

#### Parameter <a id="main-body-back-plate-include">`include`</a>

If `true`, include a back plate block. This is not contingent upon `reflect`.

#### Parameter <a id="main-body-back-plate-beam-height">`beam-height`</a>

The nominal vertical extent of the back plate in mm. Because the plate is bottom-hulled to the floor, the effect of this setting is on the area of the plate above its holes.

#### Section <a id="main-body-back-plate-fasteners">`fasteners`</a>

Two threaded bolts run through the back plate.

##### Parameter <a id="main-body-back-plate-fasteners-bolt-properties">`bolt-properties`</a>

This parameter describes the properties of a screw or bolt. It takes a mapping appropriate for the `bolt` function in the [`scad-klupe.iso`](https://github.com/veikman/scad-klupe) library.

The following describes only a subset of what you can include here:

* `m-diameter`: The ISO metric diameter of a bolt, e.g. `6` for M6.
* `head-type`: A keyword describing the head of the bolt, such as `hex` or `countersunk`.
* `total-length`: The length of the threaded part of the bolt, in mm.

Default values provided by the application are bare minima. More usefully, the application injects DFM functions and flags negative space for specific uses.

##### Parameter <a id="main-body-back-plate-fasteners-distance">`distance`</a>

The horizontal distance between the bolts.

##### Parameter <a id="main-body-back-plate-fasteners-bosses">`bosses`</a>

If `true`, cut nut bosses into the inside wall of the block.

#### Section <a id="main-body-back-plate-anchoring">`anchoring`</a>

Where to place the middle of the back plate. The concept of anchoring is explained [here](options-anchoring.md), along with the parameters available in this section.

### Section <a id="main-body-bottom-plate">`bottom-plate`</a>

A bottom plate can be added to close the case. This is useful mainly to simplify transportation.

#### Overview

The bottom plate is largely two-dimensional. The application builds most of it from a set of polygons, trying to match the perimeter of the case at the ground level (i.e. z = 0).

Specifically, there is one polygon per key cluster, limited to `full` wall edges, one polygon for the rear housing, and one set of polygons for each of the first-level case `tweaks` that use `at-ground`, ignoring chunk size and almost ignoring tweaks nested within lists of tweaks.

This methodology is mentioned here because its results are not perfect. Pending future features in OpenSCAD, a future version may be based on a more exact projection of the case, but as of 2018, such a projection is hollow and cannot be convex-hulled without escaping the case, unless your case is convex to start with.

For this reason, while the polygons fill the interior, the perimeter of the bottom plate is extended by key walls and case `tweaks` as they would appear at the height of the bottom plate. Even this brutality may be inadequate. If you require a more exact match, do a projection of the case without a bottom plate, save it as DXF/SVG etc. and post-process that file to fill the interior gap.

#### Parameter <a id="main-body-bottom-plate-include">`include`</a>

If `true`, include a bottom plate for the case.

#### Parameter <a id="main-body-bottom-plate-preview">`preview`</a>

Preview mode. If `true`, put a model of the plate in the same file as the case it closes. Not for printing.

#### Parameter <a id="main-body-bottom-plate-combine">`combine`</a>

If `true`, combine wrist rests for the case and the bottom plate into a single model, when both are enabled. This is typically used with the `solid` style of wrest rest.

#### Parameter <a id="main-body-bottom-plate-thickness">`thickness`</a>

The thickness (i.e. height) in mm of all bottom plates you choose to include. This covers plates for the case and for the wrist rest.

The case will not be raised to compensate for this. Instead, the height of the bottom plate will be removed from the bottom of the main model so that it does not extend to z = 0.

#### Section <a id="main-body-bottom-plate-installation">`installation`</a>

How your bottom plate is attached to the rest of your case.

##### Parameter <a id="main-body-bottom-plate-installation-style">`style`</a>

The general means of installation. This parameter has been reduced to a placeholder: The only available style is `threads`, signifying the use of threaded fasteners connecting the bottom plate to anchors in the body of the keyboard.

##### Parameter <a id="main-body-bottom-plate-installation-dome-caps">`dome-caps`</a>

If `true`, terminate each anchor with a hemispherical tip. This is an aesthetic feature, primarily intended for externally visible anchors and printed threading. If all of your anchors are completely internal to the case, and/or you intend to tap the screw holes after printing, dome caps are wasteful at best and counterproductive at worst.

##### Parameter <a id="main-body-bottom-plate-installation-thickness">`thickness`</a>

The thickness in mm of each wall of the anchor points for threaded fasteners.

##### Section <a id="main-body-bottom-plate-installation-inserts">`inserts`</a>

You can use heat-set inserts in the anchor points.

It is assumed that, as in Tom Short’s Dactyl-ManuForm, the inserts are largely cylindrical.

###### Parameter <a id="main-body-bottom-plate-installation-inserts-include">`include`</a>

If `true`, make space for inserts.

###### Parameter <a id="main-body-bottom-plate-installation-inserts-length">`length`</a>

The length in mm of each insert.

###### Section <a id="main-body-bottom-plate-installation-inserts-diameter">`diameter`</a>

Inserts may vary in diameter across their length.

###### Parameter <a id="main-body-bottom-plate-installation-inserts-diameter-top">`top`</a> at level 7

Top diameter in mm.

###### Parameter <a id="main-body-bottom-plate-installation-inserts-diameter-bottom">`bottom`</a> at level 7

Bottom diameter in mm. This needs to be at least as large as the top diameter since the mounts for the inserts only open from the bottom.

##### Section <a id="main-body-bottom-plate-installation-fasteners">`fasteners`</a>

The type and positions of the threaded fasteners used to secure each bottom plate.

###### Parameter <a id="main-body-bottom-plate-installation-fasteners-bolt-properties">`bolt-properties`</a>

This parameter describes the properties of a screw or bolt. It takes a mapping appropriate for the `bolt` function in the [`scad-klupe.iso`](https://github.com/veikman/scad-klupe) library.

The following describes only a subset of what you can include here:

* `m-diameter`: The ISO metric diameter of a bolt, e.g. `6` for M6.
* `head-type`: A keyword describing the head of the bolt, such as `hex` or `countersunk`.
* `total-length`: The length of the threaded part of the bolt, in mm.

Default values provided by the application are bare minima. More usefully, the application injects DFM functions and flags negative space for specific uses.

###### Parameter <a id="main-body-bottom-plate-installation-fasteners-positions">`positions`</a>

A list of places where threaded fasteners will connect the bottom plate to the rest of the case.

### Section <a id="main-body-leds">`leds`</a>

Support for light-emitting diodes in the case walls.

#### Parameter <a id="main-body-leds-include">`include`</a>

If `true`, cut slots for LEDs out of the case wall, facing the space between the two halves.

#### Section <a id="main-body-leds-position">`position`</a>

Where to attach the LED strip.

##### Parameter <a id="main-body-leds-position-cluster">`cluster`</a>

The key cluster at which to anchor the strip.

#### Parameter <a id="main-body-leds-amount">`amount`</a>

The number of LEDs.

#### Parameter <a id="main-body-leds-housing-size">`housing-size`</a>

The length of the side on a square profile used to create negative space for the housings on a LED strip. This assumes the housings are squarish, as on a WS2818.

The negative space is not supposed to penetrate the wall, just make it easier to hold the LED strip in place with tape, and direct its light. With that in mind, feel free to exaggerate by 10%.

#### Parameter <a id="main-body-leds-emitter-diameter">`emitter-diameter`</a>

The diameter of a round hole for the light of an LED.

#### Parameter <a id="main-body-leds-interval">`interval`</a>

The distance between LEDs on the strip. You may want to apply a setting slightly shorter than the real distance, since the algorithm carving the holes does not account for wall curvature.

## Section <a id="central-housing">`central-housing`</a>

A major body separate from the main body, located in between and connecting the two halves of a reflected main body. The central housing is documented in detail [here](options-central.md).

## Freely keyed section <a id="custom-bodies">`custom-bodies`</a>

Bodies in addition to those predefined by the application.

This feature is intended for dividing up a keyboard case into parts for easier printing and/or easier assembly. It is not intended for adding novel shapes, such as washable cup holders to fit into `ports` etc. Custom bodies can be combined with `tweaks` to add and separate shapes, but complex novel shapes should typically be designed separately from this application (see `dmote-beam` for an example), or else added as features of the application.

The structure of the `custom-bodies` section is a map of new body names to maps of the following parameters:

- `include`: If `true`, and the parent body is also set to be included, make the custom body an output of the application, with its own SCAD file.
- `parent-body`: The name of another body, into which the custom body fits. By default, the main body (`main`). `auto` cannot be used, but another custom body can be, so long as there is no loop of custom bodies being one another’s parents.
- `cut`: Where the custom body fits into the parent body.

The name of a custom body cannot match that of a predefined body such as `main`.

`cut` takes a map of [arbitrary shapes](options-arbitrary-shapes.md) and ultimately describes the shape of the custom body. However, the combined shape of the cut is not the shape of the custom body itself. Those parts of the parent body that would normally have fallen within the area instead disappear from the parent body *and* constitute the entire custom body instead. What’s already inside the area effectively moves from one body to the other. This behaviour can be broken by `preview` settings, since they artificially extend bodies for purposes of visualization.

Here’s an example of a custom body named `pet-flap`, with a simple cut, arbitrarily named `shape`:

```custom-bodies:
  pet-flap:
    include: true
    parent-body: central-housing
    cut
      shape:
      - [origin, {size: 60}]
```

This example describes a cube-shaped area in the middle of the central housing. The wall there will gets its own SCAD file, so you can print it separately and then mount it on hinges for small animals to live in your keyboard.

In `cut` nodes, the `positive` parameter is ignored, as is `body`. Neither `at-ground` nor `to-ground` will affect bottom plates.

If its parent body is governed by `reflect`, the custom body will also be reflected, appearing in left- and right-hand versions.

## Freely keyed section <a id="flanges">`flanges`</a>

Extra screws.

`flanges` is named by analogy. It is intended to connect custom bodies to their parent bodies by means of screws, in the manner of pipe flanges joined by threaded fasteners.

The structure of the `flanges` section is a map of arbitrary names to maps of the following parameters:

- `bolt-properties` (required): A map of standard `scad-klupe` parameters, as for `bolt-properties` elsewhere.
- `boss-diameter-factor` (optional): This factor multiplies the `m-diameter` of `bolt-properties` to produce the total exterior diameter of a boss for each screw. Typical values range from about 1.5 to 2.5. Even if a value is supplied, bosses are not included by default. Instead, they are added to the keyboard as a result of `tweaks` targeting each individual flange screw by its alias.
- `positions` (optional): A list of individual flange screws.

Each item in the list of `positions`, in turn, has the following structure:

- `alias` (optional): A name for the position. Unlike the name for the flange as a whole, the alias can be used with `tweaks` to target the screw and build a boss or larger positive shape.
- `body` (optional): A code identifying the predefined [body](configuration.md) that contains the screw, before the effect of any custom bodies.
- `anchoring` (optional): Room for standard anchoring parameters. The concept of anchoring is explained [here](options-anchoring.md), along with the parameters available in this section.

## Freely keyed section <a id="tweaks">`tweaks`</a>

Additional shapes. This parameter is usually needed to bridge gaps between the walls of key clusters. The expected value here is a map of [arbitrary shapes](options-arbitrary-shapes.md).

In the following example, `A` and `B` are key aliases that would be defined elsewhere.

```tweaks:
  bridge-between-A-and-B:
    - chunk-size: 2
      hull-around:
      - [A, SSE, 0]
      - [B, NNE, 0]
      - [A, SSW, 0, 3]
```

The example is interpreted to mean that a plate should be created stretching from the south-by-southeast corner of `A` to the north-by-northeast corner of `B`. Due to `chunk-size` 2, that first plate will be joined to, but not fully hulled with, a second plate from `B` back to a different corner of `A`, with a longer stretch of (all) wall segments running down the corner of `A`.

In `tweaks` nodes, the `body` setting is meaningful, but should not refer to a custom body, because the shape of a custom body is always fully determined by its parent body and its cut. Tweaks are not applied to custom bodies as such.

## Section <a id="mcu">`mcu`</a>

MCU is short for ”micro-controller unit”. You need at least one of these, it’s assumed to be mounted on a PCB, and you typically want some support for it inside the case.

The total number of MCUs is governed by more than one setting, roughly in the following order:

* If `mcu` → `include` is `false`, there is no MCU.
* If `mcu` → `include` is `true` but `main-body` → `reflect` is `false`, there is one MCU.
* If `mcu` → `include` and `main-body` → `reflect` and `mcu` → `position`  → `central` are all `true`, there is (again) one MCU.
* Otherwise, there are two MCUs: One in each half of the case, because of reflection.

### Parameter <a id="mcu-include">`include`</a>

If `true`, make space for at least one MCU PCBA.

### Parameter <a id="mcu-preview">`preview`</a>

If `true`, render a visualization of the MCU PCBA. For use in development.

### Parameter <a id="mcu-body">`body`</a>

A code identifying the [body](configuration.md) that houses the MCU.

### Parameter <a id="mcu-type">`type`</a>

A code name for a form factor. The following values are supported, representing a selection of designs for commercial products from PJRC, SparkFun, the QMK team and others:

* `elite-c`: Elite-C.
* `promicro`: Pro Micro.
* `proton-c`: Proton C.
* `teensy-l`: Teensy++ 2.0.
* `teensy-m`: Medium-size Teensy, 3.2 or LC.
* `teensy-s`: Teensy 2.0.
* `teensy-xl`: Extra large Teensy, 3.5 or 3.6.

### Section <a id="mcu-anchoring">`anchoring`</a>

Where to place the middle of the back plate. The concept of anchoring is explained [here](options-anchoring.md), along with the parameters available in this section. The PCBA has its base point in the front and rotates around that point. By default, the PCBA appears lying flat, with the MCU side up and the connector end facing nominal north, away from the user.

### Section <a id="mcu-support">`support`</a>

This section offers a couple of different, mutually compatible ways to hold an MCU PCBA in place. Without such support, the MCU will be rattling around inside the case.

Support is especially important if connector(s) on the PCBA will be exposed to animals, such as people. Take care that the user can plug in a USB cable, which requires a receptable to be both reachable through the case *and* held there firmly enough that the force of the user’s interaction will neither damage nor displace the board.

Despite the importance of support in most use cases, no MCU support is included by default. Instead of using the features offered in this section, consider using `tweaks` anchored to the PCBA instead.

#### Parameter <a id="mcu-support-preview">`preview`</a>

If `true`, render a visualization of the support in place. This applies only to those parts of the support that are not part of the case model.

#### Section <a id="mcu-support-shelf">`shelf`</a>

The case can include a shelf for the MCU.

A shelf is the simplest type of MCU support, found on the original Dactyl-ManuForm. It provides very little mechanical support to hold the MCU itself in place, so it is not suitable for exposing a connector on the MCU PCBA through the case. Instead, it’s suitable for use together with a pigtail cable between the MCU and a secondary USB connector embedded in the case wall (see `ports`). It’s especially good with stiff single-strand wiring that will help keep the MCU in place without a lock or firm grip.

##### Parameter <a id="mcu-support-shelf-include">`include`</a>

If `true`, include a shelf.

##### Parameter <a id="mcu-support-shelf-extra-space">`extra-space`</a>

Modifiers for the size of the PCB, on all three axes, in mm, for the purpose of determining the size of the shelf.

For example, the last term, for z, adds extra space between the component side of the PCBA up to the overhang on each side of the shelf, if any. The MCU will appear centered inside the available space, so this parameter can move the plane of the shelf itself.

##### Parameter <a id="mcu-support-shelf-thickness">`thickness`</a>

The thickness of material in the shelf, below or behind the PCBA, in mm.

##### Parameter <a id="mcu-support-shelf-bevel">`bevel`</a>

A map of cardinal compass points to angles in radians, whereby any and all sides of the shelf are turned away from the MCU PCBA. This feature is intended mainly for manufacturability, to reduce the need for supports in printing, but it can also add strength or help connect to other features.

##### Section <a id="mcu-support-shelf-rim">`rim`</a>

By default, a shelf includes raised edges to hold on to the PCBA. This is most useful when the shelf is rotated, following the MCU, out of the xy-plane.

###### Parameter <a id="mcu-support-shelf-rim-lateral-thickness">`lateral-thickness`</a>

The thickness of material to each side of the MCU, in mm.

###### Parameter <a id="mcu-support-shelf-rim-overhang-thickness">`overhang-thickness`</a>

The thickness of material in the outermost part on each side, in mm.

###### Parameter <a id="mcu-support-shelf-rim-overhang-width">`overhang-width`</a>

The extent to which each side extends out across the PCBA, in mm.

###### Parameter <a id="mcu-support-shelf-rim-offsets">`offsets`</a>

One or two lengthwise offsets in mm. When these are left at zero, the sides of the shelf will appear in full. A negative or positive offset shortens the corresponding side, towards or away from the connector side of the PCBA.

#### Section <a id="mcu-support-lock">`lock`</a>

An MCU lock is a support feature made up of three parts:

* A fixture printed as part of the case. This fixture includes a plate for the PCB and a socket. The socket holds a USB connector on the PCB in place.
* The bolt of the lock, printed separately.
* A threaded fastener, not printed.
The fastener connects the bolt to the fixture as the lock closes over the PCB. Confusingly, the fastener constitutes a bolt, in a different sense of that word.

A lock is most appropriate when the PCB aligns with a long, flat wall; typically the wall of a rear housing. It has the advantage that it can hug the connector on the PCB tightly from four sides, thus preventing a fragile surface-mounted connector from snapping off.

##### Parameter <a id="mcu-support-lock-include">`include`</a>

If `true`, include a lock.

##### Parameter <a id="mcu-support-lock-width-factor">`width-factor`</a>

A multiplier for the width of the PCB. This determines the width of the parts touching the PCB in a lock: The plate and the base of the bolt.

##### Parameter <a id="mcu-support-lock-fastener-properties">`fastener-properties`</a>

Like the various `bolt-properties` parameters elsewhere, this parameter describes a threaded fastener using the `bolt` function in the [`scad-klupe.iso`](https://github.com/veikman/scad-klupe) library.

This particular set of fastener propertes should not include a `total-length` because the application will interpolate default values for both `unthreaded-length` and `threaded-length` based on other properties of the lock. A contradictory `total-length` is an error.

##### Section <a id="mcu-support-lock-plate">`plate`</a>

In the lock, the MCU PCBA sits on a plate, as part of the fixture. This plate is named by analogy with a roughly corresponding part in a door lock. The plate actually looks like a bed for the PCB.

The plate is typically more narrow than the PCB, its width being determined by `width-factor`. Its total height is the sum of this section’s `base-thickness` and `clearance`.

###### Parameter <a id="mcu-support-lock-plate-alias">`alias`</a>

A name you can use to target the base of the plate for `tweaks`. This is useful mainly when there isn’t a flat wall behind the lock.

###### Parameter <a id="mcu-support-lock-plate-base-thickness">`base-thickness`</a>

The thickness of the base of the plate, in mm.

###### Parameter <a id="mcu-support-lock-plate-backing-thickness">`backing-thickness`</a>

The thickness of whatever is behind the plate, in mm. The only influence of this parameter is on the length and position of the fastener, which is extended this far away from the plate to penetrate its support.

###### Parameter <a id="mcu-support-lock-plate-clearance">`clearance`</a>

The distance between the MCU PCB and the base of the plate, in mm.

Unlike the base of the plate, its clearance displaces the PCB and cannot be targeted by `tweaks`, but both parts of the plate have the same length and width.

The main use for `clearance` is to leave room between a wall supporting the lock and the PCB’s through-holes, so its height should be roughly matched to the length of wire overshoot through the PCB, with a safety margin for air.

##### Section <a id="mcu-support-lock-socket">`socket`</a>

A housing around the USB connector on the MCU PCBA.

###### Parameter <a id="mcu-support-lock-socket-thickness">`thickness`</a>

The wall thickness of the socket.

##### Section <a id="mcu-support-lock-bolt">`bolt`</a>

The bolt of the MCU lock, named by analogy with a regular door lock, is not to be confused with the threaded fastener holding it in place. The properties of the threaded fastener are set using `fastener-properties` above while the properties of the lock bolt are set here.

###### Parameter <a id="mcu-support-lock-bolt-clearance">`clearance`</a>

The distance of the bolt from the populated side of the PCB. This distance should be slightly greater than the height of the tallest component on the PCB.

###### Parameter <a id="mcu-support-lock-bolt-overshoot">`overshoot`</a>

The distance across which the bolt will touch the PCB at the mount end. Take care that this distance is free of components on the PCB.

###### Parameter <a id="mcu-support-lock-bolt-mount-length">`mount-length`</a>

The length of the base containing a threaded channel used to secure the bolt over the MCU. This is in addition to `overshoot` and goes in the opposite direction, away from the PCB.

###### Parameter <a id="mcu-support-lock-bolt-mount-thickness">`mount-thickness`</a>

The thickness of the mount. This should have some rough correspondence to the threaded portion of your fastener, which should not have a shank.

## Freely keyed section <a id="ports">`ports`</a>

This section describes ports, including sockets in the case walls to contain electronic receptacles for signalling connections and other interfaces. Each port gets its own subsection. Ports are documented in detail [here](options-ports.md).

## Section <a id="wrist-rest">`wrist-rest`</a>

An optional extension to support the user’s wrist.

### Parameter <a id="wrist-rest-include">`include`</a>

If `true`, include a wrist rest with the keyboard.

### Parameter <a id="wrist-rest-style">`style`</a>

The style of the wrist rest. Available styles are:

- `threaded`: threaded fasteners connect the case and wrist rest.
- `solid`: the case and wrist rest are joined together by `tweaks` as a single piece of plastic.

### Parameter <a id="wrist-rest-preview">`preview`</a>

Preview mode. If `true`, this puts a model of the wrist rest in the same OpenSCAD file as the case. That model is simplified, intended for gauging distance, not for printing.

### Section <a id="wrist-rest-anchoring">`anchoring`</a>

Where to place the wrist rest. The concept of anchoring is explained [here](options-anchoring.md), along with the parameters available in this section. For wrist rests, the vertical component of the anchor’s position is ignored, including any vertical offset.

### Parameter <a id="wrist-rest-plinth-height">`plinth-height`</a>

The average height of the plastic plinth in mm, at its upper lip.

### Section <a id="wrist-rest-shape">`shape`</a>

The wrist rest needs to fit the user’s hand.

#### Section <a id="wrist-rest-shape-spline">`spline`</a>

The horizontal outline of the wrist rest is a closed spline.

##### Parameter <a id="wrist-rest-shape-spline-main-points">`main-points`</a>

A list of nameable points, in clockwise order. The spline will pass through all of these and then return to the first one. Each point can have two properties:

- Mandatory: `position`. A pair of coordinates, in mm, relative to other points in the list.
- Optional: `alias`. A name given to the specific point, for the purpose of placing yet more things in relation to it.

##### Parameter <a id="wrist-rest-shape-spline-resolution">`resolution`</a>

The amount of vertices per main point. The default is 1. If 1, only the main points themselves will be used, giving you full control. A higher number gives you smoother curves.

If you want the closing part of the curve to look smooth in high resolution, position your main points carefully.

Resolution parameters, including this one, can be disabled in the main `resolution` section.

#### Section <a id="wrist-rest-shape-lip">`lip`</a>

The lip is the uppermost part of the plinth, lining and supporting the edge of the pad. Its dimensions are described here in mm away from the pad.

##### Parameter <a id="wrist-rest-shape-lip-height">`height`</a>

The vertical extent of the lip.

##### Parameter <a id="wrist-rest-shape-lip-width">`width`</a>

The horizontal width of the lip at its top.

##### Parameter <a id="wrist-rest-shape-lip-inset">`inset`</a>

The difference in width between the top and bottom of the lip. A small negative value will make the lip thicker at the bottom. This is recommended for fitting a silicone mould.

#### Section <a id="wrist-rest-shape-pad">`pad`</a>

The top of the wrist rest should be printed or cast in a soft material, such as silicone rubber.

##### Section <a id="wrist-rest-shape-pad-surface">`surface`</a>

The upper surface of the pad, which will be in direct contact with the user’s palm or wrist.

###### Section <a id="wrist-rest-shape-pad-surface-edge">`edge`</a>

The edge of the pad can be rounded.

###### Parameter <a id="wrist-rest-shape-pad-surface-edge-inset">`inset`</a> at level 7

The horizontal extent of softening. This cannot be more than half the width of the outline, as determined by `main-points`, at its narrowest part.

###### Parameter <a id="wrist-rest-shape-pad-surface-edge-resolution">`resolution`</a> at level 7

The number of faces on the edge between horizontal points.

Resolution parameters, including this one, can be disabled in the main `resolution` section.

###### Section <a id="wrist-rest-shape-pad-surface-heightmap">`heightmap`</a>

The surface can optionally be modified by the [`surface()` function](https://en.wikibooks.org/wiki/OpenSCAD_User_Manual/Other_Language_Features#Surface), which requires a heightmap file.

###### Parameter <a id="wrist-rest-shape-pad-surface-heightmap-include">`include`</a> at level 7

If `true`, use a heightmap. The map will intersect the basic pad polyhedron.

###### Parameter <a id="wrist-rest-shape-pad-surface-heightmap-filepath">`filepath`</a> at level 7

The file identified here should contain a heightmap in a format OpenSCAD can understand. The path should also be resolvable by OpenSCAD.

##### Section <a id="wrist-rest-shape-pad-height">`height`</a>

The piece of rubber extends a certain distance up into the air and down into the plinth. All measurements in mm.

###### Parameter <a id="wrist-rest-shape-pad-height-surface-range">`surface-range`</a>

The vertical range of the upper surface. Whatever values are in a heightmap will be normalized to this scale.

###### Parameter <a id="wrist-rest-shape-pad-height-lip-to-surface">`lip-to-surface`</a>

The part of the rubber pad between the top of the lip and the point where the heightmap comes into effect. This is useful if your heightmap itself has very low values at the edges, such that moulding and casting it without a base would be difficult.

###### Parameter <a id="wrist-rest-shape-pad-height-below-lip">`below-lip`</a>

The depth of the rubber wrist support, measured from the top of the lip, going down into the plinth. This part of the pad just keeps it in place.

### Section <a id="wrist-rest-rotation">`rotation`</a>

The wrist rest can be rotated to align its pad with the user’s palm.

#### Parameter <a id="wrist-rest-rotation-pitch">`pitch`</a>

Tait-Bryan pitch.

#### Parameter <a id="wrist-rest-rotation-roll">`roll`</a>

Tait-Bryan roll.

### Special section <a id="wrist-rest-mounts">`mounts`</a>

A list of mounts for threaded fasteners. Each such mount will include at least one cuboid block for at least one screw that connects the wrist rest to the case. This section is used only with the `threaded` style of wrist rest.

### Section <a id="wrist-rest-sprues">`sprues`</a>

Holes in the bottom of the plinth. You pour liquid rubber through these holes when you make the rubber pad. Sprues are optional, but the general recommendation is to have two of them if you’re going to be casting your own pads. That way, air can escape even if you accidentally block one sprue with a low-viscosity silicone.

#### Parameter <a id="wrist-rest-sprues-include">`include`</a>

If `true`, include sprues.

#### Parameter <a id="wrist-rest-sprues-inset">`inset`</a>

The horizontal distance between the perimeter of the wrist rest and the default position of each sprue.

#### Parameter <a id="wrist-rest-sprues-diameter">`diameter`</a>

The diameter of each sprue.

#### Parameter <a id="wrist-rest-sprues-positions">`positions`</a>

The positions of all sprues. This is a list where each item needs an `anchor` naming a main point in the spline. You can add an optional two-dimensional `offset`.

### Section <a id="wrist-rest-bottom-plate">`bottom-plate`</a>

The equivalent of the case `bottom-plate` parameter. If included, a bottom plate for a wrist rest uses the `thickness` configured for the bottom of the case.

Bottom plates for the wrist rests have no ESDS electronics to protect but serve other purposes: Covering nut pockets, silicone mould-pour cavities, and plaster or other dense material poured into plinths printed without a bottom shell.

#### Parameter <a id="wrist-rest-bottom-plate-include">`include`</a>

Whether to include a bottom plate for each wrist rest.

#### Parameter <a id="wrist-rest-bottom-plate-inset">`inset`</a>

The horizontal distance between the perimeter of the wrist rest and the default position of each threaded fastener connecting it to its bottom plate.

#### Parameter <a id="wrist-rest-bottom-plate-fastener-positions">`fastener-positions`</a>

The positions of threaded fasteners used to attach the bottom plate to its wrist rest. The syntax of this parameter is precisely the same as for the case’s bottom-plate fasteners. Corners are ignored and the starting position is inset from the perimeter of the wrist rest by the `inset` parameter above, before any offset stated here is applied.

Other properties of these fasteners are determined by settings for the case.

### Parameter <a id="wrist-rest-mould-thickness">`mould-thickness`</a>

The thickness in mm of the walls and floor of the mould to be used for casting the rubber pad.

## Section <a id="resolution">`resolution`</a>

Settings for the amount of detail on curved surfaces. More specific resolution parameters are available in other sections.

### Parameter <a id="resolution-include">`include`</a>

If `true`, apply resolution parameters found throughout the configuration. Otherwise, use defaults built into this application, its libraries and OpenSCAD. The defaults are generally conservative, providing quick renders for previews.

### Parameter <a id="resolution-minimum-face-size">`minimum-face-size`</a>

File-wide OpenSCAD minimum face size in mm.

## Section <a id="dfm">`dfm`</a>

Settings for design for manufacturability (DFM).

### Parameter <a id="dfm-error-general">`error-general`</a>

A measurement in mm of errors introduced to negative space in the xy plane by slicer software and the printer you will use.

The default value is zero. An appropriate value for a typical slicer and FDM printer with a 0.5 mm nozzle would be about -0.5 mm.

This application will try to compensate for the error, though only for certain sensitive inserts, not for the case as a whole.

### Section <a id="dfm-keycaps">`keycaps`</a>

Measurements of error, in mm, for parts of keycap models. This is separate from `error-general` because it’s especially important to have a tight fit between switch sliders and cap stems, and the size of these details is usually comparable to an FDM printer nozzle.

If you will not be printing caps, ignore this section.

#### Parameter <a id="dfm-keycaps-error-stem-positive">`error-stem-positive`</a>

Error on the positive components of stems on keycaps, such as the entire stem on an ALPS-compatible cap.

#### Parameter <a id="dfm-keycaps-error-stem-negative">`error-stem-negative`</a>

Error on the negative components of stems on keycaps, such as the cross on an MX-compatible cap.

### Section <a id="dfm-bottom-plate">`bottom-plate`</a>

DFM for bottom plates.

#### Parameter <a id="dfm-bottom-plate-fastener-plate-offset">`fastener-plate-offset`</a>

A vertical offset in mm for the placement of screw holes in bottom plates. Without a slight negative offset, slicers will tend to make the holes too wide for screw heads to grip the plate securely.

Notice this will not affect how screw holes are cut into the case.

## Section <a id="mask">`mask`</a>

A box limits the entire shape, cutting off any projecting by-products of the algorithms. By resizing and moving this box, you can select a subsection for printing. You might want this while you are printing prototypes for a new style of switch, MCU support etc.

### Parameter <a id="mask-size">`size`</a>

The size of the mask in mm. By default, a cube of 1 m³.

### Parameter <a id="mask-center">`center`</a>

The position of the center point of the mask. By default, `[0, 0, 500]`, which is supposed to mask out everything below ground level. If you include bottom plates, their thickness will automatically affect the placement of the mask beyond what you specify here.

⸻

This document was generated from the application CLI.
