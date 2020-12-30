# Configuration of the application

As noted in the [introduction](intro.md), the DMOTE application is governed by
parameters in YAML files. This document outlines some of the general concepts
you need to understand such files. The specifics are documented starting
[here](options-main.md), with subordinate, nested structures linked through
that document. For a more hands-on approach to the process of configuration,
starting from scratch, try the [Butty tutorial](tutorial-1a.md).

## Picking files

The application comes bundled with some YAML files under `config`. You don’t
have to use them. If you do use them, you can change them according to your
needs. However, it is generally easier to add your own files, maintaining them
separately from the DMOTE repository. That way, there will be no collisions
when you upgrade the application with a `git pull`.

The [execution guide](execution.md) shows how to run the application with a
given file or a whole series of files at once, including any combination of
bundled and personal files.

## Nomenclature

The terms you encounter in the configuration are based partly on metaphors.

### The compass metaphor

The parameter files—as well as the code—use a compass metaphor to describe
directions. You enter compass points like `N` (north) and `NW` (northwest) to
identify the sides and corners of features.

To understand these terms, imagine having (the right-hand side of) the keyboard
in front of you, as you would use it, while you face true north.

Nominal north in configuration thus refers to the direction away from the user:
the far side. South is the direction toward the user: the near side.

West and east vary on each half of a split keyboard because the left-hand
side is a mirror image of the right-hand side. The right-hand side is primary
for the purposes of nomenclature. On either half, the west is inward, toward
the space between the two halves of the keyboard. The east is outward, away
from the other half of the keyboard. If you are not building a split keyboard,
think of your case as you would the right-hand side: The west is on your left
and the east is on your right.

In Euclidean space, the x axis goes from west to east and the y axis from south
to north. However, there is one caveat to this: When features are rotated, the
compass needle doesn’t move. Where the distinction can be made, the compass
metaphor describes the local vector space, which is not always the global
vector space.

### The anchor metaphor

An anchor is a named starting point for the position of a part of the keyboard.
As the metaphor implies, an anchored feature is allowed to drift as if on a
rode, a set distance away from its anchor, in a given direction.

You can define new anchors as part of the configuration, and refer to different
pieces of them by side and segment codes. Anchoring is explained in detail
[here](options-anchoring.md).

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
or more angles, including parameters with names like `intrinsic-rotation`,
`pitch`, `roll`, `yaw` etc., you can specify each angle in at least two ways:

* As a real number, such as `1`, for 1 radian.
* As a mathematical formula using [π](https://en.wikipedia.org/wiki/Pi), such
  as `π * 2`, for one full turn.
* As text, naming a point of the compass, such as `NNE` for one sixteenth turn,
  i.e π/8 radians. This is intended for rotation in the xy plane and would be
  confusing in other uses.

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
flat. This can be changed with the `intrinsic-rotation` parameter for its
`anchoring`. To flip it over, getting the component side facing down and the
connector edge facing your right, you would set this parameter like `[0, π,
π/2]`. That’s no rotation around the x axis, a half turn (flip) around the y
axis, and a quarter turn around the z axis.

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

Note that although custom bodies can be defined, they should not be named in a
`body` setting, because they are formed entirely from parts of the predefined
bodies named above.
