<!--This document was generated and is intended for rendering to HTML on GitHub. Edit the source files, not this file.-->

# General configuration options

Each heading in this document represents a recognized configuration key in the main body of a YAML file for a DMOTE variant. Other documents cover special sections of this one in more detail.

## Table of contents
- Parameter <a href="#user-content-split">`split`</a>
- Section <a href="#user-content-keys">`keys`</a>
    - Parameter <a href="#user-content-keys-preview">`preview`</a>
    - Parameter <a href="#user-content-keys-styles">`styles`</a>
- Special section <a href="#user-content-key-clusters">`key-clusters`</a>
- Section <a href="#user-content-by-key">`by-key`</a>
    - Special recurring section <a href="#user-content-by-key-parameters">`parameters`</a>
    - Special section <a href="#user-content-by-key-clusters">`clusters`</a> ← overrides go in here
- Parameter <a href="#user-content-secondaries">`secondaries`</a>
- Section <a href="#user-content-case">`case`</a>
    - Parameter <a href="#user-content-case-key-mount-thickness">`key-mount-thickness`</a>
    - Parameter <a href="#user-content-case-key-mount-corner-margin">`key-mount-corner-margin`</a>
    - Parameter <a href="#user-content-case-web-thickness">`web-thickness`</a>
    - Section <a href="#user-content-case-rear-housing">`rear-housing`</a>
        - Parameter <a href="#user-content-case-rear-housing-include">`include`</a>
        - Parameter <a href="#user-content-case-rear-housing-wall-thickness">`wall-thickness`</a>
        - Parameter <a href="#user-content-case-rear-housing-roof-thickness">`roof-thickness`</a>
        - Section <a href="#user-content-case-rear-housing-position">`position`</a>
            - Parameter <a href="#user-content-case-rear-housing-position-cluster">`cluster`</a>
            - Section <a href="#user-content-case-rear-housing-position-offsets">`offsets`</a>
                - Parameter <a href="#user-content-case-rear-housing-position-offsets-north">`north`</a>
                - Parameter <a href="#user-content-case-rear-housing-position-offsets-west">`west`</a>
                - Parameter <a href="#user-content-case-rear-housing-position-offsets-east">`east`</a>
                - Parameter <a href="#user-content-case-rear-housing-position-offsets-south">`south`</a>
        - Parameter <a href="#user-content-case-rear-housing-height">`height`</a>
        - Section <a href="#user-content-case-rear-housing-fasteners">`fasteners`</a>
            - Parameter <a href="#user-content-case-rear-housing-fasteners-diameter">`diameter`</a>
            - Parameter <a href="#user-content-case-rear-housing-fasteners-bosses">`bosses`</a>
            - Section <a href="#user-content-case-rear-housing-fasteners-west">`west`</a>
                - Parameter <a href="#user-content-case-rear-housing-fasteners-west-include">`include`</a>
                - Parameter <a href="#user-content-case-rear-housing-fasteners-west-offset">`offset`</a>
            - Section <a href="#user-content-case-rear-housing-fasteners-east">`east`</a>
                - Parameter <a href="#user-content-case-rear-housing-fasteners-east-include">`include`</a>
                - Parameter <a href="#user-content-case-rear-housing-fasteners-east-offset">`offset`</a>
    - Section <a href="#user-content-case-back-plate">`back-plate`</a>
        - Parameter <a href="#user-content-case-back-plate-include">`include`</a>
        - Parameter <a href="#user-content-case-back-plate-beam-height">`beam-height`</a>
        - Section <a href="#user-content-case-back-plate-fasteners">`fasteners`</a>
            - Parameter <a href="#user-content-case-back-plate-fasteners-diameter">`diameter`</a>
            - Parameter <a href="#user-content-case-back-plate-fasteners-distance">`distance`</a>
            - Parameter <a href="#user-content-case-back-plate-fasteners-bosses">`bosses`</a>
        - Section <a href="#user-content-case-back-plate-position">`position`</a>
            - Parameter <a href="#user-content-case-back-plate-position-anchor">`anchor`</a>
            - Parameter <a href="#user-content-case-back-plate-position-offset">`offset`</a>
    - Section <a href="#user-content-case-bottom-plate">`bottom-plate`</a>
        - Parameter <a href="#user-content-case-bottom-plate-include">`include`</a>
        - Parameter <a href="#user-content-case-bottom-plate-preview">`preview`</a>
        - Parameter <a href="#user-content-case-bottom-plate-combine">`combine`</a>
        - Parameter <a href="#user-content-case-bottom-plate-thickness">`thickness`</a>
        - Section <a href="#user-content-case-bottom-plate-installation">`installation`</a>
            - Parameter <a href="#user-content-case-bottom-plate-installation-style">`style`</a>
            - Parameter <a href="#user-content-case-bottom-plate-installation-thickness">`thickness`</a>
            - Section <a href="#user-content-case-bottom-plate-installation-inserts">`inserts`</a>
                - Parameter <a href="#user-content-case-bottom-plate-installation-inserts-length">`length`</a>
                - Section <a href="#user-content-case-bottom-plate-installation-inserts-diameter">`diameter`</a>
                    - Parameter <a href="#user-content-case-bottom-plate-installation-inserts-diameter-top">`top`</a>
                    - Parameter <a href="#user-content-case-bottom-plate-installation-inserts-diameter-bottom">`bottom`</a>
            - Section <a href="#user-content-case-bottom-plate-installation-fasteners">`fasteners`</a>
                - Parameter <a href="#user-content-case-bottom-plate-installation-fasteners-diameter">`diameter`</a>
                - Parameter <a href="#user-content-case-bottom-plate-installation-fasteners-length">`length`</a>
                - Parameter <a href="#user-content-case-bottom-plate-installation-fasteners-positions">`positions`</a>
    - Section <a href="#user-content-case-leds">`leds`</a>
        - Parameter <a href="#user-content-case-leds-include">`include`</a>
        - Section <a href="#user-content-case-leds-position">`position`</a>
            - Parameter <a href="#user-content-case-leds-position-cluster">`cluster`</a>
        - Parameter <a href="#user-content-case-leds-amount">`amount`</a>
        - Parameter <a href="#user-content-case-leds-housing-size">`housing-size`</a>
        - Parameter <a href="#user-content-case-leds-emitter-diameter">`emitter-diameter`</a>
        - Parameter <a href="#user-content-case-leds-interval">`interval`</a>
    - Parameter <a href="#user-content-case-tweaks">`tweaks`</a>
    - Section <a href="#user-content-case-foot-plates">`foot-plates`</a>
        - Parameter <a href="#user-content-case-foot-plates-include">`include`</a>
        - Parameter <a href="#user-content-case-foot-plates-height">`height`</a>
        - Parameter <a href="#user-content-case-foot-plates-polygons">`polygons`</a>
