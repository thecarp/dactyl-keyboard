# GNU makefile. https://www.gnu.org/software/make/manual/make.html

# By default, CONFDIR is the bundled configuration file directory.
CONFDIR ?= config/

# Real prior artefacts are charted as this makefile is parsed.
YAML := $(shell find $(CONFDIR) -name '*.yaml')
SOURCECODE := $(shell find src -type f)

# YAML files are not made from here but are treated as targets anyway.
# This is a means of activating them by naming them as CLI arguments.
.PHONY: $(YAML) dmote_62key macropad_12key vis low mutual caseside all doc-images docs test clean

# CONFFILES is a space-separated array of relative paths to selected
# YAML files, starting with a near-neutral base.
CONFFILES := $(CONFDIR)base.yaml

# TO_SCAD evaluates to a Java CLI call.
TO_SCAD = java -jar target/dmote.jar $(foreach FILE,$(CONFFILES),-c $(FILE))

# The append_config function is what adds (more) YAML filepaths to CONFFILES.
# If not already present in the path, CONFDIR will be prepended to each path.
# This will break if CONFDIR is duplicated in the argument.
define append_config
	$(eval CONFFILES += $(CONFDIR)$(subst $(CONFDIR),,$$1))
endef

# Targets and their recipes follow.

# The %.yaml pattern target ensures that each YAML file named as a target,
# including each prerequisite named below, is appended to CONFFILES.
%.yaml:
	$(call append_config,$@)

# The dmote_62key target, acting as the default, builds SCAD files.
# When resolved, its recipe constructs a Java command where each
# selected configuration file gets its own -c parameter.
dmote_62key: target/dmote.jar dmote/base.yaml
	$(TO_SCAD)

concertina_64key: target/dmote.jar concertina/base.yaml concertina/assortment/base.yaml concertina/assortment/reset.yaml concertina/assortment/magnets/slits.yaml concertina/assortment/magnets/cylinder5x2p5_centre.yaml
	$(TO_SCAD)

# A corresponding target approximating Tom Short’s Dactyl-ManuForm.
dactylmanuform_46key: target/dmote.jar dactyl_manuform/base.yaml
	$(TO_SCAD)

# A corresponding target for a relatively simple 12-key macropad.
macropad_12key: target/dmote.jar macropad/base.yaml
	$(TO_SCAD)

# Curated shorthand for configuration fragments. These run no shell commands.
vis: visualization.yaml
low: low_resolution.yaml
mutual: dmote/wrist/threaded_mutual.yaml
caseside: dmote/wrist/threaded_caseside.yaml

# The remainder of this file describes more typical Make work, starting with
# the compilation of the Clojure application into a Java .jar, specific
# pieces of documentation, and illustrations for documentation.

target/dmote.jar: $(SOURCECODE)
	lein uberjar

doc/img/butty/bare.png: target/dmote.jar
	java -jar target/dmote.jar
	openscad -o $@ --imgsize 300,200 things/scad/body-main.scad

doc/img/butty/min.png: target/dmote.jar resources/butty/config/02.yaml
	java -jar target/dmote.jar -c resources/butty/config/02.yaml
	openscad -o $@ --camera 0,0,8,50,0,50,70 --imgsize 400,260 --render 1 things/scad/body-main.scad

doc/img/butty/base.png: target/dmote.jar resources/butty/config/02.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/02.yaml
	openscad -o $@ --camera 0,0,8,50,0,50,70 --imgsize 400,260 --render 1 things/scad/body-main.scad

doc-images: doc/img/*/*

doc/options-main.md: target/dmote.jar
	java -jar target/dmote.jar --describe-parameters main > $@

doc/options-central.md: target/dmote.jar
	java -jar target/dmote.jar --describe-parameters central > $@

doc/options-clusters.md: target/dmote.jar
	java -jar target/dmote.jar --describe-parameters clusters > $@

doc/options-nested.md: target/dmote.jar
	java -jar target/dmote.jar --describe-parameters nested > $@

doc/options-ports.md: target/dmote.jar
	java -jar target/dmote.jar --describe-parameters ports > $@

doc/options-wrist-rest-mounts.md: target/dmote.jar
	java -jar target/dmote.jar --describe-parameters wrist-rest-mounts > $@

doc/tutorial-1a.md: resources/butty/*/*
	echo "<!--This document was generated. Edit the source files under “resources/butty”, not this file.-->" > $@
	cat resources/butty/doc/01.md \
			resources/butty/config/02.yaml \
			resources/butty/doc/03.md \
			resources/butty/config/04.yaml \
			resources/butty/doc/05.md \
			>> $@

docs: doc-images doc/options-central.md doc/options-clusters.md doc/options-main.md doc/options-nested.md doc/options-ports.md doc/options-wrist-rest-mounts.md doc/tutorial-1a.md

test:
	lein test

# The “all” target is intended for code sanity checking before pushing a commit.
all: test docs
	lein run -c test/config/central_housing_1.yaml
	lein run -c test/config/mount_types.yaml
	make vis mutual dmote_62key

clean:
	-rm things/scad/*.scad
	-rmdir things/scad/
	-rm things/stl/*.stl
	-rmdir things/stl/
	lein clean
