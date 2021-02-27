<!--This document was generated and is intended for rendering to HTML on GitHub. Edit the source files, not this file.-->

# Central housing configuration options

When you mirror the [main body](options-main.md) of the keyboard to make two halves, you get a space in between. A central housing can occupy that space, providing a rigid mechanical connection and room for an MCU.

When present, a central housing naturally determines the position of each other part of the keyboard: Key clusters on each side of the main body should be anchored to points on the central housing.

## Table of contents
- Parameter <a href="#user-content-include">`include`</a>
- Parameter <a href="#user-content-preview">`preview`</a>
- Section <a href="#user-content-shape">`shape`</a>
    - Parameter <a href="#user-content-shape-width">`width`</a>
    - Parameter <a href="#user-content-shape-thickness">`thickness`</a>
    - Parameter <a href="#user-content-shape-interface">`interface`</a>
- Section <a href="#user-content-adapter">`adapter`</a>
    - Parameter <a href="#user-content-adapter-include">`include`</a>
    - Parameter <a href="#user-content-adapter-width">`width`</a>
    - Section <a href="#user-content-adapter-lip">`lip`</a>
        - Parameter <a href="#user-content-adapter-lip-include">`include`</a>
        - Parameter <a href="#user-content-adapter-lip-thickness">`thickness`</a>
        - Section <a href="#user-content-adapter-lip-width">`width`</a>
            - Parameter <a href="#user-content-adapter-lip-width-outer">`outer`</a>
            - Parameter <a href="#user-content-adapter-lip-width-inner">`inner`</a>
            - Parameter <a href="#user-content-adapter-lip-width-taper">`taper`</a>
    - Section <a href="#user-content-adapter-fasteners">`fasteners`</a>
        - Parameter <a href="#user-content-adapter-fasteners-bolt-properties">`bolt-properties`</a>
        - Parameter <a href="#user-content-adapter-fasteners-positions">`positions`</a>
    - Section <a href="#user-content-adapter-receivers">`receivers`</a>
        - Section <a href="#user-content-adapter-receivers-thickness">`thickness`</a>
            - Parameter <a href="#user-content-adapter-receivers-thickness-rim">`rim`</a>
            - Section <a href="#user-content-adapter-receivers-thickness-bridge">`bridge`</a>
                - Parameter <a href="#user-content-adapter-receivers-thickness-bridge-radial">`radial`</a>
                - Parameter <a href="#user-content-adapter-receivers-thickness-bridge-tangential">`tangential`</a>
        - Section <a href="#user-content-adapter-receivers-width">`width`</a>
            - Parameter <a href="#user-content-adapter-receivers-width-inner">`inner`</a>
            - Parameter <a href="#user-content-adapter-receivers-width-taper">`taper`</a>

## Parameter <a id="include">`include`</a>

If this and `main-body` → `reflect` are both true, add a central housing.

## Parameter <a id="preview">`preview`</a>

If `true`, include the central housing when rendering each half of the main body of the keyboard.

## Section <a id="shape">`shape`</a>

The shape of the central housing determines, in part, how it connects to the rest of the keyboard, including the general shape of an optional adapter. The adapter is also influenced, in part, by settings devoted to it, in the next section.

Assuming that adapters are included, you can think of a keyboard with a central housing as a row of buildings: Terraced housing, also known as row houses. In this metaphor, the series of vertices placed with the `interface` parameter in this section, and the `width`, determine the shape of each house. The adapters are the gables on either side of a particular house, up against which two others house are built: The two mirrored halves of the main body.

### Parameter <a id="shape-width">`width`</a>

The approximate extent of the housing itself, on the x axis, in mm.

### Parameter <a id="shape-thickness">`thickness`</a>

The thickness of the housing and its adapter (if any), in mm.

### Parameter <a id="shape-interface">`interface`</a>

The `interface` setting is essentially a list of points: Coordinates in space. Each of these points can influence the shape of the central housing itself, its adapter, and/or the shape of its bottom plate.