- Section <a href="#user-content-mcu">`mcu`</a>
    - Parameter <a href="#user-content-mcu-include">`include`</a>
    - Parameter <a href="#user-content-mcu-preview">`preview`</a>
    - Parameter <a href="#user-content-mcu-type">`type`</a>
    - Parameter <a href="#user-content-mcu-margin">`margin`</a>
    - Section <a href="#user-content-mcu-position">`position`</a>
        - Parameter <a href="#user-content-mcu-position-prefer-rear-housing">`prefer-rear-housing`</a>
        - Parameter <a href="#user-content-mcu-position-anchor">`anchor`</a>
        - Parameter <a href="#user-content-mcu-position-corner">`corner`</a>
        - Parameter <a href="#user-content-mcu-position-offset">`offset`</a>
        - Parameter <a href="#user-content-mcu-position-rotation">`rotation`</a>
    - Section <a href="#user-content-mcu-support">`support`</a>
        - Parameter <a href="#user-content-mcu-support-style">`style`</a>
        - Parameter <a href="#user-content-mcu-support-preview">`preview`</a>
        - Parameter <a href="#user-content-mcu-support-height-factor">`height-factor`</a>
        - Parameter <a href="#user-content-mcu-support-lateral-spacing">`lateral-spacing`</a>
        - Section <a href="#user-content-mcu-support-lock">`lock`</a>
            - Section <a href="#user-content-mcu-support-lock-fastener">`fastener`</a>
                - Parameter <a href="#user-content-mcu-support-lock-fastener-style">`style`</a>
                - Parameter <a href="#user-content-mcu-support-lock-fastener-diameter">`diameter`</a>
            - Section <a href="#user-content-mcu-support-lock-socket">`socket`</a>
                - Parameter <a href="#user-content-mcu-support-lock-socket-thickness">`thickness`</a>
            - Section <a href="#user-content-mcu-support-lock-bolt">`bolt`</a>
                - Parameter <a href="#user-content-mcu-support-lock-bolt-clearance">`clearance`</a>
                - Parameter <a href="#user-content-mcu-support-lock-bolt-overshoot">`overshoot`</a>
                - Parameter <a href="#user-content-mcu-support-lock-bolt-mount-length">`mount-length`</a>
                - Parameter <a href="#user-content-mcu-support-lock-bolt-mount-thickness">`mount-thickness`</a>
        - Section <a href="#user-content-mcu-support-stop">`stop`</a>
            - Parameter <a href="#user-content-mcu-support-stop-anchor">`anchor`</a>
            - Parameter <a href="#user-content-mcu-support-stop-direction">`direction`</a>
            - Section <a href="#user-content-mcu-support-stop-gripper">`gripper`</a>
                - Parameter <a href="#user-content-mcu-support-stop-gripper-notch-depth">`notch-depth`</a>
                - Parameter <a href="#user-content-mcu-support-stop-gripper-total-depth">`total-depth`</a>
                - Parameter <a href="#user-content-mcu-support-stop-gripper-grip-width">`grip-width`</a>
