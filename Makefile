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

doc/img/butty/base.png: target/dmote.jar config/base.yaml resources/butty/config/02.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/02.yaml
	openscad -o $@ --camera 0,0,8,50,0,50,70 --imgsize 400,260 --render 1 things/scad/body-main.scad

doc/img/butty/bevel.png: target/dmote.jar config/base.yaml resources/butty/config/12.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/12.yaml
	openscad -o $@ --camera 0,0,6,50,0,50,70 --imgsize 400,260 --render 1 things/scad/body-main.scad

doc/img/butty/to-ground.png: target/dmote.jar config/base.yaml resources/butty/config/14.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/14.yaml
	openscad -o $@ --camera 0,0,3,50,0,50,80 --imgsize 400,330 --render 1 things/scad/body-main.scad

doc/img/butty/open-back-front.png: target/dmote.jar config/base.yaml resources/butty/config/16.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/16.yaml
	openscad -o $@ --camera 0,0,3,50,0,50,80 --imgsize 400,330 --render 1 things/scad/body-main.scad

doc/img/butty/open-back-rear.png: target/dmote.jar config/base.yaml resources/butty/config/16.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/16.yaml
	openscad -o $@ --camera 0,0,6,120,0,150,78 --imgsize 400,300 --render 1 things/scad/body-main.scad

doc/img/butty/rear-housing.png: target/dmote.jar config/base.yaml resources/butty/config/22.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/22.yaml
	openscad -o $@ --camera 12,0,17,50,0,40,100 --imgsize 500,400 --render 1 things/scad/body-main.scad

doc/img/butty/mcu-1-default.png: target/dmote.jar config/base.yaml resources/butty/config/24.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/24.yaml
	openscad -o $@ --camera 8,0,13,55,0,35,90 --imgsize 500,400 --render 1 things/scad/body-main.scad

doc/img/butty/mcu-2-preview.png: target/dmote.jar config/base.yaml resources/butty/config/26.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/26.yaml
	openscad -o $@ --camera 0,0,0,60,0,30,130 --imgsize 500,420 --render 1 things/scad/body-main.scad

doc/img/butty/mcu-3-inplace.png: target/dmote.jar config/base.yaml resources/butty/config/28a.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/28a.yaml
	openscad -o $@ --camera 10,0,-11,135,0,35,100 --imgsize 500,420 --render 1 things/scad/body-main.scad

doc/img/butty/mcu-4-port.png: target/dmote.jar config/base.yaml resources/butty/config/28b.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/28b.yaml
	openscad -o $@ --camera -9,0,-8,70,0,165,110 --imgsize 500,440 --render 1 things/scad/body-main.scad

doc/img/butty/tweak-roof-1.png: target/dmote.jar config/base.yaml resources/butty/config/32.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/32.yaml
	openscad -o $@ --camera 0,14,3,70,0,90,45 --imgsize 500,500 --render 1 things/scad/body-main.scad

doc/img/butty/tweak-roof-2.png: target/dmote.jar config/base.yaml resources/butty/config/34a.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/34a.yaml
	openscad -o $@ --camera 0,14,6,70,0,90,30 --imgsize 500,280 --render 1 things/scad/body-main.scad

doc/img/butty/tweak-roof-3.png: target/dmote.jar config/base.yaml resources/butty/config/34b.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/34b.yaml
	openscad -o $@ --camera 0,14,6,70,0,90,30 --imgsize 500,280 --render 1 things/scad/body-main.scad

doc/img/butty/tweak-roof-4.png: target/dmote.jar config/base.yaml resources/butty/config/34c.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/34c.yaml
	openscad -o $@ --camera 0,14,6,70,0,90,30 --imgsize 500,280 --render 1 things/scad/body-main.scad

doc/img/butty/tweak-roof-5-side.png: target/dmote.jar config/base.yaml resources/butty/config/34d.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/34d.yaml
	openscad -o $@ --camera 0,14,6,70,0,90,30 --imgsize 500,280 --render 1 things/scad/body-main.scad

doc/img/butty/tweak-roof-5-bottom.png: target/dmote.jar config/base.yaml resources/butty/config/34d.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/34d.yaml
	openscad -o $@ --camera 0,14,6.5,100,0,90,36 --imgsize 500,400 --render 1 things/scad/body-main.scad

doc/img/butty/tweak-roof-6-bottom.png: target/dmote.jar config/base.yaml resources/butty/config/34e.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/34e.yaml
	openscad -o $@ --camera 0,14,6.5,100,0,90,36 --imgsize 500,400 --render 1 things/scad/body-main.scad

doc/img/butty/tweak-wall-side.png: target/dmote.jar config/base.yaml resources/butty/config/36.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/36.yaml
	openscad -o $@ --camera 8,0,13,55,0,35,90 --imgsize 500,400 --render 1 things/scad/body-main.scad

