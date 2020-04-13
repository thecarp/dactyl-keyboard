# Introduction to the DMOTE

Here’s how to use this code repository to build a keyboard case.
For a still-more general introduction to the larger project, see
[this](https://viktor.eikman.se/article/the-dmote/).

## From code to print

This repository is source code for a Clojure application. Clojure runs on
[the JVM](https://en.wikipedia.org/wiki/Java_(software_platform)). The
application produces an [OpenSCAD](http://www.openscad.org/) program which,
in turn, can be rendered to a portable geometric description like
[STL](https://en.wikipedia.org/wiki/STL_(file_format)). STL can be
[sliced](https://en.wikipedia.org/wiki/Slicer_(3D_printing)) to
[G-code](https://en.wikipedia.org/wiki/G-code) and the G-code can
steer a 3D printer.

OpenSCAD can represent the model visually, but there is no step in this process
where you point and click with a mouse to change the design. The shape of the
keyboard is determined by your written parameters to the Clojure application.

Roughly, the build chain looks like this:

> parameters through this app (compiled) → preview → rendering → slicing → printing

Equivalently, in terms of typical file name endings:

> .yaml through .clj (or .jar) → .scad → .stl → .gcode → tangible keyboard

If this repository includes STL files you will find them in the `things/stl`
directory. They should be ready to print. Otherwise, here’s how to make your
own.

### Setting up the build environment

* Install the [Clojure runtime](https://clojure.org)
* Install the [Leiningen project manager](http://leiningen.org/)
* Optional: Install [GNU make](https://www.gnu.org/software/make/)
* Install [OpenSCAD](http://www.openscad.org/)

On Debian GNU+Linux, the first three are accomplished with `apt install clojure
leiningen make`. The necessary Clojure libraries will be pulled in when you run
Leiningen.

### Producing OpenSCAD and STL files

There is more than one way to run the application. The easiest and most
automated is to call `make` from your command line. Refer to the [execution
guide](execution.md) for details and alternatives.

After running the application, start OpenSCAD. Open one of the
`things/scad/*.scad` files for a preview. To render a complex model in
OpenSCAD, you may need to go to Edit >> Preferences >> Advanced and raise the
ceiling for when to “Turn off rendering”. When you are satisfied with the
preview, you can render to STL from OpenSCAD.

## Customization

You probably want to customize the design for your own hands. You won’t need
to touch the source code for a personal fit or additional keys.

The Clojure application combines configuration details from zero or more
[YAML](https://en.wikipedia.org/wiki/YAML) files like the ones under `config`.
The process is [documented here](configuration.md).

### Deeper changes

If you find that you cannot get what you want just by changing the parameters,
you need to edit the source code. If you are not familiar with OpenSCAD, start
by experimenting with its native format, writing `.scad` files from scratch.
Then consider starting in `src/dactyl_keyboard/sandbox.clj` to get familiar
with `scad-clj`. It writes OpenSCAD code for you with helpful abstractions.

If you want your changes to the source code to be merged upstream, please do
not remove or break existing features. There are already several `include` and
`style` parameters designed to support a variety of mutually incompatible
styles in the code base. Add yours instead of simply repurposing functions,
and test to make sure you have not damaged other styles.

## Printing tips

For printing prototypes and any printing with PLA-like materials that stiffen
quickly, build support from the base plate only. This simplifies the process
of removing the supports.

### Accuracy

If you are printing holes for threaded fasteners as part of your design, please
note that common FDM printers won’t print threaded holes smaller than M3 with
useful accuracy. M4 is a safer bet, but even that may require some manual
cleanup, particularly in orientations other than the vertical.

For accuracy problems in general, and especially for problems with threaded
holes, consider tweaking the DFM settings [documented here](options-main.md),
particularly the `error-general` parameter.

You may prefer tapping threads yourself. Each of the `bolt-properties`
parameters to the application can take a value for `include-threading`. If you
set this to `false`, a hole cut for that bolt will be a plain cylinder with the
inner diameter (a.k.a. minor diameter) of standard ISO threading. If you have
enough plastic in the perimeter of that hole, you can drill it to clean up the
print and then tap it.

### Bottom plates

If you are using threaded fasteners to connect bottom plates directly to the
case (the `threads` style), please note that common FDM printers usually won’t
print threaded holes smaller than M3 with useful accuracy. M4 is a safer bet.

If you are having trouble with the fit and neither DFM settings nor larger
fasteners are helping, consider a greater `thickness` for the anchor points,
along with slicer settings that give you thinner walls and less infill. This
should give you a more yielding threaded hole, decreasing the risk of a
delaminating crack, but increasing the risk of threads deforming over time.

In any case you may want to use `foot-plates` to provide additional support for
the anchor points.

### Wrist rests

If you are including wrist rests, consider printing the plinths without a
bottom plate and with sparse or gradual infill. This makes it easy to pour
plaster or some other dense material into the plinths to add mass.

## After printing

Instructions specific to the DMOTE have yet to be written for hand-wiring the
switches with diodes and installing embedded microcontrollers. To get started
with that stuff, please refer to the original instructions for the
Dactyl-ManuForm or the Dactyl, or contact the maintainer of the fork you
are printing.

As for microcontroller firmware, QMK works great. In that project, the DMOTE
is filed as a version of the Dactyl-ManuForm
[here](https://github.com/qmk/qmk_firmware/tree/master/keyboards/handwired/dactyl_manuform/dmote).
