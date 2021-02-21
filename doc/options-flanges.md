<!--This document was generated and is intended for rendering to HTML on GitHub. Edit the source files, not this file.-->

# Flange configuration options

Each heading in this document represents a recognized configuration key in [YAML files for the DMOTE application](configuration.md).

This specific document describes options for any individual flange. One set of such options will exist for each entry in `flanges`, a section whose place in the larger hierarchy can be seen [here](options-main.md).

Example uses for flanges are:

- Connecting a custom body to its parent body.
- Connecting a bottom plate to its parent body.
- Providing hardpoints for attaching other stuff, inside or outside a keyboard.

Flanges most often do this mainly by reserving negative space for threaded fasteners. The feature is named after pipe flanges joined by such fasteners.

## Table of contents
- Parameter <a href="#user-content-include">`include`</a>
- Parameter <a href="#user-content-reflect">`reflect`</a>
- Parameter <a href="#user-content-body">`body`</a>
- Parameter <a href="#user-content-bottom">`bottom`</a>
- Section <a href="#user-content-bolts">`bolts`</a>
    - Parameter <a href="#user-content-bolts-include">`include`</a>
    - Parameter <a href="#user-content-bolts-bolt-properties">`bolt-properties`</a>
- Section <a href="#user-content-inserts">`inserts`</a>
    - Parameter <a href="#user-content-inserts-include">`include`</a>
    - Parameter <a href="#user-content-inserts-height">`height`</a>
    - Parameter <a href="#user-content-inserts-length">`length`</a>
    - Section <a href="#user-content-inserts-diameter">`diameter`</a>
        - Parameter <a href="#user-content-inserts-diameter-top">`top`</a>
        - Parameter <a href="#user-content-inserts-diameter-bottom">`bottom`</a>
- Section <a href="#user-content-bosses">`bosses`</a>
    - Parameter <a href="#user-content-bosses-include">`include`</a>
    - Parameter <a href="#user-content-bosses-segments">`segments`</a>
- Parameter <a href="#user-content-positions">`positions`</a>

## Parameter <a id="include">`include`</a>

If `true`, include those parts of the flange that are in turn marked with `include` in their own subsections.

## Parameter <a id="reflect">`reflect`</a>

If `true`, mirror every part of the flange around x = 0. For example, you might use this to get identical fasteners on both sides of a central
    housing.

## Parameter <a id="body">`body`</a>

A code identifying the [body](configuration.md) to which the flange belongs.

It is not necessary for an entire flange to belong to a single body. With the default value (`auto`) for this parameter, the body membership of each part of the flange is determined separately, based on its individual anchoring.

## Parameter <a id="bottom">`bottom`</a>

If `true`, treat this flange as connecting its parent body to that body’s bottom plate. This has the following side effects:

- The flange will be disabled if its parent body is configured to have no bottom plate, even if the flange itself has `include: true`.
- Each part of the flange is turned to face straight up, because all the bolts in an ordinary bottom flange enter straight through the bottom plate from below.
- Anchoring becomes two-dimensional, so that, in the absence of any explicit offset you specify, each part of the flange sits at floor level beneath its `anchor`.
- Bottom flanges, when rendered as part of bottom plates but not when rendered as part of the case, are affected by `fastener-plate-offset`, a DFM parameter described [here](options-main.md).
- For performance reasons, the application will select whether to include each flange based on the `bottom` attribute so that, for example, top flanges will not affect a bottom plate even if their position intersects the plate.

## Section <a id="bolts">`bolts`</a>

Flanges typically connect two parts of a keyboard by means of threaded fasteners.

### Parameter <a id="bolts-include">`include`</a>

If `true`, reserve negative space for fasteners.

### Parameter <a id="bolts-bolt-properties">`bolt-properties`</a>

This parameter describes the properties of a screw or bolt. It takes a mapping appropriate for the `bolt` function in the [`scad-klupe.iso`](https://github.com/veikman/scad-klupe) library.

The following describes only a subset of what you can include here:

* `m-diameter`: The ISO metric diameter of a bolt, e.g. `6` for M6.
* `head-type`: A keyword describing the head of the bolt, such as `hex` or `countersunk`.
* `total-length`: The length of the threaded part of the bolt, in mm.

The DMOTE application provides some parameters that differ from the default values in `scad-klupe` itself, in the following ways:

* `negative`: The DMOTE application automatically sets this to `true` for bolt models that represent negative space.
* `compensator`: The application automatically injects a DFM function.
* `include-threading`: This is `true` by default in `scad-klupe` and `false` by default in the DMOTE application. The main reason for this difference is the general pattern of defaulting to false in the application for predictability. Secondary reasons are rendering performance, the option of tapping threads after printing, and the uselessness of threads in combination with heat-set inserts.


## Section <a id="inserts">`inserts`</a>

If you are not printing or tapping threads, consider cylindrical
    heat-set inserts to receive bolts, as used in the original
    Dactyl-ManuForm.

### Parameter <a id="inserts-include">`include`</a>

If `true`, reserve negative space for inserts.This is a subsection with two parameters: `top` and `bottom`.

### Parameter <a id="inserts-height">`height`</a>

The distance in mm between the flange’s position and the bottom of the insert.

### Parameter <a id="inserts-length">`length`</a>

The length in mm of each insert.

### Section <a id="inserts-diameter">`diameter`</a>

The diameter in mm of each insert. 

#### Parameter <a id="inserts-diameter-top">`top`</a>

Top diameter. Consider making this slightly smaller than the real diameter to ensure there`s enough material to secure the insert.

#### Parameter <a id="inserts-diameter-bottom">`bottom`</a>

Bottom diameter. This is the diameter of the insert at the side closest to the flange’s stated position.

## Section <a id="bosses">`bosses`</a>

Both bolts and heat-set inserts for bolts require positive space to hold on to. Where there is enough material around them, extra space can be created using `bosses`.

### Parameter <a id="bosses-include">`include`</a>

If `true`, automatically add bosses in the form of loft sequences encompassing all of the defined segments of each boss.

If `false` (default), you can still get bosses, but you will need to give each position of the flange an alias and then target it with `tweaks`.

### Parameter <a id="bosses-segments">`segments`</a>

A map of numeric segment IDs to segment properties. Numbering starts at zero and in YAML, segment IDs that are map keys must be enclosed in quotes.

The recognized properties of a segment are:
- `style`: The shape of the segment. One of `cylinder` (default), `cube` or `sphere`.
- `size`: The measurements of the segment, in mm.
- `intrinsic-offset`: An xyz-offset in mm from the previous segment or, in the case of segment zero, from the flange position itself.


## Parameter <a id="positions">`positions`</a>

A list of all the places on the keyboard where the parts of the flange will appear.

A position is a map recognizing the following properties:

- `boss-alias`: A name for the position’s boss, for use in `tweaks`. In the case of reflected positions, this alias refers to the original, not the mirror image.
- `reflect`: If `true`, mirror this part of the flange around x = 0.
- `anchoring`: Room for standard anchoring parameters.

⸻

This document was generated from the application CLI.