doc/img/butty/tweak-wall-bottom.png: target/dmote.jar config/base.yaml resources/butty/config/36.yaml
	java -jar target/dmote.jar -c config/base.yaml -c resources/butty/config/36.yaml
	openscad -o $@ --camera 10,0,-11,135,0,35,100 --imgsize 500,420 --render 1 things/scad/body-main.scad

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
	cat resources/butty/doc/artefact_alert.md \
			resources/butty/doc/01.md \
			resources/butty/config/02.yaml \
			resources/butty/doc/03.md \
			resources/butty/config/04.yaml \
			resources/butty/doc/05.md \
			> $@

doc/tutorial-1b.md: resources/butty/*/*
	cat resources/butty/doc/artefact_alert.md \
			resources/butty/doc/11.md \
			resources/butty/config/12.yaml \
			resources/butty/doc/13.md \
			resources/butty/config/14.yaml \
			resources/butty/doc/15.md \
			resources/butty/config/16.yaml \
			resources/butty/doc/17.md \
			> $@

doc/tutorial-1c.md: resources/butty/*/*
	! diff resources/butty/config/22.yaml resources/butty/config/24.yaml --unchanged-line-format='' --new-line-format='%L' > /tmp/24.yaml
	! diff resources/butty/config/22.yaml resources/butty/config/26.yaml --unchanged-line-format='' --new-line-format='%L' > /tmp/26.yaml
	! diff resources/butty/config/22.yaml resources/butty/config/28a.yaml --unchanged-line-format='' --new-line-format='%L' > /tmp/28.yaml
	cat resources/butty/doc/artefact_alert.md \
			resources/butty/doc/21.md \
			resources/butty/config/22.yaml \
			resources/butty/doc/23.md \
			/tmp/24.yaml \
			resources/butty/doc/25.md \
			/tmp/26.yaml \
			resources/butty/doc/27.md \
			/tmp/28.yaml \
			resources/butty/doc/29.md \
			> $@

doc/tutorial-1d.md: resources/butty/*/*
	! diff resources/butty/config/28b.yaml resources/butty/config/32.yaml --unchanged-line-format='' --new-line-format='%L' > /tmp/32.yaml
	! diff resources/butty/config/32.yaml resources/butty/config/34a.yaml --unchanged-line-format='' --new-line-format='%L' > /tmp/34a.yaml
	! diff resources/butty/config/32.yaml resources/butty/config/34b.yaml --unchanged-line-format='' --new-line-format='%L' > /tmp/34b.yaml
	! diff resources/butty/config/32.yaml resources/butty/config/34c.yaml --unchanged-line-format='' --new-line-format='%L' > /tmp/34c.yaml
	! diff resources/butty/config/32.yaml resources/butty/config/34d.yaml --unchanged-line-format='' --new-line-format='%L' > /tmp/34d.yaml
	! diff resources/butty/config/32.yaml resources/butty/config/34e.yaml --unchanged-line-format='' --new-line-format='%L' > /tmp/34e.yaml
	! diff resources/butty/config/34e.yaml resources/butty/config/36.yaml --unchanged-line-format='' --new-line-format='%L' > /tmp/36.yaml
	cat resources/butty/doc/artefact_alert.md \
			resources/butty/doc/31.md \
			resources/yamlblock_begin.md \
			resources/butty/config/32.yaml \
			resources/yamlblock_end.md \
			resources/butty/doc/33a.md \
			resources/yamlblock_begin.md /tmp/32.yaml resources/yamlblock_end.md \
			resources/butty/doc/33b.md \
			resources/yamlblock_begin.md /tmp/34a.yaml resources/yamlblock_end.md \
			resources/butty/doc/35a.md \
			resources/yamlblock_begin.md /tmp/34b.yaml resources/yamlblock_end.md \
			resources/butty/doc/35b.md \
			resources/yamlblock_begin.md /tmp/34c.yaml resources/yamlblock_end.md \
			resources/butty/doc/35c.md \
			resources/yamlblock_begin.md /tmp/34d.yaml resources/yamlblock_end.md \
			resources/butty/doc/35d.md \
			resources/yamlblock_begin.md /tmp/34e.yaml resources/yamlblock_end.md \
			resources/butty/doc/35e.md \
			resources/yamlblock_begin.md /tmp/36.yaml resources/yamlblock_end.md \
			resources/butty/doc/37.md \
			> $@

docs: doc-images doc/options-central.md doc/options-clusters.md doc/options-main.md doc/options-nested.md doc/options-ports.md doc/options-wrist-rest-mounts.md doc/tutorial-1a.md doc/tutorial-1b.md doc/tutorial-1c.md doc/tutorial-1d.md

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
