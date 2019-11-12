## The Dactyl-ManuForm keyboard: Opposable Thumb Edition

This, the DMOTE, is a fork of the
[Dactyl-ManuForm](https://github.com/tshort/dactyl-keyboard), a parametrized,
split-hand, concave, columnar, ergonomic keyboard. In this fork, the thumb
cluster has been modified to minimize shearing forces.

[![Image of the second working DMOTE](http://viktor.eikman.se/image/dmote-2-top-down-view/display)](http://viktor.eikman.se/article/the-dmote/)

For more images and a brief article click the image.

Parameters have been moved out of the application code itself, into separate
files that are safe and easy to edit. Use them to change:

- Switch type: ALPS or MX.
- Size and shape.
    - Row and column curvature and tilt (tenting).
    - Exceptions at any level, down to the position of individual keys.
- Minor features like LED strips and wrist rests.


### Introduction & Getting Started: [doc/intro.md](doc/intro.md)
### Documentation: [doc/](doc/)
---
## The DMOTE Family

* Custom wrist rests: [veikman/dmote-topography](https://github.com/veikman/dmote-topography)

   Generates custom wrist rests with smooth organic topography. The topography is based on a bivariate normal distribution function multiplied by a logarithmic mound shape.

* A stabilizing accesory for the DMOTE: [veikman/dmote-beam](https://github.com/veikman/dmote-beam)

    Want even more tenting control? Hate it when your split keyboard gets out of line? Try the DMOTE Beam.

* Keycaps for ALPS or MX switches: [veikman/dmote-keycap](https://github.com/veikman/dmote-keycap)

    `dmote-keycap` produces a three-dimensional geometry for a keycap: Either a featureless maquette for use in easily rendered previews, or a keycap that you can print and use.


---

### License

Copyright © 2015-2019 Matthew Adereth, Tom Short, Viktor Eikman et al.

The source code for generating the models (everything excluding the [things/](things/) and [resources/](resources/) directories) is distributed under the [GNU AFFERO GENERAL PUBLIC LICENSE Version 3](LICENSE). The generated models and PCB designs are distributed under the [Creative Commons Attribution-NonCommercial-ShareAlike License Version 3.0](LICENSE-models).