There are no `body` parameters here. Instead, the question of which bodies will include each item in the list is answered by the properties of each item in the list, including the position of each point. Specifically, the `base` z-axis `offset` coordinate (see below) controls the default values of the following two optional properties, if you omit them:

* `at-ground`: If `true`, or if omitted and the `base` z coordinate is not positive, the item will shape the bottom plate for the central housing, if any.
* `above-ground`: If `true`, or if the `base` z coordinate is not negative, the item will shape the central housing itself as a body. If an adapter is included, the item will shape that too.

Those items which are “above ground” determine the shape of each outer edge of the housing, at the interface between the housing itself and the rest of the case. They do this as an ordered list of vertices, each defined primarily by an offset in a little section under the `base` key. The following keys are allowed in the value of `base`:

* `offset` (required): A three-dimensional position in mm. This offset is from a point that is already displaced from the origin of the coordinate system by exactly one half of the `width` set above. The last coordinate (z coordinate) in this value determines the default settings for both `at-ground` and `above-ground`, above.
* `left-hand-alias` (optional): A symbolic name for this point on the interface. Specifically, `left-hand-alias` will identify the point on the left-hand edge of the housing in its standard orientation.
* `right-hand-alias` (optional): A symbolic name for the point on the right-hand-side edge. Because the right-hand side of the main body of the keyboard is the source of the left-hand side (with `reflect`, hence with a central housing), a `right-hand-alias` has more general utility.

In addition to all properties named thus far, each item in the `interface` list may also include an `adapter` subsection. This section, and everything in it, is optional and relates to the central housing adapter feature, described elsewhere in this document. Briefly, the adapter fits precisely onto each interface of the housing. Here’s what the `adapter` subsection can contain:
* `alias`: A symbolic name for this point on the side of the adapter facing away from the central housing. Notice that the side facing toward the central housing is coterminous with the interface itself, so the corresponding point on it is named by `right-hand-alias`.
* `segments`: A map of integer segment IDs to maps with three-dimensional offsets in mm. In this map, segment 0 refers to the outer shell of the adapter, and any offset provided for it is added to both the width of the adapter and the base position when determining the exact shape of the adapter. Segment 1 refers to the inside of the adapter; any offset provided for it is relative to the sum of the final position of segment 0 and the central housing’s thickness, which acts as a radial inset.

`segments` resembles an entry in the `by-key` section of parameters. There are two important differences: Segment IDs other than 0 and 1 cannot be targeted, and because the offset for segment 1 is applied only after housing thickness, it does not provide total and direct control. Notice also that adapter segment offsets refer to global vector space.

The following example covers only one vertex on the housing and two corresponding vertices on its adapter, and is therefore not a complete interface. That said, it does show all of the properties one item in the `interface` list can have, with realistic values.

```
  interface:
  - at-ground: true
    above-ground: true
    base:
      offset: [0, 20, 40]
      left-hand-alias: housing-side-1L
      right-hand-alias: housing-side-1R
    adapter:
      alias: adapter-side-1
      segments:
        "0":
          intrinsinc-offset: [10, 0, 0]
        "1":
          intrinsinc-offset: [-1, 0, 0]
```

In this example, the vertex named `adapter-side-1` will be placed 10 mm plus the overall width of the adapter away from `housing-side-1R`, with the adapter covering the intervening distance, so that the shell of the adapter touches both vertices, and the housing only touches one. Given that the `base` z coordinate is zero, both `at-ground` and `above-ground` have their default values and are therefore redundant in the example.

The example’s segment-1 offset puts a gradient on the outer face of the adapter, the side facing away from the central housing. Such a gradient can be useful for joining the adapter to key walls with `tweaks`.

Aliases for vertices on the interface itself, as opposed to the adapter, are useful mainly when you anchor features like an MCU holder to the central housing. Doing so, you need to be aware that the central housing has both symmetric and asymmetric properties. Its basic shape, including everything you can determine with `interface`, will be bilaterally symmetric. Adapters and their fasteners, and bottom plates, are also symmetric, except for threaded holes. By contrast, central-housing-specific `tweaks` and MCU holders will only appear on the specific side of the housing that you indicate, breaking symmetry.