- Section <a href="#user-content-connection">`connection`</a>
    - Parameter <a href="#user-content-connection-include">`include`</a>
    - Parameter <a href="#user-content-connection-socket-size">`socket-size`</a>
    - Parameter <a href="#user-content-connection-socket-thickness">`socket-thickness`</a>
    - Section <a href="#user-content-connection-position">`position`</a>
        - Parameter <a href="#user-content-connection-position-prefer-rear-housing">`prefer-rear-housing`</a>
        - Parameter <a href="#user-content-connection-position-anchor">`anchor`</a>
        - Parameter <a href="#user-content-connection-position-corner">`corner`</a>
        - Parameter <a href="#user-content-connection-position-raise">`raise`</a>
        - Parameter <a href="#user-content-connection-position-offset">`offset`</a>
        - Parameter <a href="#user-content-connection-position-rotation">`rotation`</a>
- Section <a href="#user-content-wrist-rest">`wrist-rest`</a>
    - Parameter <a href="#user-content-wrist-rest-include">`include`</a>
    - Parameter <a href="#user-content-wrist-rest-style">`style`</a>
    - Parameter <a href="#user-content-wrist-rest-preview">`preview`</a>
    - Section <a href="#user-content-wrist-rest-position">`position`</a>
        - Parameter <a href="#user-content-wrist-rest-position-anchor">`anchor`</a>
        - Parameter <a href="#user-content-wrist-rest-position-corner">`corner`</a>
        - Parameter <a href="#user-content-wrist-rest-position-offset">`offset`</a>
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

## Parameter <a id="split">`split`</a>

If `true`, build two versions of the case: One for the right hand and a mirror image for the left hand. Threaded holes and other chiral components of the case are exempted from mirroring.

## Section <a id="keys">`keys`</a>

Keys, that is keycaps and electrical switches, are not the main focus of this application, but they influence the shape of the case.

### Parameter <a id="keys-preview">`preview`</a>

If `true`, include models of the keycaps in place on the keyboard. This is intended for illustration as you work on a design, not for printing.

### Parameter <a id="keys-styles">`styles`</a>

