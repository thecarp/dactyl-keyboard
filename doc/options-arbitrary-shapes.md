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
but the soil of a grove of trees. Nodes at this entry level can have special properties, whereas subordinate nodes cannot.

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
- `intrinsic-rotation` (optional): An `[x, y, z]` vector describing angles of
  rotation around the center of the leaf at each segment of its anchor. Certain
  types of anchors will ignore this setting.
- `size` (optional): An `[x, y, z]` vector describing a cuboid, in mm. If you
  supply this, for certain types of anchors, it overrides the default model.
  However, some types of anchors will ignore a custom size as well as
  `intrinsic-rotation`. The default size depends both on the type of anchor and
  on which anchoring parameters you use. For types of anchors that have no
  basic size associated with them, the default is a tiny point.

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
  want but, this is only meaningful for `side`.

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

Leaf and branch nodes immediately following a root-level
name are special. They grow in the soil and represent individual plants with
their own roots. When nodes are selected for a particular purpose, that
selection happens at the root level. Nodes at the root level may therefore
contain the following extra keys, which determine how each node affects the
keyboard in a tweak:

- `positive` (optional): If `true`, add material to the case. If `false`,
  subtract material. The default value is `true`.
- `at-ground` (optional): This setting has two effects. If `true`, extend
  vertically down to the ground plane, as with a `full` wall, *and* influence
  the shape of a `bottom-plate`. The default value is `false`.
- `above-ground` (optional): If `true`, appear as part of the case. The
  default value is `true`. When this is `false` and `at-ground` is `true`, the
  node affects the bottom plate only, which is the only use for this option.
- `body` (optional): Refer to general documentation [here](configuration.md).
  As usual, the default value is `auto`. When the node is a branch, `auto`
  uses the first leaf subordinate to the branch for the usual heuristics.

Nodes subordinate to branches may not contain these extra keys. They are
largely ignored for custom-body cuts.