Here’s an example of a valid, minimal, triangular profile, like a little ridge tent:

```
  interface:
  - base:
      offset: [0, -10, 0]
  - base:
      offset: [4, 0, 10]
  - base:
      offset: [0, 10, 0]
```
In this example, the high point in the middle is offset on the x axis, giving the edge of the central housing a slant as seen from infinite y. Notice also that the lowest z coordinate is 0, which places this central housing on the floor, inside the `mask`. This is not required. A lower z coordinate will cause the `mask` to open the bottom of the central housing, which is usually more practical for running wires through the keyboard. Notice that if you do use a lower z coordinate and you still want the two lower points to be included in the central housing as a body, you will need to set `above-ground` to `true`, as an explicit addition to the two items at the extremes.

The DMOTE application uses the `interface` to construct OpenSCAD polyhedrons. OpenSCAD and CGAL have many requirements upon polyhedrons and a carelessly constructed interface will violate them, resulting in a central housing that cannot be rendered or printed. As a rule of thumb, define your interface moving **clockwise** from the point of view of positive infinite x, like the tent example above. Be especially careful with `adapter` segment offsets on the y and z axes, and try to keep the housing itself more than twice as broad as its wall thickness.

It may not be obvious why `at-ground` and `above-ground` are mutually independent of one another and of the `base` z coordinate that gives them their default values. The main reason for this design is to allow points that are “ethereal”, appearing in neither body. Ethereal points are similar to `secondaries`, except that unlike `secondaries`, they are influenced by width settings and not tied to other points on the interface. They are intended as anchors for key clusters and tweaks, where their responsiveness to width alone comes in handy as you work out the precise shape of the housing.

## Section <a id="adapter">`adapter`</a>

The central housing can connect to key clusters through an adapter: A part that is shaped like the central housing and extends the rest of the case to meet the central housing at an interface.

Using `tweaks`, points on the adapter should be connected to key cluster walls to close the case around the adapter but leave the adapter itself open.

### Parameter <a id="adapter-include">`include`</a>

If this is `true`, add an adapter for the central housing.

### Parameter <a id="adapter-width">`width`</a>

The approximate width of the adapter on each side of the central housing, along its axis (the x axis). Individual points on the adapter can be offset from this width using the `interface` list’s `segments` maps.

### Section <a id="adapter-lip">`lip`</a>

To stabilize the connection between the central housing and the adapter, the interface between them can include an interior lip.

#### Parameter <a id="adapter-lip-include">`include`</a>

If `true`, attach a lip to the central housing.

#### Parameter <a id="adapter-lip-thickness">`thickness`</a>

The thickness of the lip at each point along it, in mm.

#### Section <a id="adapter-lip-width">`width`</a>

The lip extends in both directions from the edge of the central housing: Both into the adapter (the “outer” part) and into the housing itself (the “inner” part), where it grows out of the inner wall with an optional taper. The total width of the lip is the sum of all these sections.

##### Parameter <a id="adapter-lip-width-outer">`outer`</a>

The distance the lip protrudes outside the central housing and thence into the adapter, in mm.

##### Parameter <a id="adapter-lip-width-inner">`inner`</a>

The width of the lip inside the central housing, before it starts to taper, in mm.

##### Parameter <a id="adapter-lip-width-taper">`taper`</a>

The width of a taper from the inner portion of the lip to the inner wall of the central housing, in mm.

The default value, zero, produces a right-angled transition. The higher the value, the more gentle the transition becomes.

### Section <a id="adapter-fasteners">`fasteners`</a>

To connect the central housing and the adapter, threaded fasteners can be driven through the wall of either, into receivers extending from the other.

#### Parameter <a id="adapter-fasteners-bolt-properties">`bolt-properties`</a>

