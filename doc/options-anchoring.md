# Anchoring

As noted [here](configuration.md), this application uses “anchor” as a metaphor
for how the parts of a keyboard are placed in relation to one another.

## Available anchors

Every part of the keyboard exists in the global Cartesian coordinate system of
its OpenSCAD scene. You can position each part in relation to the
[origin](https://en.wikipedia.org/wiki/Origin_(mathematics)) of that system.
The origin has the coordinates `[0, 0, 0]` and a name: `origin`.

The origin needs a name because it can serve as an anchor. Wherever the
[parameter manual](options-main.md) refers to `anchoring`, you can name
`origin` as the `anchor` and then control the final position of the part you
are configuring with an offset from that anchor. In fact, `origin` is the
default anchor, so you only need to name it in overrides.

To keep things flexible and easy to maintain, features can be anchored to other
features, as long as that chain of anchors eventually resolves back to
`origin`.

In addition to `origin`, there are a couple of other named anchors built into
the application:

* `mcu-pcba`: The printed circuit assembly of the microcontroller unit.
* `rear-housing-exterior`: The outside of the rear housing, an optional feature
  of the main body.
* `rear-housing-interior`: The inside of the rear housing.

You can have many more, by defining names for the features you use as part of
your configuration. This is often done with an `alias` parameter, but can also
happen as a side effect of adding keys to maps in special sections of the
configuration.

## Parts of an anchor

Anchoring a feature determines its position in the coordinate system with
respect to its anchor. Along with the name of the anchor, you can specify a
couple of other things to target a specific piece of the anchor:

* A `side` code. This is a compass point identifying a part of the anchor’s
  periphery.
* A `segment` code. This is a numeric ID for a vertical or depth-like segment
  of the anchor.

These parameters are interpreted with respect to the specific anchor. Their
significance therefore varies with the anchor. For example, when you anchor to
`origin`, both `side` and `segment` are ignored because `origin` is not a
feature, just a point in space without horizontal or vertical extent.

Differences between all the different kinds of anchors will be documented here
in future. Broadly, they are:

* The availability and interpretation of different compass and vertical segment
  codes, including default values for when such codes are omitted from a
  configuration or, equivalently, set to `null`.
* Default shape, with and without compass and vertical segment codes. In
  several cases, not supplying such identifiers produces a shape similar to the
  entire anchor.

## Orientation with an anchor

By default, any feature anchored to another will be subject to the same
geometric transformations as its anchor. That is the normal way to determine
proper position and orientation. For example, if you have a row of keys with
progressive pitch, and you anchor an identical port to each key, the ports will
not all face the same direction. Each one will be rotated along with its
individual parent key.

There is an exception for features that stick to the floor, such as sprues and
bottom-plate fasteners. These would be less useful if rotated with their
anchors, so instead, they stay level.

At your option, you can similarly cancel rotation for any other feature. To do
so, supply the `preserve-orientation` parameter, set to `true`. This loosens
the coupling somewhat and can therefore make anchoring easier to reason about.
It is commonly used for key clusters, since they have complex orientation
settings of their own.

## General tuning

In addition to naming an anchor, side and segment to position a feature, you
can also affect the position of that feature in two more general ways:

* Rotations of the feature around the origin.
* Offsets for geometric translation away from the anchor.

Both types of tuning parameters come in two flavours:

* Intrinsic tuning is applied in the local vector space of the target anchor,
  before anchor-specific transformations. In this context, the origin is some
  part of the feature itself.
* Extrinsic tuning is applied later, in global vector space, following
  transformations specific to the anchor. In this context, the origin is
  `origin`.

In each context, rotation happens first, before translation. This means that
intrinsic rotation always spins the feature around itself, in place, though not
always around its own centre of mass. Extrinsic rotation is typically less
useful because it swings the feature on a long rode to the anchor, which is
harder to reason about.

Unlike side and segment codes, the effects of tuning do not depend on the type
of anchor. Tuning is also *not* affected by `preserve-orientation`.

If you want to, you can anchor everything directly to the `origin` and use only
tuning to put things wherever you want them. However, if you do anchor
everything to that point, there will be lots of large coordinate offsets. That
can get hard to read and will not be adaptive to further changes you make.

## The full parameter set

Where the [parameter manual](options-main.md) offers a section called
`anchoring`, that section may contain any or all of the following parameters:

* `anchor`: A code for the anchor itself. `origin` by default.
* `side`: A compass point relative to the middle of `anchor`. The default is
  `null`, signifying the centre.
* `segment`: An integer ID for a vertical segment of `anchor`. The default is
  `null` (no segment), which varies in interpretation.
* `preserve-orientation`: A Boolean value. If `true`, all transformations
  specific to the anchor are instead applied to a set of coordinates, whereupon
  the anchored feature is merely translated to those coordinates.
* `intrinsic-offset`, `extrinsic-offset`: Each of these is a three-dimensional
  vector of numbers in mm.
* `intrinsic-rotation`, `extrinsic-rotation`: Each of these is a
  three-dimensional vector of numbers in radians, or special codes (see
  [here](configuration.md) for notation) describing rotation of the feature,
  not of the anchor.

These are all optional, and they’re all explained above.
