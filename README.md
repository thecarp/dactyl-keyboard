## The Dactyl-ManuForm keyboard: Opposable Thumb Edition

This, the DMOTE, is an application for designing a keyboard that suits you. By
editing text files of parameters, you can change:

- Switch type: ALPS or MX.
- Size and shape.
    - Row and column curvature and tilt (tenting).
    - Exceptions at any level, down to the position of individual keys.
- Minor features like LED strips and wrist rests.

Here’s an example of what you can do: A split-hand, concave, columnar,
ergonomic keyboard with large thumb clusters and wrist rests.

[![Image of the second working DMOTE](http://viktor.eikman.se/image/dmote-2-top-down-view/display)](https://viktor.eikman.se/gallery/the-dmote/)

To get started, try the [introduction](doc/intro.md), part of the project’s
[documentation](doc/). For a less technical, more illustrated overview, try
[this article](http://viktor.eikman.se/article/the-dmote/).

### Links

This project, the DMOTE application, is a fork of Tom Short’s
[Dactyl-ManuForm](https://github.com/tshort/dactyl-keyboard). It’s one of many
forks in the [Dactyl](https://github.com/adereth/dactyl-keyboard) genus.

There’s a small family of accessory projects:

* [`dmote-keycap`](https://github.com/veikman/dmote-keycap):
  A library used directly by the DMOTE application to model both featureless
  maquettes for use in easily rendered previews, and “minimal” keycaps you can
  print and use to save space.
* [`dmote-topography`](https://github.com/veikman/dmote-topography):
  Smooth organic topography for height maps you can use as wrist rests,
  including the example bundled in the [resource folder](resources/heightmap)
  of this project.
* [`dmote-beam`](https://github.com/veikman/dmote-beam):
  Configurable clips for stabilizing a split keyboard by holding a
  connecting rod between the two halves.

Like its parent, the DMOTE project is based on Matthew Farrell’s
[`scad-clj`](https://github.com/farrellm/scad-clj). A second small family of
general CAD libraries for `scad-clj` have grown up around the DMOTE project.

* [`scad-app`](https://github.com/veikman/scap-app): A simple way to turn
  `scad-clj` specifications into applications, stripping out some boilerplate.
  See also [`cad-template`](https://github.com/veikman/cad-template), which is
  all boilerplate for starting new CAD projects that are ready to render.
* [`scad-tarmi`](https://github.com/veikman/scad-tarmi): Commonplace abstractions.
* [`scad-klupe`](https://github.com/veikman/scad-klupe): Threaded fasteners.

### License

Copyright © 2015-2020 Matthew Adereth, Tom Short, Viktor Eikman et al.

The source code for generating the models (everything excluding the
[things/](things/) and [resources/](resources/) directories) is distributed
under the [GNU AFFERO GENERAL PUBLIC LICENSE Version 3](LICENSE). The generated
models and PCB designs are distributed under the [Creative Commons
Attribution-NonCommercial-ShareAlike License Version 3.0](LICENSE-models).
