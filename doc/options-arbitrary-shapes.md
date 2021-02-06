# Arbitrary shapes

This document does not describe any specific options for the application, but a
*structure* common to a couple of different options. It is used for both
custom-body cuts and tweaks, as described [here](options-main.md) with
examples specific to each use case.

The structure described in this document is a crude
[DSL](https://en.wikipedia.org/wiki/Domain-specific_language) for geometry,
developed specifically for anchoring shapes to existing shapes on a keyboard,
within YAML.

## Overview

The structure is arbitrarily nested, starting with a map of names to lists.

The names at the **root level** are arbitrary but should be distinct and
descriptive. They cannot serve as anchors. Their only technical significance
lies in the fact that when you combine multiple configuration files, a later
entry will override a previous entry if and only if they share the same name.

Names at the root level are keys. They map to lists. Each item in each list
must follow one of the following patterns:

- A **leaf node**, representing a simple shape in a specific place.
- A **branch node**, containing a list like the one below each name and
  representing some combination of the nodes in it.

These terms are metaphorical. In the metaphor, the list itself is not one tree
but the soil of a grove of trees. Nodes at this entry level can have special
properties, whereas subordinate nodes cannot.

## Leaf nodes

Each leaf node places something near a named part of the keyboard. This is
ordinary [anchoring](configuration.md) of simple shapes. In the final form of
a leaf, it is a map with the following keys:

- `anchoring` (required): A nested map. See the general documentation
  [here](configuration.md).
- `sweep` (optional): An integer. If you supply a sweep, you must also supply a
  `segment` in `anchoring`. `sweep` identifies another segment and must be the
  larger of the two numbers. With both, the leaf will represent the convex
  hull of the two segments plus all segments between them, off the same anchor.
  This is most commonly used to finish the outer walls of a case.
- `size` (optional): An `[x, y, z]` vector describing a cuboid, in mm. If you
  supply this, for certain types of anchors, it overrides the default model.
  However, some types of anchors will ignore a custom size.

The default size depends both on the type of anchor and on which anchoring
parameters you use.  For types of anchors that have no basic size associated
with them, the default is a tiny point.

### Compact notation

All those keys in a leaf map take a up a lot of space. If you wish, you can
instead define each leaf in the form of a list of 1 to 5 elements:

1. The `anchor`.
2. The `side`.
3. The starting vertical segment ID.
4. The sweep, which is the stopping vertical segment ID.

This is the DSL part. As a fifth element, and/or in place of any of the last
three, the list may contain a map of additional leaf settings that is merged
into the final representation specified above.

Here’s the fine print on the two different ways to specify a leaf:

- You cannot use the list format alone to specify a rotation, size or offset.
- When you use the list format, the first element must be the name of an
  anchor. You cannot have a map as the first element.
- In the list format, you can specify `null` in place of elements you don’t
  want, though this is only meaningful for `side`.

## Branch nodes

By default, a branch node will create a convex hull around the nodes it
contains. However, this behaviour can be modified. The following keys are
recognized in any branch node:

- `hull-around` (required): The list of child nodes.
- `chunk-size` (optional): Any integer greater than 1. If this is set, child
  nodes will not share a single convex hull. Instead, there will be a sequence
  of smaller hulls, each encompassing this many items.
- `highlight` (optional): If `true`, render the node in OpenSCAD’s highlighting
  style. This is convenient while you work.

## Special properties at the entry level

Leaf and branch nodes immediately following a root-level name are special. They
grow in the soil and represent individual plants with their own roots. When
nodes are selected for a particular purpose, that selection happens at the root
level. Nodes at the root level may therefore contain the following extra keys,
which determine how each node affects the keyboard in a tweak:

- `body` (optional): Refer to general documentation [here](configuration.md).
  As usual, the default value is `auto`. When the node is a branch, `auto`
  uses the first leaf subordinate to the branch for the usual heuristics.
- `cut` (optional): If `true`, subtract material from the body instead of
  adding material. All such cuts happen after all additions.
- `reflect` (optional): If `true`, use the node on both sides of the yz-plane,
  i.e. on both sides of x = 0 within `body`. This is useful mainly in a central
  housing, for those tweaks that should be symmetrical on the left and right.
  It is not so useful in a main body or wrist rest, because those bodies are
  reflected with all of their tweaks, on a higher level, if at all.

Additionally, on a common theme:

- `above-ground` (optional): If `true`, influence the shape of `body` itself.
- `at-ground` (optional): If `true`, influence the shape of the bottom plate
  under `body`.
- `to-ground` (optional): If `true`, extend vertically from the stated position
  all the way to the ground plane.
- `shadow-ground` (optional): If `true`, project onto the ground plane when
  shaping the bottom plate.
- `polyfill` (optional): If `true`, fill out the bottom plate using polygons.

Nodes subordinate to branches may not contain these extra keys.

The extra keys are largely irrelevant for custom-body cuts, because the entire
grove is already a cut from a specified body in that context, and that body
does does not have its own bottom plate.

### Bottom plating methods

By default, `at-ground` causes the application to expand a bottom plate beneath
the marked node using polygons. This is redundant together with `polyfill`.

Polygons render quickly and can be concave, but are generally inaccurate
because the application cannot always identify the most relevant vertices in
the outermost hull.

`to-ground` and `shadow-ground` both change the means by which a node affects
the bottom plate. They use projection. Projections are slower than polygons and
will, depending on `chunk-size`, either fail to fill interior space
(`chunk-size` > 1) or fail to be concave.

`polyfill` is useful with `to-ground` or `shadow-ground` to force the use of
*both* projection and polygons, which is extra slow but effective in some
corner cases where you want a neat rim on a bottom plate under a partly concave
outer wall with `chunk-size` > 1.

### Interactions

`to-ground` implies both `at-ground` and `above-ground`, in addition to its
main effect of a convex hull with a projection of the node.

Both `shadow-ground` and `polyfill` individually imply `at-ground`, but are
specific and complementary in their respective methods.

All five parameters ultimately default to `false`, but this default value is
soft. If omitted, both `above-ground` and `to-ground` prefer the value of
`to-ground`, if `true`, to their default value. `at-ground` prefers both
`shadow-ground` and `polyfill` over `to-ground`.

The following examples of combinations are not exhaustive and assume that both
`above-ground` and `polyfill` are omitted from the node configuration.

| `at-ground` | `to-ground` | `shadow-ground` | In bottom plate? | In body? |
| ----------- | ----------- | --------------- | ---------------- | -------- |
| Omitted     | Omitted     | Omitted         | No               | No       |
| `true`      | Omitted     | Omitted         | Yes, polygons    | No       |
| `true`      | `true`      | Omitted         | Yes, projection  | Yes      |
| `true`      | `false`     | Omitted         | Yes, polygons    | No       |
| Omitted     | `true`      | Omitted         | Yes, projection  | Yes      |
| Omitted     | Omitted     | `true`          | Yes, projection  | No       |
| `false`     | `true`      | Omitted         | No               | Yes      |
| `false`     | Omitted     | `true`          | No               | No       |

Even with attention to side effects, many possible combinations are partly
redundant.

The reason why the parameters interact this way is so that default values can
be both consistent, conservative and good for rendering performance, while all
effects are still fully controllable with a fairly terse configuration.
