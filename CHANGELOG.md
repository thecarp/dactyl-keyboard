# Change log
This log follows the conventions of
[keepachangelog.com](http://keepachangelog.com/). It picks up from DMOTE
version 0.2.0, thus covering only a fraction of the project’s history.

## [Unreleased]
### Changed
- Moved and replaced some parameters:
    - The `case` section of parameters was **renamed** to `main-body` to
      avoid ambiguity with respect to the new central housing.
        - The top-level parameter `split` was both moved into the `main-body`
          section and renamed to `reflect` to avoid misleading the user about
          how it interacts with the new `central-housing` feature.
        - The `case-side` style of mounting wrist rests was similarly renamed
          to `main-side`.
    - Multiple sections for anchoring have been **renamed** from `position` to
      `anchoring` to reduce ambiguity with respect to bodies.
        - Parameters named `corner` have been **renamed** to `side`. They now
          take codes for cardinal compass points as well as actual corners. The
          default side for anchoring has changed to the north.
    - All parameters governing individual properties of threaded bolts have
      been removed in favour of more powerful new parameters based on options
      exposed by a new library (`scad-klupe`) that draws bolts for the
      application.
      This change provides greater freedom to choose different bolt head types,
      partial threading, no threading (with a diameter suitable for tapping
      holes manually) etc.
    - The `connection` section has been replaced by a general `ports` map.
    - Heat-set inserts for attaching bottom plates are no longer a separate
      style. Thus the `inserts` option has been removed from the `main-body` →
      `bottom-plate` → `installation` → `style` parameter and replaced by a new
      `include` parameter, governing the same feature independently of style.
- Removed much of the special treatment of the rear housing, no longer needed.
    - The two parameters named `prefer-rear-housing` and their associated
      functionality were all **removed**, having been obviated by placement in
      relation to a wide range of anchors.
    - The `raise` parameter for `connection` has been replaced by an extension
      of the concept of vertical segments to the rear housing.
    - Removed `into-nook`, which automated some fine tuning for placing ports
      and MCUs inside the rear housing. This has been obviated in part by the
      corner-to-side change, which allows the user to target an edge rather
      than a corner of the rear housing, and in part by the extension of
      segments on the rear housing to include segment 2, referring to floor
      level beneath the walls.
    - Moved the parameters that anchor each named secondary position to another
      feature into a nested mapping called `anchoring`. An `offset` within this
      mapping no longer gets exceptional treatment; it now occurs in the vector
      space of the anchor. The old behaviour, i.e. translation in the
      global vector space, has a new parameter (`translation`).
- MCU supports have changed, gaining more power but losing some ease of use,
  to work better with the option of central housing.
    - The default orientation of the MCU PCB has changed, from standing on its
      long edge to lying flat.
        - In the Dactyl-ManuForm model, the MCU now lies flat, whereas in other
          pre-v0.6.0 bundled build targets, it’s still on its long edge because
          of accompanying changes to the configurations.
    - The MCU support style parameter was **removed**.
        - Instead of `lock` and `stop` existing as mutually exclusive styles,
          with a single grip and the requirement of a key mount as the anchor
          of that grip for a stop, any MCU can now be supported by any number
          of `grip` anchors: Named points in space around the MCU, connected
          using `tweaks`. These grips can be freely combined with a lock.
          See the Dactyl-ManuForm base file for an example of how to use grips.
    - `mcu` → `support` → `height-factor` was moved to
      `mcu` → `support` → `lock` → `width-factor`.
    - `mcu` → `support` → `lateral-spacing` was moved to
      `mcu` → `support` → `lock` → `plate` → `clearance`.
- Anchoring one feature to another no longer imposes a rotation. Thus the
  MCU's `rotation` parameter has moved out of its `position` map, to a new
  name (`intrinsic-rotation`) and will be needed more often.
- The alcove generated for the front end of an MCU PCBA now uses the general
  DFM setting (`error-general`). The `mcu` → `margin` setting was removed.
- Anchors for bottom-plate mounting screws no longer have domed caps by
  default. These are still available but are governed by a new parameter.
  Domes were made optional to make it easier to tap threads rather than print
  them, and use longer screws than was originally intended.

### Added
- Documentation:
    - An execution guide, as a new document branched off from the introduction.
    - A general guide to concepts in the configuration layer.
    - Tables of content in auto-generated documents.
    - Stock descriptions of recurring parameters.
- Support for a number of different types of MCUs beyond the Pro Micro:
  Common Teensies as well as the Elite-C and Proton C.
- Central housing, a new feature.
- An MCU shelf. This type of MCU support corresponds directly to the
  Dactyl-ManuForm’s `teensy-holder` object and is therefore not new, but
  it has some parameters to extend its functionality.
- Support for an arbitrary number of ports.
    - Support for standard types of ports, including different USB connectors
      and a modular connector (616E for 4P4C, previously emulated in
      configuration).
- Extensions to bottom plates for projections of the anchors used to fasten
  such plates to the case. This restores a feature of the upstream
  Dactyl-ManuForm.
- The ability to target the plate of an MCU lock, and ports, for case `tweaks`.
- An extension of the concept of segments to the rear housing and the plate of
  an MCU lock for case `tweaks`.
- The ability to override specific coordinates for secondary named positions.
- A GNU Make target for the Dactyl-ManuForm.

### Fixed
- More accurate and printer-friendly spaces for the wings on ALPS-style
  switches.
- Made `transpile.sh` sensitive to configuration changes again.
- A more categorical fix for `dmote-keycap` parameter support, achieved by
  migrating that library’s parsing logic into the library itself.
  See version 0.5.1.
- Slightly improved user feedback when configuration files contain structural
  problems.

### Developer
- Improved REPL support.
- New modules:
  - `anch`, collecting collectors of anchor points.
  - `compass`, gathering code from `generics` and `matrix` with refactoring
    to improve the compass metaphor for feature placement. For example, the new
    MCU grip anchors are created with a corner such as `SW`, and this maps to a
    keyword (`:SW`), not to a tuple of cardinal-direction keywords (`[:south
    :west]`). Corner keywords are translated to tuples at need. Note that
    the new direction keywords are not yet namespaced to the compass module.
  - `cots`, gathering information on commercial off-the-shelf goods.
  - `mcu`, breaking MCU features out of `auxf`.
  - `misc`, which collects everything that remained of `generics` after
    compass code moved out. This makes two `misc` modules.
  - `poly`, collecting helper functions for making polyhedra.
  - `tweak`, breaking tweak plating out of `body` and `bottom`.
- A folder of configuration files under `test/config` for manual regression
  testing.

### Migration guide

Compare versions of `config/dmote/base.yaml` to see how to migrate your old
configuration files. Salient points:

* Replace each `fasteners` → `diameter` with a `bolt-properties` → `m-diameter`
  setting, and each bolt length setting with `bolt-properties` → `total-length`
  or `threaded-length`, depending on whether you want the head to count towards
  length. For the MCU lock, the term is `fastener-properties` to avoid
  confusion with the bolt of a lock.
* To compensate for the changed default orientation of the MCU in an existing
  custom configuration, use the moved `intrinsic-rotation` setting for your MCU
  support, with the approximate value `[0, 1.5708, 0]`, plus something for the
  z axis if that used to be rotated by its anchor.
* Rename `corner` parameters to `side`.
* Remove `connection` and add equivalent settings to the new `ports` map.

## [Version 0.5.1] - 2019-10-16
### Fixed
- Added a parser for one more of `dmote-keycap`’s parameters (`supported`),
  thus allowing supports to be turned off for keycap models rendered through
  this application.

## [Version 0.5.0] - 2019-07-21
### Changed
- Secondary aliases (`secondaries`) are now a map and case `tweaks` are
  likewise nested underneath a layer of names. Both of these structural
  changes add power to the configuration layer, reducing the need for
  duplication of data.
- Fixes that make the height of the case easier to manage constitute changes
  to previous behaviour:
    - Key mounting plates, which were previously located immediately on top of
      the nominal position of each key, are now below the nominal. In other
      words, key mounting plates have dropped down by the thickness of the
      mounting plates. This means that, on a completely flat keyboard, the
      configured height of each key mount is now the height of the case.
        - When you change key mount thickness, the difference is now internal
          to the keyboard and will no longer affect the relative positions of
          the switches.
        - To match this change, mount thickness is no longer a factor in
          computing curvature. This in turn affects the nominal positions of
          keys. It effectively recalibrates the scale of matrix separation,
          correcting it so that the default value of value of zero is more
          likely to create a good design.
    - Webbing and case walls etc. are (still) governed by key mounts, so they
      have also dropped down.
    - Similarly, the configured height of the rear housing is now its actual
      height, not the centre height of the cuboids that make up its corners.
- Moved bundled YAML.
    - The entire `resources/opt` folder is now at `config`.
    - Most of `resources/opt/default.yaml` has been renamed (to
      `config/dmote/base.yaml`) and is now less privileged.
    - A simple `make` has the same effect as before but passes more
      arguments to achieve it. Without arguments, the Clojure
      application now describes an unusable single-button keyboard.
- Changes to the bundled 62-key DMOTE configuration:
    - Switched from M3 to M4 screws for attaching the bottom plate and for
      locking the MCU PCB in place. This makes for quicker previews and easier
      printing.
    - Switched from flat to conical points for bottom-plate fasteners, just
      so the holes are easier to slice without getting interior supports.
    - Minor tweaks, like renaming the `maquette-dsa` style to `default`
      and removing a secondary anchor obviated by new controls for rear-housing
      post thickness.
- Replaced the nut boss in an MCU lock bolt with printed threading of the hole.

### Added
- Key mounting plates responsive to key `unit-size` for ease with oblong keys.
- A bundled 12-key macropad configuration, mainly as an object lesson.
- A `split` parameter at the highest level. This is false by default and absent
  in `config/base.yaml`, to enable macropads and relatively regular keyboards.
  It’s true in `config/dmote/base.yaml`.
    - An `include` parameter for the connection metasocket. This is false by
      default and true in `config/dmote/base.yaml`. Its effect is contingent
      upon the `split` parameter.
- An `include` parameter for MCU PCBA support. This is false by
  default and absent in `config/base.yaml`, mainly to enable tutorials where
  the MCU support does not pose a distraction, and partly to allow custom
  alternatives to the supported styles.
- More finely grained control for dimensions previously governed by general
  case webbing thickness.
    - New parameters for rear-housing wall and roof thickness.
    - A new parameter for connection socket wall thickness.
- A new implementation of the `solid` style of wrist rest attachment,
  with a bundled example configuration powered by the new structure of
  `secondaries` and case `tweaks`. In the example, the case and wrist rest
  are one piece of plastic. In a previous implementation, removed in version
  0.3.0, the two were separate pieces that snapped together, which put more
  requirements on the shape of the case and was not useful enough.
- A new DFM parameter, `fastener-plate-offset`, for tighter holes through
  bottom plates.
- A new bundled configuration fragment, `config/dmote/mx.yaml`, imposing
  MX-style switches on the DMOTE.
- More features added to `config/dactyl_manuform/base.yaml`,
  reconstructing part of the classic upstream shape as a configuration.
- A primitive means of combining YAML files by passing them as targets to Make.
  This does not work as intended and may be removed in future.

### Fixed
- Renamed a file (from `aux` to `auxf`) to work around file system
  restrictions inherited from MS DOS into current versions of Windows.
- Corrected placement of wrist-rest fastener anchors for the thickness of the
  bottom plate.
- Fixed a bad function call for `stop`-style MCU support.
- Slightly more accurate models of switches.

### Developer
- In the interest of versatility, the Clojure code no longer refers to any YAML
  files. Instead, the default configuration values that are built into the code
  itself are slightly richer, to prevent crashing without YAML files.
- Restructured the makefile, renaming some of the phony targets (e.g.
  `visualization` to `vis`) and removing others for the present. `make all` no
  longer exercises as much of the code.

## [Version 0.4.0] - 2019-06-06
### Changed
- Moved and replaced some parameters:
    - Both the `keycaps` and `switches` sections have been replaced. There is
      now a `keys` section that defines one or more `styles`, as well as a
      `key-style` parameter in the nesting `by-key` section of parameters.
    - The `to-ground` key for case tweaks has been renamed to `at-ground` for
      clarity with respect to a new `above-ground` key.
    - The general `error` parameter for DFM has been renamed `error-general`.
- Changed default.yaml from a 60-key layout with a user-facing 1-key `aux0`
  cluster to a 62-key layout (31 on each half) with a 2-key `aux0` cluster at
  the opposite corner and facing away from the user.

### Added
- Support for multiple, named styles of keys.
    - This includes some with enough detail on the keycaps to permit printing.
      These printable models are now among the outputs of the application.
- Improved case `tweaks`.
    - Tweaks are no longer restricted to key aliases.
        - Any named feature can be used in a leaf node.
        - It is no longer necessary to specify a corner or segment for a tweak.
    - Tweaks can now target bottom plates without impacting case walls.
      There is a new configuration key for this (`above-ground`, which must be
      turned off to see the new behaviour).
    - Added a `secondaries` parameter for named features that are just points
      in space, placed in relation to other named features. Used in `tweaks`,
      these secondaries give greater freedom in shaping the case.
- Added a `resolution` section to the parameters.
    - By default, already-existing resolution parameters (both of them are for
      wrist-rest pads) will be disabled by a default-negative new `include`
      parameter in the new section.
    - The new section also provides a means of rendering curved surfaces
      elsewhere in more detail.
- Added a `thickness` parameter specific to the threaded anchors for
  bottom-plate screws.

### Fixed
- Improved the fit of a bottom plate for the case, at the cost of
  greater rendering complexity.
- Reduced risk and impact of collision between nut bosses built into the
  rear housing and the interior negative of the socket for connecting the
  two halves, by reducing the thickness of one part of the negative.

### Developer
- Took advantage of new developments in general-purpose libraries:
    - Outsourced file authoring to `scad-app` for improved CPU thread scaling,
      rendering feedback and face-size constraints.
    - `scad-tarmi` lofting replaced `pairwise-hulls` and `triangle-hulls`.
    - `scad-tarmi` flex functions obviated several separate functions for
      object placement and reasoning about that placement.
    - `scad-tarmi` coordinate specs replaced locals.
    - Featureful `dmote-keycap` models replaced internal maquettes.
- Made the rosters of models and module definitions reactive to the
  configuration.
    - Converted keycaps and switches to OpenSCAD modules.
- Changed the merge order in the `reckon-from-anchor` function to make
  secondaries useful in tweaks.

## [Version 0.3.0] - 2019-02-18
### Changed
- Moved and replaced some options:
    - Dimensions of `keycaps` have moved into nestable `parameters` under
      `by-key`.
    - `key-alias` settings have been merged into `anchor`. `anchor` can now
      refer to a variety of features either by alias or by a built-in and
      reserved name like `rear-housing` or `origin`. In some cases, it is now
      possible to anchor features more freely as a result.
    - Moved `case` → `rear-housing` → `offsets` into
      `case` → `rear-housing` → `position`.
    - Moved `case` → `rear-housing` → `distance` into
      `case` → `rear-housing` → `position` → `offsets` as `south`.
    - Renamed the `key-corner` input of `case` → `foot-plates` → `polygons`
      to `corner`.
    - Removed the option `case` → `rear-housing` → `west-foot` in favour of
      more general `foot-plates` functionality.
    - Removed `wrist-rest` → `shape` → `plinth-base-size` in favour of settings
      (in a new `spline` section) that do not restrict you to a cuboid.
    - Removed `wrist-rest` → `shape` → `chamfer`. You can achieve the old
      chamfered, boxy look by setting spline resolution to 1 and manually
      positioning the corners of the wrist rest for it.
    - Moved `wrist-rest` → `shape` → `lip-height` to
      `wrist-rest` → `shape` → `lip` → `height`.
    - Moved `wrist-rest` → `shape` → `pad` → `surface-heightmap`
      to `wrist-rest` → `shape` → `pad` → `surface` → `heightmap` → `filepath`.
    - Substantial changes to `wrist-rest` → `fasteners`, which has been castled
      to `wrist-rest` → `mounts` and is now a list.
- Removed the implementation of the `solid` style of wrist rest attachment.
  This was reimplemented in version 0.5.0.
- Removed the option `wrist-rest` → `fasteners` → `mounts` → `plinth-side` →
  `pocket-scale`, obviated by a new generic `dfm` feature.
- Renamed the ‘finger’ key cluster to ‘main‘.
- As a side effect of outsourcing the design of threaded fasteners to
  `scad-tarmi`, the `flat` style of bolt head has been renamed to
  the more specific `countersunk`.
- Removed `create-models.sh`, adding equivalent functionality to the Clojure
  application itself (new flags: `--render`, `--renderer`).
- Added intermediate `scad` and `stl` folders under `things`.
- Split generated documentation (`options.md`) into four separate documents
  (`options-*.md`).

### Added
- This log.
- Support for generating a bottom plate that closes the case.
    - This includes support for a separate plate for the wrist rest, and a
      combined plate that joins the two models.
- Improvements to wrist rests.
    - Arbitrary polygonal outlines and vertically rounded edges, without a
      height map.
    - Tilting.
    - Support for placing wrist rests in relation to their point
      of attachment to the case using a new `anchoring` parameter.
    - Support for multiple mount points.
    - Support for naming the individual blocks that anchor a wrist rest.
    - Support for placing wrist rests in relation to a specific corner of a key.
      In the previous version, the attachment would be to the middle of the key.
    - Parametrization of mould wall thickness.
    - Parametrization of sprues.
- Support for naming your key clusters much more freely, and/or adding
  additional clusters. Even the new ‘main’ cluster is optional.
    - Support for a `cluster` parameter to `case` → `rear-housing` →
      `position`. The rear housing would previously be attached to the finger
      cluster.
    - Support for a `cluster` parameter to `case` → `leds` → `position`.
      LEDs would previously be attached to the finger cluster.
    - Support for anchoring any cluster to any other, within logical limits.
- Parametrization of keycap sizes, adding support for sizes other than 1u in
  both horizontal dimensions, as well as diversity in keycap height and
  clearance.
- Support for a filename whitelist in the CLI.
- Support for placing `foot-plates` in relation to objects other than keys.
- Support for generic compensation for slicer and printer inaccuracies in the
  xy plane through a new option, `dfm` → `error`.

### Fixed
- Improved support for Windows by using `clojure.java.io/file` instead of
  string literals with Unix-style file-path separators.
- Strengthened parameter validation for nested sections.

### Developer
- Significant restructuring of the code base for separation of concerns.
    - Switched to docstring-first function definitions.
    - Shifted more heavily toward explicit namespacing and took the opportunity
      to shorten some function names in the matrix module and elsewhere.
- Added a dependency on `scad-tarmi` for shorter OpenSCAD code and more
  capable models of threaded fasteners.
- Rearranged derived parameter structure somewhat to support arbitrary key
  clusters and the use of aliases for more types of objects (other than keys).
- Removed the `new-scad` function without replacement.
- Removed a dependency on `unicode-math`. The requisite version of the library
  had not been deployed to Clojars and its use was cosmetic.

[Unreleased]: https://github.com/veikman/dactyl-keyboard/compare/dmote-v0.5.1...HEAD
[Version 0.5.1]: https://github.com/veikman/dactyl-keyboard/compare/dmote-v0.5.0...dmote-v0.5.1
[Version 0.5.0]: https://github.com/veikman/dactyl-keyboard/compare/dmote-v0.4.0...dmote-v0.5.0
[Version 0.4.0]: https://github.com/veikman/dactyl-keyboard/compare/dmote-v0.3.0...dmote-v0.4.0
[Version 0.3.0]: https://github.com/veikman/dactyl-keyboard/compare/dmote-v0.2.0...dmote-v0.3.0
