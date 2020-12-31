# Execution guide

This document describes multiple ways to run the DMOTE application.
Please read the [introduction](intro.md) first.

## Method 1: GNU Make

Automation using [GNU Make](https://www.gnu.org/software/make/) has both pros
and cons:

* Pro: Convenient for a small set of bundled designs.
* Pro: Fast iterations when you change a configuration.
* Con: You need Make.
* Con: No advanced selection of configuration files.
* Con: Slow iterations when you change application source code.

### Examples

To produce OpenSCAD files for the Dactyl-ManuForm, just run `make` in your
terminal, from the project folder.

To produce a Concertina instead of a Dactyl-ManuForm, run `make
concertina_64key` instead of `make`. There is a very short list of such
canonical build targets in the `Makefile`. A similarly short list of modifiers,
such as `vis` for visualization, can be mixed in, e.g. `make vis concertina_64key`.

Make shows you each command it runs, so you can see which YAML files are used
for each target, and in what order. However, arbitrary YAML files cannot be
mixed in by this method.

### Further automation

The `transpile.sh` shell script in the project root uses Make as well as
`inotify` to automatically retrigger Make on a named target when you save
changes to the configuration or application. `transpile.sh` can also send build
artefacts to a render farm with `rsync`. This may be convenient on Linux but
is not very portable.

## Method 2: Running with Leiningen

[Leiningen](https://leiningen.org/) is an automation tool used to manage the
DMOTE project. You can use it to run the application as a one-off CLI program.

* Pro: Portable. Pure Clojure.
* Pro: Easy configuration selection.
* Pro: Easy rendering to STL. It’s just an extra flag.
* Pro: Faster than Make when you’re changing source code.
* Con: Slower than Make when you’re only changing configuration files.

Note: The default configuration with Leiningen is built into the application
itself and is not the same as you get with a plain `make`. This means that
although free configuration selection is easier, it is marginally harder to
just look at bundled designs.

### Examples

For OpenSCAD files identical to what you get with a plain `make`, run `lein run
-c config/base.yaml -c config/dactyl_manuform/base.yaml`. To render to STL
automatically, add the `--render` flag.

Notice that you are naming specific YAML files and combining them into one
configuration, in the order of your choice. This gives you full control over
the composite configuration so you can more easily work with your own original
projects.

Put your own file(s) last to get the most power. For example, to override all
the ALPS-style key mounts with MX-style mounts on a DMOTE, call `lein run -c
config/base.yaml -c config/dmote/base.yaml -c config/dmote/mx.yaml`.

## Method 3: REPL

The [REPL](https://clojure.org/guides/repl/introduction) is a command line
within the application itself.

* Pro: Portable. You can get a REPL with Leiningen.
* Pro: Fast iterations even if you’re changing source code.
* Pro: Possible integration with your text editor.
* Con: Hard.

### Examples

Run `lein repl` and work interactively from there.

Let’s suppose you’re editing the sandbox module
(`src/dactyl_keyboard/sandbox.clj`) in another window. To reload the module
into the running application and then use it to build the bundled DMOTE
configuration without having to restart anything, enter these two lines at the
REPL prompt:

```clojure
(use 'dactyl-keyboard.sandbox :reload)
(run {:configuration-file ["config/base.yaml" "config/dmote/base.yaml"]})
```

Notice that `:configuration-file` corresponds directly to the `-c` flag used
with `lein run`. `-c` is short for `--configuration-file` and appends its
argument to the vector you recreate manually at the REPL.
