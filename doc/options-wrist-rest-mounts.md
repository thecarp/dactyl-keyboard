<!--This document was generated and is intended for rendering to HTML on GitHub. Edit the source files, not this file.-->

# Options for connecting a wrist rest

Each heading in this document represents a recognized configuration key in [YAML files for the DMOTE application](configuration.md).

This specific document describes options for each “mount”, a pair of cuboid blocks used to anchor threaded fasteners for the `threaded` style of wrist rest, a parameter documented [here](options-main.md). Each block in the pair is part of a different body: One is part of the wrist rest, and the other a “partner” body.

## Table of contents
- Section <a href="#user-content-fasteners">`fasteners`</a>
    - Parameter <a href="#user-content-fasteners-amount">`amount`</a>
    - Parameter <a href="#user-content-fasteners-bolt-properties">`bolt-properties`</a>
    - Parameter <a href="#user-content-fasteners-heights">`heights`</a>
- Parameter <a href="#user-content-authority">`authority`</a>
- Parameter <a href="#user-content-angle">`angle`</a>
- Section <a href="#user-content-blocks">`blocks`</a>
    - Parameter <a href="#user-content-blocks-distance">`distance`</a>
    - Parameter <a href="#user-content-blocks-width">`width`</a>
    - Section <a href="#user-content-blocks-partner-side">`partner-side`</a>
        - Parameter <a href="#user-content-blocks-partner-side-body">`body`</a>
        - Section <a href="#user-content-blocks-partner-side-anchoring">`anchoring`</a>
        - Parameter <a href="#user-content-blocks-partner-side-depth">`depth`</a>
    - Section <a href="#user-content-blocks-wrist-side">`wrist-side`</a>
        - Section <a href="#user-content-blocks-wrist-side-anchoring">`anchoring`</a>
        - Parameter <a href="#user-content-blocks-wrist-side-depth">`depth`</a>
- Section <a href="#user-content-aliases">`aliases`</a>
    - Parameter <a href="#user-content-aliases-blocks">`blocks`</a>
    - Parameter <a href="#user-content-aliases-nuts">`nuts`</a>

## Section <a id="fasteners">`fasteners`</a>

Threaded fasteners in the mount.

### Parameter <a id="fasteners-amount">`amount`</a>

The number of vertically stacked screws in the mount. 1 by default.

### Parameter <a id="fasteners-bolt-properties">`bolt-properties`</a>

This parameter describes the properties of a screw or bolt. It takes a mapping appropriate for the `bolt` function in the [`scad-klupe.iso`](https://github.com/veikman/scad-klupe) library.

The following describes only a subset of what you can include here:

* `m-diameter`: The ISO metric diameter of a bolt, e.g. `6` for M6.
* `head-type`: A keyword describing the head of the bolt, such as `hex` or `countersunk`.
* `total-length`: The length of the threaded part of the bolt, in mm.

The DMOTE application provides some parameters that differ from the default values in `scad-klupe` itself, in the following ways:

* `negative`: The DMOTE application automatically sets this to `true` for bolt models that represent negative space.
* `compensator`: The application automatically injects a DFM function.
* `include-threading`: This is `true` by default in `scad-klupe` and `false` by default in the DMOTE application. The main reason for this difference is the general pattern of defaulting to false in the application for predictability. Secondary reasons are rendering performance, the option of tapping threads after printing, and the uselessness of threads in combination with heat-set inserts.


### Parameter <a id="fasteners-heights">`heights`</a>

A list of heights in mm, above the ground level. Each number describes the level of a set of fasteners: The centre of one threaded rod and any nuts etc. attaching it.

## Parameter <a id="authority">`authority`</a>

One of:

- `partner-side`: The `angle` parameter in this section determines the angle of the blocks and threaded fasteners in the mount. In effect, the wrist-side block is placed by `angle` and `distance`, while its own explicit `anchoring` section of parameters is ignored.
- `mutual`: The `angle` and `distance` parameters are ignored. Each block is anchored independently. The angle and distance between the blocks determines the angle of the fasteners.

## Parameter <a id="angle">`angle`</a>

The angle in radians of the mount, on the xy plane, counter-clockwise from the y axis. This parameter is only used with `partner-side` anchoring.

## Section <a id="blocks">`blocks`</a>

Blocks for anchoring threaded fasteners.

### Parameter <a id="blocks-distance">`distance`</a>

The distance in mm between the two posts in a mount. This parameter is only used with `partner-side` authority.

### Parameter <a id="blocks-width">`width`</a>

The width in mm of each block that will hold a fastener.

### Section <a id="blocks-partner-side">`partner-side`</a>

A block on the side of the partner body is mandatory.

#### Parameter <a id="blocks-partner-side-body">`body`</a>

A code identifying the [body](configuration.md) that houses the block.

#### Section <a id="blocks-partner-side-anchoring">`anchoring`</a>

Where on the ground to place the block. The concept of anchoring is explained [here](options-anchoring.md), along with the parameters available in this section.

#### Parameter <a id="blocks-partner-side-depth">`depth`</a>

The thickness of the block in mm along the axis of the fastener(s).

### Section <a id="blocks-wrist-side">`wrist-side`</a>

A block on the side of the wrist rest.

#### Section <a id="blocks-wrist-side-anchoring">`anchoring`</a>

Where on the ground to place the block, as for the partner side. This entire section is ignored with `partner-side` authority.

#### Parameter <a id="blocks-wrist-side-depth">`depth`</a>

The thickness of the mount in mm along the axis of the fastener(s).

## Section <a id="aliases">`aliases`</a>

Short names for different parts of the mount, for use elsewhere in the application.

### Parameter <a id="aliases-blocks">`blocks`</a>

A map of short names to specific blocks as such, i.e. `partner-side` or `wrist-side`.

### Parameter <a id="aliases-nuts">`nuts`</a>

A map of short names to nuts. Nuts are identified by tuples (lists of two items) where each tuple names a block, i.e. `partner-side` or `wrist-side`, and indexes a fastener in the `heights` list above. Indexing starts from zero.

This parameter is used to name nuts to go on each end of each threaded rod. The intended use for this is with negative-space `tweaks`, where you target each nut by its name and supply `cut: true`. Some recipes:

- To get a cavity for a nut wholly inside a block, just target the nut for a tweak without an offset or other special arguments. It will be necessary to pause printing in order to insert the nut in such a cavity.
- To get a pocket for sliding in a nut from the top of the mount, hull a nut in its place with the same nut, offset higher on the z axis. Design the pad of the wrist rest to cover the pocket.
- To get a similar pocket that opens from the bottom, target a nut in place with `at-ground`. Use a bottom plate to hide the pocket.
- To get a nut boss instead of a pocket, offset a nut on the y axis. This is also useful with hex-head bolts.

⸻

This document was generated from the application CLI.
