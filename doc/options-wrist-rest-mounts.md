<!--This document was generated and is intended for rendering to HTML on GitHub. Edit the source files, not this file.-->

# Options for connecting a wrist rest

Each heading in this document represents a recognized configuration key in YAML files for a DMOTE variant.

This specific document describes options for each “mount”, a pair of cuboid blocks used to anchor threaded fasteners for the `threaded` style of wrist rest, a parameter documented [here](options-main.md).

## Table of contents
- Section <a href="#user-content-fasteners">`fasteners`</a>
    - Parameter <a href="#user-content-fasteners-amount">`amount`</a>
    - Parameter <a href="#user-content-fasteners-diameter">`diameter`</a>
    - Parameter <a href="#user-content-fasteners-length">`length`</a>
    - Section <a href="#user-content-fasteners-height">`height`</a>
        - Parameter <a href="#user-content-fasteners-height-first">`first`</a>
        - Parameter <a href="#user-content-fasteners-height-increment">`increment`</a>
- Parameter <a href="#user-content-anchoring">`anchoring`</a>
- Parameter <a href="#user-content-angle">`angle`</a>
- Section <a href="#user-content-blocks">`blocks`</a>
    - Parameter <a href="#user-content-blocks-distance">`distance`</a>
    - Parameter <a href="#user-content-blocks-width">`width`</a>
    - Section <a href="#user-content-blocks-case-side">`case-side`</a>
        - Section <a href="#user-content-blocks-case-side-position">`position`</a>
            - Parameter <a href="#user-content-blocks-case-side-position-anchor">`anchor`</a>
            - Parameter <a href="#user-content-blocks-case-side-position-corner">`corner`</a>
            - Parameter <a href="#user-content-blocks-case-side-position-offset">`offset`</a>
        - Parameter <a href="#user-content-blocks-case-side-depth">`depth`</a>
        - Section <a href="#user-content-blocks-case-side-nuts">`nuts`</a>
            - Section <a href="#user-content-blocks-case-side-nuts-bosses">`bosses`</a>
                - Parameter <a href="#user-content-blocks-case-side-nuts-bosses-include">`include`</a>
    - Section <a href="#user-content-blocks-plinth-side">`plinth-side`</a>
        - Section <a href="#user-content-blocks-plinth-side-position">`position`</a>
            - Parameter <a href="#user-content-blocks-plinth-side-position-anchor">`anchor`</a>
            - Parameter <a href="#user-content-blocks-plinth-side-position-offset">`offset`</a>
        - Parameter <a href="#user-content-blocks-plinth-side-depth">`depth`</a>
        - Parameter <a href="#user-content-blocks-plinth-side-pocket-height">`pocket-height`</a>
    - Parameter <a href="#user-content-blocks-aliases">`aliases`</a>

## Section <a id="fasteners">`fasteners`</a>

Threaded fasteners in the mount.

### Parameter <a id="fasteners-amount">`amount`</a>

The number of vertically stacked screws in the mount. 1 by default.

### Parameter <a id="fasteners-diameter">`diameter`</a>

The ISO metric diameter of each fastener.

### Parameter <a id="fasteners-length">`length`</a>

The length in mm of each fastener.

### Section <a id="fasteners-height">`height`</a>

The vertical level of the fasteners.

#### Parameter <a id="fasteners-height-first">`first`</a>

The distance in mm from the bottom of the first fastener down to the ground level of the model.

#### Parameter <a id="fasteners-height-increment">`increment`</a>

The vertical distance in mm from the center of each fastener to the center of the next.

## Parameter <a id="anchoring">`anchoring`</a>

One of:

- `case-side`: The `angle` parameter in this section determines the angle of the blocks and threaded fasteners in the mount. In effect, the plinth-side block is placed by `angle` and `distance`, while its own explicit `position` section of parameters is ignored.
- `mutual`: The `angle` and `distance` parameters are ignored. Each block is anchored to a separate and independent feature. The angle and distance between these two features determines the angle of the fasteners and the distance between the blocks.

## Parameter <a id="angle">`angle`</a>

The angle in radians of the mount, on the xy plane, counter-clockwise from the y axis. This parameter is only used with `case-side` anchoring.

## Section <a id="blocks">`blocks`</a>

Blocks for anchoring threaded fasteners.

### Parameter <a id="blocks-distance">`distance`</a>

The distance in mm between the two posts in a mount. This parameter is only used with `case-side` anchoring.

### Parameter <a id="blocks-width">`width`</a>

The width in mm of the face or front bezel on each block that will anchor a fastener.

### Section <a id="blocks-case-side">`case-side`</a>

A block on the side of the keyboard case is mandatory.

#### Section <a id="blocks-case-side-position">`position`</a>

Where to place the block.

##### Parameter <a id="blocks-case-side-position-anchor">`anchor`</a>

An alias referring to a feature that anchors the block.

##### Parameter <a id="blocks-case-side-position-corner">`corner`</a>

A corner of the anchor. By default: `SSE` for south-by-southeast.

##### Parameter <a id="blocks-case-side-position-offset">`offset`</a>

A two-dimensional vector offset in mm from the anchor to the block.

#### Parameter <a id="blocks-case-side-depth">`depth`</a>

The thickness of the block in mm along the axis of the fastener(s).

#### Section <a id="blocks-case-side-nuts">`nuts`</a>

Extra features for threaded nuts on the case side.

##### Section <a id="blocks-case-side-nuts-bosses">`bosses`</a>

Nut bosses on the rear (interior) of the mount. You may want this if the distance between case and plinth is big enough for a nut. If that distance is too small, bosses can be counterproductive.

###### Parameter <a id="blocks-case-side-nuts-bosses-include">`include`</a>

If `true`, include bosses.

### Section <a id="blocks-plinth-side">`plinth-side`</a>

A block on the side of the wrist rest.

#### Section <a id="blocks-plinth-side-position">`position`</a>

Where to place the block. This entire section is ignored in the `case-side` style of anchoring.

##### Parameter <a id="blocks-plinth-side-position-anchor">`anchor`</a>

An alias referring to a feature that anchors the block. Whereas the case-side mount is typically anchored to a key, the plinth-side mount is typically anchored to a named point on the plinth.

##### Parameter <a id="blocks-plinth-side-position-offset">`offset`</a>

An offset in mm from the named feature to the block.

#### Parameter <a id="blocks-plinth-side-depth">`depth`</a>

The thickness of the mount in mm along the axis of the fastener(s). This is typically larger than the case-side depth to allow adjustment.

#### Parameter <a id="blocks-plinth-side-pocket-height">`pocket-height`</a>

The height of the nut pocket inside the mounting plate, in mm.

With a large positive value, this will provide a chute for the nut(s) to go in from the top of the plinth, which allows you to hide the hole beneath the pad. With a large negative value, the pocket will instead open from the bottom, which is convenient if `depth` is small. With a small value or the default value of zero, it will be necessary to pause printing in order to insert the nut(s); this last option is therefore recommended for advanced users only.

### Parameter <a id="blocks-aliases">`aliases`</a>

A map of short names to specific blocks, i.e. `case-side` or `plinth-side`. Such aliases are for use elsewhere in the configuration.

⸻

This document was generated from the application CLI.