Here you name all the styles of keys on the keyboard and describe each style using parameters to the `keycap` function of the [`dmote-keycap`](https://github.com/veikman/dmote-keycap) library. Switch type is one aspect of key style.

These key styles determine the size of key mounting plates on the keyboard and what kind of holes are cut into those plates for the switches to fit inside. Negative space is also reserved above the plate for the movement of the keycap: A function of switch height, switch travel, and keycap shape. In addition, if the keyboard is curved, key styles help determine the spacing between key mounts.

In options by key, documented [here](options-nested.md), you specify which style of key is used for each position on the keyboard.

## Special section <a id="key-clusters">`key-clusters`</a>

This section describes the general size, shape and position of the clusters of keys on the keyboard, each in its own subsection. It is documented in detail [here](options-clusters.md).

## Section <a id="by-key">`by-key`</a>

This section repeats. Each level of settings inside it is more specific to a smaller part of the keyboard, eventually reaching the level of individual keys. It’s all documented [here](options-nested.md).

### Special recurring section <a id="by-key-parameters">`parameters`</a>

Default values at the global level.

### Special section <a id="by-key-clusters">`clusters`</a> ← overrides go in here

Starting here, you gradually descend from the global level toward the key level.

## Parameter <a id="secondaries">`secondaries`</a>

A map where each item provides a name for a position in space. Such positions exist in relation to other named features of the keyboard and can themselves be used as named features: Typically as supplementary targets for `tweaks`, which are defined below.

An example:

```secondaries:
  s0:
    anchor: f0
    corner: NNE
    segment: 3
    offset: [0, 0, 10]
```
This example gives the name `s0` to a point 10 mm above a key or some other feature named `f0`, which must be defined elsewhere.

A `corner` and `segment` are useful mainly with key aliases. An `offset` is applied late, i.e. in the overall coordinate system, following any transformations inherent to the anchor.

## Section <a id="case">`case`</a>

Much of the keyboard case is generated from the `wall` parameters described [here](options-nested.md). This section deals with lesser features of the case.

### Parameter <a id="case-key-mount-thickness">`key-mount-thickness`</a>

The thickness in mm of each switch key mounting plate.

### Parameter <a id="case-key-mount-corner-margin">`key-mount-corner-margin`</a>

The thickness in mm of an imaginary “post” at each corner of each key mount. Copies of such posts project from the key mounts to form the main walls of the case.

`key-mount-thickness` is similarly the height of each post.

### Parameter <a id="case-web-thickness">`web-thickness`</a>

The thickness in mm of the webbing between switch key mounting plates, and of the rear housing’s walls and roof.

### Section <a id="case-rear-housing">`rear-housing`</a>

The furthest row of a key cluster can be extended into a rear housing for the MCU and various other features.

#### Parameter <a id="case-rear-housing-include">`include`</a>

If `true`, add a rear housing. Please arrange case walls so as not to interfere, by removing them along the far side of the last row of key mounts in the indicated cluster.

#### Parameter <a id="case-rear-housing-wall-thickness">`wall-thickness`</a>

The horizontal thickness in mm of the walls.

#### Parameter <a id="case-rear-housing-roof-thickness">`roof-thickness`</a>

The vertical thickness in mm of the flat top.

#### Section <a id="case-rear-housing-position">`position`</a>

Where to put the rear housing. By default, it sits all along the far side of the `main` cluster but has no depth.

##### Parameter <a id="case-rear-housing-position-cluster">`cluster`</a>

The key cluster at which to anchor the housing.

##### Section <a id="case-rear-housing-position-offsets">`offsets`</a>

Modifiers for where to put the four sides of the roof. All are in mm.

###### Parameter <a id="case-rear-housing-position-offsets-north">`north`</a>

The extent of the roof on the y axis; its horizontal depth.

###### Parameter <a id="case-rear-housing-position-offsets-west">`west`</a>

The extent on the x axis past the first key in the row.

###### Parameter <a id="case-rear-housing-position-offsets-east">`east`</a>

The extent on the x axis past the last key in the row.

###### Parameter <a id="case-rear-housing-position-offsets-south">`south`</a>

The horizontal distance in mm, on the y axis, between the furthest key in the row and the roof of the rear housing.

#### Parameter <a id="case-rear-housing-height">`height`</a>

The height in mm of the roof, over the floor.

#### Section <a id="case-rear-housing-fasteners">`fasteners`</a>

Threaded bolts can run through the roof of the rear housing, making it a hardpoint for attachments like a stabilizer to connect the two halves of a split keyboard.

##### Parameter <a id="case-rear-housing-fasteners-diameter">`diameter`</a>

The ISO metric diameter of each fastener.

##### Parameter <a id="case-rear-housing-fasteners-bosses">`bosses`</a>

If `true`, add nut bosses to the ceiling of the rear housing for each fastener. Space permitting, these bosses will have some play on the north-south axis, to permit adjustment of the angle of the keyboard halves under a stabilizer.

##### Section <a id="case-rear-housing-fasteners-west">`west`</a>

A fastener on the inward-facing end of the rear housing.

###### Parameter <a id="case-rear-housing-fasteners-west-include">`include`</a>

If `true`, include this fastener.

###### Parameter <a id="case-rear-housing-fasteners-west-offset">`offset`</a>

A one-dimensional offset in mm from the inward edge of the rear housing to the fastener. You probably want a negative number if any.

##### Section <a id="case-rear-housing-fasteners-east">`east`</a>

A fastener on the outward-facing end of the rear housing. All parameters are analogous to those for `west`.

###### Parameter <a id="case-rear-housing-fasteners-east-include">`include`</a>



###### Parameter <a id="case-rear-housing-fasteners-east-offset">`offset`</a>



### Section <a id="case-back-plate">`back-plate`</a>

Given that independent movement of each half of a split keyboard is not useful, each half can include a mounting plate for a stabilizing ‘beam’. That is a straight piece of wood, aluminium, rigid plastic etc. to connect the two halves mechanically and possibly carry the wire that connects them electrically.

This option is similar to rear housing, but the back plate block provides no interior space for an MCU etc. It is solid, with holes for threaded fasteners including the option of nut bosses. Its footprint is not part of a `bottom-plate`.

#### Parameter <a id="case-back-plate-include">`include`</a>

If `true`, include a back plate block. This is not contingent upon `split`.

#### Parameter <a id="case-back-plate-beam-height">`beam-height`</a>

The nominal vertical extent of the back plate in mm. Because the plate is bottom-hulled to the floor, the effect of this setting is on the area of the plate above its holes.

#### Section <a id="case-back-plate-fasteners">`fasteners`</a>

Two threaded bolts run through the back plate.

##### Parameter <a id="case-back-plate-fasteners-diameter">`diameter`</a>

The ISO metric diameter of each fastener.

##### Parameter <a id="case-back-plate-fasteners-distance">`distance`</a>

The horizontal distance between the fasteners.

##### Parameter <a id="case-back-plate-fasteners-bosses">`bosses`</a>

If `true`, cut nut bosses into the inside wall of the block.

#### Section <a id="case-back-plate-position">`position`</a>

The block is positioned in relation to a named feature.

##### Parameter <a id="case-back-plate-position-anchor">`anchor`</a>

The name of a feature where the block will attach.

##### Parameter <a id="case-back-plate-position-offset">`offset`</a>

An offset in mm from the named feature to the middle of the base of the back-plate block.

### Section <a id="case-bottom-plate">`bottom-plate`</a>

A bottom plate can be added to close the case. This is useful mainly to simplify transportation.

#### Overview

The bottom plate is largely two-dimensional. The application builds most of it from a set of polygons, trying to match the perimeter of the case at the ground level (i.e. z = 0).

Specifically, there is one polygon per key cluster, limited to `full` wall edges, one polygon for the rear housing, and one set of polygons for each of the first-level case `tweaks` that use `at-ground`, ignoring chunk size and almost ignoring tweaks nested within lists of tweaks.

This methodology is mentioned here because its results are not perfect. Pending future features in OpenSCAD, a future version may be based on a more exact projection of the case, but as of 2018, such a projection is hollow and cannot be convex-hulled without escaping the case, unless your case is convex to start with.

For this reason, while the polygons fill the interior, the perimeter of the bottom plate is extended by key walls and case `tweaks` as they would appear at the height of the bottom plate. Even this brutality may be inadequate. If you require a more exact match, do a projection of the case without a bottom plate, save it as DXF/SVG etc. and post-process that file to fill the interior gap.


#### Parameter <a id="case-bottom-plate-include">`include`</a>

If `true`, include a bottom plate for the case.

#### Parameter <a id="case-bottom-plate-preview">`preview`</a>

Preview mode. If `true`, put a model of the plate in the same file as the case it closes. Not for printing.

#### Parameter <a id="case-bottom-plate-combine">`combine`</a>

If `true`, combine wrist rests for the case and the bottom plate into a single model, when both are enabled. This is typically used with the `solid` style of wrest rest.

#### Parameter <a id="case-bottom-plate-thickness">`thickness`</a>

The thickness (i.e. height) in mm of all bottom plates you choose to include. This covers plates for the case and for the wrist rest.

The case will not be raised to compensate for this. Instead, the height of the bottom plate will be removed from the bottom of the main model so that it does not extend to z = 0.

#### Section <a id="case-bottom-plate-installation">`installation`</a>

How your bottom plate is attached to the rest of your case.

##### Parameter <a id="case-bottom-plate-installation-style">`style`</a>

The general means of installation. All currently available styles use threaded fasteners with countersunk heads. The styles differ only in how these fasteners attach to the case.

One of:

- `threads`: Threaded holes in the case.
- `inserts`: Unthreaded holes for threaded heat-set inserts.

##### Parameter <a id="case-bottom-plate-installation-thickness">`thickness`</a>

The thickness in mm of each wall of the anchor points.

##### Section <a id="case-bottom-plate-installation-inserts">`inserts`</a>

Properties of heat-set inserts for the `inserts` style.

###### Parameter <a id="case-bottom-plate-installation-inserts-length">`length`</a>

The length in mm of each insert.

###### Section <a id="case-bottom-plate-installation-inserts-diameter">`diameter`</a>

It is assumed that, as in Tom Short’s Dactyl-ManuForm, the inserts are largely cylindrical but vary in diameter across their length.

###### Parameter <a id="case-bottom-plate-installation-inserts-diameter-top">`top`</a> at level 7

Top diameter in m.

###### Parameter <a id="case-bottom-plate-installation-inserts-diameter-bottom">`bottom`</a> at level 7

Bottom diameter in mm. This needs to be at least as large as the top diameter since the mounts for the inserts only open from the bottom.

##### Section <a id="case-bottom-plate-installation-fasteners">`fasteners`</a>

The type and positions of the threaded fasteners used to secure each bottom plate.

###### Parameter <a id="case-bottom-plate-installation-fasteners-diameter">`diameter`</a>

The ISO metric diameter of each fastener.

###### Parameter <a id="case-bottom-plate-installation-fasteners-length">`length`</a>

The length in mm of each fastener. In the `threads` style, this refers to the part of the screw that is itself threaded: It excludes the head.

###### Parameter <a id="case-bottom-plate-installation-fasteners-positions">`positions`</a>

A list of places where threaded fasteners will connect the bottom plate to the rest of the case.

### Section <a id="case-leds">`leds`</a>

Support for light-emitting diodes in the case walls.

#### Parameter <a id="case-leds-include">`include`</a>

If `true`, cut slots for LEDs out of the case wall, facing the space between the two halves.

#### Section <a id="case-leds-position">`position`</a>

Where to attach the LED strip.

##### Parameter <a id="case-leds-position-cluster">`cluster`</a>

The key cluster at which to anchor the strip.

#### Parameter <a id="case-leds-amount">`amount`</a>

The number of LEDs.

#### Parameter <a id="case-leds-housing-size">`housing-size`</a>

The length of the side on a square profile used to create negative space for the housings on a LED strip. This assumes the housings are squarish, as on a WS2818.

The negative space is not supposed to penetrate the wall, just make it easier to hold the LED strip in place with tape, and direct its light. With that in mind, feel free to exaggerate by 10%.

#### Parameter <a id="case-leds-emitter-diameter">`emitter-diameter`</a>

The diameter of a round hole for the light of an LED.

#### Parameter <a id="case-leds-interval">`interval`</a>

The distance between LEDs on the strip. You may want to apply a setting slightly shorter than the real distance, since the algorithm carving the holes does not account for wall curvature.

### Parameter <a id="case-tweaks">`tweaks`</a>

Additional shapes. This is usually needed to bridge gaps between the walls of the key clusters. The expected value here is an arbitrarily nested structure starting with a map of names to lists.

The names at the top level are arbitrary but should be distinct and descriptive. Their only technical significance lies in the fact that when you combine multiple configuration files, a later tweak will override a previous tweak if and only if they share the same name.

Below the names, each item in each list can follow one of the following patterns:

- A leaf node. This is a tuple of 1 to 4 elements specified below.
- A map, representing an instruction to combine nested items in a specific way.
- A list of any combination of the other two types. This type exists at the second level from the top and as the immediate child of each map node.

Each leaf node identifies a particular named feature of the keyboard. It’s usually a set of corner posts on a named (aliased) key mount. These are identical to the posts used to build the walls, but this section gives you greater freedom in how to combine them. The elements of a leaf are, in order:

1. Mandatory: The name of a feature, such as a key alias.
2. Optional: A corner ID, such as `NNE` for north by north-east. If this is omitted, i.e. if only the mandatory element is given, the tweak will use the middle of the named feature.
3. Optional: A starting wall segment ID, which is an integer from 0 to 4 inclusive. If this is omitted, but a corner is named, the default value is 0.
4. Optional: A second wall segment ID. If this is provided, the leaf will represent the convex hull of the two indicated segments plus all segments between them. If this is omitted, only one wall post will be placed.

By default, a map node will create a convex hull around its child nodes. However, this behaviour can be modified. The following keys are recognized:

- `at-ground`: If `true`, child nodes will be extended vertically down to the ground plane, as with a `full` wall. The default value for this key is `false`. See also: `bottom-plate`.
- `above-ground`: If `true`, child nodes will be visible as part of the case. The default value for this key is `true`.
- `chunk-size`: Any integer greater than 1. If this is set, child nodes will not share a single convex hull. Instead, there will be a sequence of smaller hulls, each encompassing this many items.
- `highlight`: If `true`, render the node in OpenSCAD’s highlighting style. This is convenient while you work.
- `hull-around`: The list of child nodes. Required.

In the following example, `A` and `B` are key aliases that would be defined elsewhere. The example is interpreted to mean that a plate should be created stretching from the south-by-southeast corner of `A` to the north-by-northeast corner of `B`. Due to `chunk-size` 2, that first plate will be joined, not hulled, with a second plate from `B` back to a different corner of `A`, with a longer stretch of (all) wall segments down the corner of `A`.

```case:
  tweaks:
    bridge-between-A-and-B:
      - chunk-size: 2
        hull-around:
        - [A, SSE]
        - [B, NNE]
        - [A, SSW, 0, 4]
```

### Section <a id="case-foot-plates">`foot-plates`</a>

Optional flat surfaces at ground level for adding silicone rubber feet or cork strips etc. to the bottom of the keyboard to increase friction and/or improve feel, sound and ground clearance.

#### Parameter <a id="case-foot-plates-include">`include`</a>

If `true`, include foot plates.

#### Parameter <a id="case-foot-plates-height">`height`</a>

The height in mm of each mounting plate.

#### Parameter <a id="case-foot-plates-polygons">`polygons`</a>

A list describing the horizontal shape, size and position of each mounting plate as a polygon.

## Section <a id="mcu">`mcu`</a>

This is short for ”micro-controller unit”. Each half has one.

### Parameter <a id="mcu-include">`include`</a>

If `true`, build support for the MCU PCBA.

### Parameter <a id="mcu-preview">`preview`</a>

If `true`, render a visualization of the MCU PCBA. For use in development.

### Parameter <a id="mcu-type">`type`</a>

A symbolic name for a commercial product. Currently, only `promicro` is supported, referring to any MCU PCBA with the dimensions of a SparkFun Pro Micro, including That-Canadian’s Elite-C.

### Parameter <a id="mcu-margin">`margin`</a>

A general measurement in mm of extra space around each part of the PCBA, including PCB and USB connector. This is applied to DMOTE components meant to hold the PCBA in place, accounting for printing inaccuracy as well as inaccuracies in manufacturing the PCBA.

### Section <a id="mcu-position">`position`</a>

Where to place the MCU PCBA.

#### Parameter <a id="mcu-position-prefer-rear-housing">`prefer-rear-housing`</a>

If `true` and `rear-housing` is included, place the PCBA in relation to the rear housing. Otherwise, place the PCBA in relation to a named feature identified by `anchor`.

#### Parameter <a id="mcu-position-anchor">`anchor`</a>

The name of a key at which to place the PCBA if `prefer-rear-housing` is `false` or rear housing is not included.

#### Parameter <a id="mcu-position-corner">`corner`</a>

A code for a corner of the `anchor` feature. This determines both the location and facing of the PCBA.

#### Parameter <a id="mcu-position-offset">`offset`</a>

A 3D offset in mm, measuring from the `corner`.

#### Parameter <a id="mcu-position-rotation">`rotation`</a>

A vector of 3 angles in radians. This parameter governs the rotation of the PCBA around its anchor point in the front. You would not normally need this for the PCBA.

### Section <a id="mcu-support">`support`</a>

The support structure that holds the MCU PCBA in place.

#### Parameter <a id="mcu-support-style">`style`</a>

The style of the support. Available styles are:

- `lock`: A separate physical object that is bolted in place over the MCU. This style is appropriate only with a rear housing, and then only when the PCB aligns with a long wall of that housing. It has the advantage that it can hug the connector on the PCB tightly, thus preventing a fragile surface-mounted connector from breaking off.
- `stop`: A gripper that holds the PCBA in place at its rear end. This gripper, in turn, is held up by key mount webbing and is thus integral to the keyboard, not printed separately like the lock. This style does not require rear housing.

#### Parameter <a id="mcu-support-preview">`preview`</a>

If `true`, render a visualization of the support in place. This applies only to those parts of the support that are not part of the case model.

#### Parameter <a id="mcu-support-height-factor">`height-factor`</a>

A multiplier for the width of the PCB, producing the height of the support actually touching the PCB.

#### Parameter <a id="mcu-support-lateral-spacing">`lateral-spacing`</a>

A lateral 1D offset in mm. With rear housing, this creates space between the rear housing itself and the back of the PCB’s through-holes, so it should be roughly matched to the length of wire overshoot. Without rear housing, it isn’t so useful but it does work analogously.

#### Section <a id="mcu-support-lock">`lock`</a>

Parameters relevant only with a `lock`-style support.

##### Section <a id="mcu-support-lock-fastener">`fastener`</a>

A threaded bolt connects the lock to the case.

###### Parameter <a id="mcu-support-lock-fastener-style">`style`</a>

A style of bolt head (cap) supported by `scad-tarmi`.

###### Parameter <a id="mcu-support-lock-fastener-diameter">`diameter`</a>

The ISO metric diameter of the fastener.

##### Section <a id="mcu-support-lock-socket">`socket`</a>

A housing around the USB connector on the MCU PCBA.

###### Parameter <a id="mcu-support-lock-socket-thickness">`thickness`</a>

The wall thickness of the socket.

##### Section <a id="mcu-support-lock-bolt">`bolt`</a>

The part of a `lock`-style support that does not print with the keyboard case. This bolt, named by analogy with a lock, is not to be confused with the threaded fastener (also a bolt) holding it in place.

###### Parameter <a id="mcu-support-lock-bolt-clearance">`clearance`</a>

The distance of the bolt from the populated side of the PCB. This distance should be slightly greater than the height of the tallest component on the PCB.

###### Parameter <a id="mcu-support-lock-bolt-overshoot">`overshoot`</a>

The distance across which the bolt will touch the PCB at the mount end. Take care that this distance is free of components on the PCB.

###### Parameter <a id="mcu-support-lock-bolt-mount-length">`mount-length`</a>

The length of the base containing a threaded channel used to secure the bolt over the MCU. This is in addition to `overshoot` and goes in the opposite direction, away from the PCB.

###### Parameter <a id="mcu-support-lock-bolt-mount-thickness">`mount-thickness`</a>

The thickness of the mount. This should have some rough correspondence to the threaded portion of your fastener, which should not have a shank.

#### Section <a id="mcu-support-stop">`stop`</a>

Parameters relevant only with a `stop`-style support.

##### Parameter <a id="mcu-support-stop-anchor">`anchor`</a>

The name of a key where a stop will start to attach itself.

##### Parameter <a id="mcu-support-stop-direction">`direction`</a>

A direction in the matrix from the named key. The stop will attach to a hull of four neighbouring key mount corners in this direction.

##### Section <a id="mcu-support-stop-gripper">`gripper`</a>

The shape of the part that grips the PCB.

###### Parameter <a id="mcu-support-stop-gripper-notch-depth">`notch-depth`</a>

The horizontal depth of the notch in the gripper that holds the PCB. The larger this number, the more flexible the case has to be to allow assembly.

Note that while this is similar in effect to `lock`-style `overshoot`, it is a separate parameter because of the flexion limit.

###### Parameter <a id="mcu-support-stop-gripper-total-depth">`total-depth`</a>

The horizontal depth of the gripper as a whole in line with the PCB.

###### Parameter <a id="mcu-support-stop-gripper-grip-width">`grip-width`</a>

The width of a protrusion on each side of the notch.

## Section <a id="connection">`connection`</a>

There must be a signalling connection between the two halves of a split keyboard.

### Parameter <a id="connection-include">`include`</a>

If `true`, inclue a “metasocket”, i.e. physical support for a socket where you plug in a cable that will, in turn, provide the signalling connection between the two halves.

### Parameter <a id="connection-socket-size">`socket-size`</a>

The size in mm of a hole in the case, for the female to fit into. For example, the female might be a type 616E socket for a (male) 4P4C “RJ9” plug, in which case the metasocket has to fit around the entire 616E.

This parameter assumes a cuboid socket. For a socket of a different shape, get as close as possible, then make your own adapter and/or widen the metasocket with a soldering iron or similar tools.

### Parameter <a id="connection-socket-thickness">`socket-thickness`</a>

The thickness in mm of the roof, walls and floor of the metasocket, i.e. around the hole in the case.

### Section <a id="connection-position">`position`</a>

Where to place the socket. Equivalent to `mcu` → `position`.

#### Parameter <a id="connection-position-prefer-rear-housing">`prefer-rear-housing`</a>



#### Parameter <a id="connection-position-anchor">`anchor`</a>



#### Parameter <a id="connection-position-corner">`corner`</a>



#### Parameter <a id="connection-position-raise">`raise`</a>

If `true`, and the socket is being placed in relation to the rear housing, put it directly under the ceiling, instead of directly over the floor.

#### Parameter <a id="connection-position-offset">`offset`</a>



#### Parameter <a id="connection-position-rotation">`rotation`</a>



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

### Section <a id="wrist-rest-position">`position`</a>

The wrist rest is positioned in relation to a named feature.

#### Parameter <a id="wrist-rest-position-anchor">`anchor`</a>

The name of a feature where the wrist rest will attach. The vertical component of its position will be ignored.

#### Parameter <a id="wrist-rest-position-corner">`corner`</a>

A corner of the feature named in `anchor`.

#### Parameter <a id="wrist-rest-position-offset">`offset`</a>

An offset in mm from the feature named in `anchor`.

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

The size of the mask in mm. By default, `[1000, 1000, 1000]`.

### Parameter <a id="mask-center">`center`</a>

The position of the center point of the mask. By default, `[0, 0, 500]`, which is supposed to mask out everything below ground level. If you include bottom plates, their thickness will automatically affect the placement of the mask beyond what you specify here.

⸻

This document was generated from the application CLI.
