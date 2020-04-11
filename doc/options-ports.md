<!--This document was generated and is intended for rendering to HTML on GitHub. Edit the source files, not this file.-->

# Port configuration options

Each heading in this document represents a recognized configuration key in YAML files for a DMOTE variant.

This specific document describes options for the shape and position of any individual port. One set of such options will exist for each entry in `ports`, a section whose place in the larger hierarchy can be seen [here](options-main.md). Example uses for ports:

* One port for the connection between the two halves of a reflected keyboard without a central housing. Such ports are usually TRRS or 4P4C (“RJ9”), but you can use practically anything with enough wires.
* An external USB port for interfacing with your computer, such as a full-size USB A port. You might want this when your MCU either has no such port attached or the attached port is too weak for direct human use (cf. `shelf`) or difficult to get into a good position.
* Additional USB ports, connected via internal hub or to an integrated microphone clip, phone charger etc.
* A speaker for QMK audio.
* An LCD screen for QMK video.
* An exotic human interface device, such as a large rotary encoder or trackball, not supported by this application as a type of keyboard switch.
* Assortment drawers built into a large rear or central housing.

Notice ports attached directly to microcontroller boards are treated in the `mcu` section, not here.

## Table of contents
- Parameter <a href="#user-content-include">`include`</a>
- Parameter <a href="#user-content-type">`type`</a>
- Parameter <a href="#user-content-size">`size`</a>
- Section <a href="#user-content-alignment">`alignment`</a>
    - Parameter <a href="#user-content-alignment-segment">`segment`</a>
    - Parameter <a href="#user-content-alignment-side">`side`</a>
- Parameter <a href="#user-content-intrinsic-rotation">`intrinsic-rotation`</a>
- Section <a href="#user-content-position">`position`</a>
    - Parameter <a href="#user-content-position-anchor">`anchor`</a>
    - Parameter <a href="#user-content-position-side">`side`</a>
    - Parameter <a href="#user-content-position-segment">`segment`</a>
    - Parameter <a href="#user-content-position-offset">`offset`</a>
- Section <a href="#user-content-holder">`holder`</a>
    - Parameter <a href="#user-content-holder-include">`include`</a>
    - Parameter <a href="#user-content-holder-alias">`alias`</a>
    - Parameter <a href="#user-content-holder-thickness">`thickness`</a>

## Parameter <a id="include">`include`</a>

If `true`, include the port. The main use of this option is for disabling ports defined in other configuration files. The default value is `false` for consistency with other inclusion parameters.

## Parameter <a id="type">`type`</a>

A code identifying a common type of port. The following values are recognized.

* `custom`, meaning that `size` (below) will take effect.
* `modular-4p4c-616e`: modular connector 4P4C, socket 616E, minus the vertical stripe.
* `usb-c`: USB C.
* `usb-full-2b`: full-size USB 2 B.
* `usb-full-3b`: full-size USB 3 B.
* `usb-full-a`: full-size USB A.
* `usb-micro-2b`: USB micro 2 B.
* `usb-mini-b`: USB mini B.

## Parameter <a id="size">`size`</a>

An `[x, y, z]` vector specifying the size of the port in mm. This is used only with the `custom` port type.

There are limited facilities for specifying the shape of a port. Basically, this parameter assumes a cuboid socket. For any different shape, get as close as possible with `tweaks`, then make your own adapter and/or widen the socket with a soldering iron or similar tools to fit a more complex object.

## Section <a id="alignment">`alignment`</a>

How the port lines itself up at its position.

### Parameter <a id="alignment-segment">`segment`</a>

Which vertical segment of the port itself to place at `position` below. The default value here is 0, meaning the ceiling of the port.

### Parameter <a id="alignment-side">`side`</a>

Which wall or corner of the port itself to place at `position`. The default value here is `N` (nominal north), which is the open face of the port.

## Parameter <a id="intrinsic-rotation">`intrinsic-rotation`</a>

An `[x, y, z]` vector of radians, rotating the port around its point of `alignment` before moving it to `position`.

## Section <a id="position">`position`</a>

This section collects the most important factors for placing the aligned port on the keyboard.

### Parameter <a id="position-anchor">`anchor`</a>

The name of a feature at which to place the port.

### Parameter <a id="position-side">`side`</a>

A compass-point code for one side of the feature named in `anchor`. The default is `N`, signifying the north side.

### Parameter <a id="position-segment">`segment`</a>

An integer identifying one vertical segment of the feature named in `anchor`. The default is `0`, signifying the topmost part of the anchor.

### Parameter <a id="position-offset">`offset`</a>

A three-dimensional offset in mm from the feature named in `anchor`. This is applied in the anchor’s local frame of reference and may therefore be subject to various rotations etc.

## Section <a id="holder">`holder`</a>

A map describing a positive addition to the case on five sides of the port: Every side but the front.

### Parameter <a id="holder-include">`include`</a>

If `true`, build a wall around the port.

### Parameter <a id="holder-alias">`alias`</a>

A name for the holder, to allow anchoring other features to it.

### Parameter <a id="holder-thickness">`thickness`</a>

A number specifying the thickness of the holder’s wall on each side, in mm.

⸻

This document was generated from the application CLI.
