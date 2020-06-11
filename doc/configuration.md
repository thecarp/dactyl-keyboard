# Configuration of the application

As noted in the [introduction](intro.md), the DMOTE application is governed by
parameters in YAML files. The specifics of these files are [documented
here](options-main.md), with subordinate, nested structures linked through that
document.

This document is not about the specifics. It outlines some of the general
concepts.

## Picking files

The application comes bundled with some YAML files under `config`.  You can
change these files if you like. However, it is generally easier to add your own
files, maintaining them separately from the DMOTE repository. That way, there
will be no collisions when you upgrade the application with a `git pull`.

The [execution guide](execution.md) shows how to run the application with a
given file or a whole series of files at once.

## Nomenclature

The terms you encounter in the configuration are based partly on metaphors.

### The compass metaphor

The parameter files—as well as the code—use a compass metaphor to describe
directions. You enter compass points like `N` (north) and `NW` (northwest) to
identify the sides and corners of features.

To understand these terms, imagine having (the right-hand side of) the keyboard
in front of you, as you would use it, while you face true north.

“North” in configuration thus refers to the direction away from the user: the
far side. “South” is the direction toward the user: the near side.

“West” and “east” vary on each half of a split keyboard because the left-hand
side is a mirror image of the right-hand side. The right-hand side is primary
for the purposes of nomenclature. On either half, the west is inward, toward
the space between the two halves of the keyboard. The east is outward, away
from the other half of the keyboard. If you are not building a split keyboard,
think of your case as you would the right-hand side: The west is on your left
and the east is on your right.

In Euclidean space, the x axis goes from west to east, the y axis from south to
north, and the z axis from earth to sky. However, there is one caveat to this:
When features are rotated, the compass needle doesn’t move. Where the
distinction can be made, the compass metaphor describes the local vector space,
which is not always the global vector space.

### The anchor metaphor

All of the features of the keyboard are ultimately positioned in relation to
the origin of the global coordinate system (global vector space). The origin
has the coordinates `[0, 0, 0]`. The origin is not a feature of the keyboard,
but it has a name, which is `origin`, so you can refer to it.

If you want to, you can link everything directly to the origin. It’s the
default `anchor` wherever an anchor can be named. However, if you do link
everything to that point, there will be lots of large coordinate offsets. That
can get hard to read and will not be adaptive to further changes you make.

To keep things flexible and easy to maintain, features can be anchored to other
features, as long as the chain of anchors eventually reaches `origin`.

In addition to `origin`, there are a couple of other named anchors built into
the application:

* `rear-housing-exterior`: The outside of the rear housing, an optional feature
  of the main body.
* `rear-housing-interior`: The inside of the rear housing.

#### Parts of an anchor

Anchoring a feature determines its position in the coordinate system with
respect to its anchor. Along with the name of the anchor, you can specify a few
other things to get a specific part of the anchor:

* `side`: A compass point relative to the middle of the anchor, to target its
  periphery.
* `segment`: A numeric ID for a vertical segment of the anchor.
* `offset`: A two- or three-dimensional vector describing a geometric
  translation away from the anchor.

These parameters are interpreted with respect to the anchor. Their significance
therefore varies with the anchor. The differences will be documented here in
future. Broadly, they are of the following types:

* The availability and interpretation of different sides (compass codes) and
  vertical segment codes.
* Whether or not the anchored feature will be rotated like its anchor.
  Generally, the anchored feature itself is not rotated, but the `offset` does
  occur in the local vector space of the anchor.
* The default shape of a case tweak.

## Notation

The application offers some conveniences for specifying sizes and angles.

### Sizes

Dimensional settings typically use millimetres, by pragmatic convention.

For three-dimensional objects, parameters named `size` can take a few different
kinds of values. Ultimately, they need three: One for each dimension. The
completely literal way to specify a cube of 1 mm³ is as a list of x, y and z
coordinates in that order: `[1, 1, 1]`. For convenience, size parameters can
expand a more compact notation to that form.

| YAML         | Expanded result |
| ------------ | --------------- |
| `1`          | `[1, 1, 1]`     |
| `[1]`        | `[1, 1, 1]`     |
| `[1, 2]`     | `[1, 1, 2]`     |
| `[1, 2, 3]`  | `[1, 2, 3]`     |

### Angles

Like `scad-clj`, this application describes angles in
[radians](https://en.wikipedia.org/wiki/Radian). For parameters that take one
or more angles, including parameters with names like `rotation`,
`intrinsic-rotation`, `pitch`, `roll`, `yaw` etc., you can specify each angle
in at least two ways:

* As a real number, such as `1`, for 1 radian.
* As a mathematical formula using [π](https://en.wikipedia.org/wiki/Pi), such
  as `π * 2`, for one full turn.
* As text. For those parameters that are limited to rotation in the xy plane,
  you can name a point of the compass, such as `NNE`, for one sixteenth turn,
  i.e π/8 radians.

A formula must obey the following rules:

* It must begin with the letter `π` or, equivalently, `pi`, `PI` etc.
* The letter can optionally be followed by both a mathematical operator
  and a real number.
* An operator must be `*` (the asterisk) for multiplication or `/` (the forward
  slash or stroke) for division.
* Single spaces are permitted, not required, between the letter, the operator
  and the number. They are not permitted anywhere else in the formula.

A practical example: By default, as described in the
[parameter manual](options-main.md), a keyboard microcontroller PCB will lie
flat. This can be changed with a setting called `intrinsic-rotation`. To flip
it over, getting the component side facing down and the connector edge facing
your right, you would set this parameter like `[0, π, π/2]`. That’s no rotation
around the x axis, a half turn (flip) around the y axis, and a quarter turn
around the z axis.

## Bodies

The anchor metaphor is related to the concept of bodies. Whereas an `anchor`
setting controls the position of a feature in vector space, a `body` setting
controls which 3D models and therefore which output files will include the
feature, regardless of its position in vector space.

Where a `body` parameter appears, the following settings are recognized:

* `auto` (default): The application will determine the body from the anchor and
  other settings, defaulting to the main body in case of ambiguity.
* `main`: The main body of the keyboard.
* `central-housing`: The central housing.
* `wrist-rest`: The wrist rest.

The `auto` setting should cover most needs. When you deviate from it, take care
to harmonize with other settings. For example, if you specify
`central-housing`, the application will not check whether you have enabled the
central housing itself, and will not automatically enable it.