This parameter describes the properties of a screw or bolt. It takes a mapping appropriate for the `bolt` function in the [`scad-klupe.iso`](https://github.com/veikman/scad-klupe) library.

The following describes only a subset of what you can include here:

* `m-diameter`: The ISO metric diameter of a bolt, e.g. `6` for M6.
* `head-type`: A keyword describing the head of the bolt, such as `hex` or `countersunk`.
* `total-length`: The length of the threaded part of the bolt, in mm.

The DMOTE application provides some parameters that differ from the default values in `scad-klupe` itself, in the following ways:

* `negative`: The DMOTE application automatically sets this to `true` for bolt models that represent negative space.
* `compensator`: The application automatically injects a DFM function.
* `include-threading`: This is `true` by default in `scad-klupe` and `false` by default in the DMOTE application. The main reason for this difference is the general pattern of defaulting to false in the application for predictability. Secondary reasons are rendering performance, the option of tapping threads after printing, and the uselessness of threads in combination with heat-set inserts.


#### Parameter <a id="adapter-fasteners-positions">`positions`</a>

A list of places where threaded fasteners will go through the wall.

Each item in this list is a map with three mandatory keys:

* `starting-point`: The name of a vertex on the interface. This must be a `base` point, not a point on the far side of the adapter.
* `radial-offset`: A distance in mm from the starting point, along the interface.
* `axial-offset`: A distance in mm from the starting point, along the axis of the central housing, which is the x axis.

Both of the two offsets are numbers: Simple scalars. Each of them can be either positive or negative, but not zero. Zero is not allowed because these numbers have side effects:

* The radial offset follows the interface in the original order of its definition. A positive radial offset moves along the interface in the direction of that original order and a negative number moves the other way around, “against” the interface. A non-zero offset is needed here to identify that direction of travel itself, which in turn is used to identify the next vertex on the interface. Take care not to enter a number larger than the distance to the next vertex.
* The lateral offset determines which model the fastener will be a part of, as negative space. A positive lateral offset puts the hole through the wall of the adapter, and a negative offset puts the hole through the wall of the central housing. A non-zero offset is needed to pick a side, and it has to be large enough to secure the fastener on that side, so it won’t intersect the oppsite side.

The following example map will start from a vertex named `apex` and proceed from there, 5 mm forward toward the next point after `apex` and 4 mm to the side, where a hole for a fastener will be left in the wall of each end of the central housing.

```positions:
  - starting-point: apex
    radial-offset: 5
    axial-offset: -4
```
In addition to these mandatory properties, each fastener position can include a more advanced property: `direction-point`. This allows you to name an arbitrary anchor point anywhere on the keyboard, overriding the side effect of `radial-offset` in choosing a neighbouring point on the interface. This feature may help resolve problems with sloping adapters or other obstacles, but consider it experimental. More detailed overrides for placement may be introduced in a future version, if they are needed.

### Section <a id="adapter-receivers">`receivers`</a>

One receiver is created for each of the `fasteners`. Each of these has a threaded hole to keep the fastener in place. Like the adapter lip, receivers extend from the inside wall, but receivers are anchored across the interface from their respective fasteners: A positive `axial-offset`, above, extends a receiver from the central housing into the adapter.

#### Section <a id="adapter-receivers-thickness">`thickness`</a>

The thickness of material in various parts of each receiver.

##### Parameter <a id="adapter-receivers-thickness-rim">`rim`</a>

The maximum thickness of the loop of each receiver where it grabs the fastener, in mm.

##### Section <a id="adapter-receivers-thickness-bridge">`bridge`</a>

The part between the hole and the tapering end, where the receiver extends axially across the interface.

###### Parameter <a id="adapter-receivers-thickness-bridge-radial">`radial`</a>

The thickness of each receiver in the plane of the housing wall, in mm.

###### Parameter <a id="adapter-receivers-thickness-bridge-tangential">`tangential`</a>

The thickness of each receiver out from the housing wall, in mm. This is bounded by the depth of the screw hole.

#### Section <a id="adapter-receivers-width">`width`</a>

This section is analogous to lip `width`. The “outer” width of each receiver is a function of its fastener’s lateral offset and cannot be configured here.

##### Parameter <a id="adapter-receivers-width-inner">`inner`</a>

The width of the receiver at its base, before it starts to taper, in mm.

##### Parameter <a id="adapter-receivers-width-taper">`taper`</a>

The width of a taper, as with the lip.

⸻

This document was generated from the application CLI.
