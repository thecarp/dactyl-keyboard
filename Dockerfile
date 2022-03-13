# syntax = docker/dockerfile:1.2

ARG CONFIG=dmote

# Build Env Setup
FROM clojure:openjdk-19-lein AS base
RUN --mount=type=cache,target=/var/cache/apt 
RUN apt-get update
RUN apt-get install -y make openscad

FROM base as base-src
COPY . /usr/src/app

FROM base-src as base-lein
WORKDIR /usr/src/app
RUN lein deps

# Specific Builds named build-${CONFIG}
FROM base-lein as build-dmote
WORKDIR /usr/src/app
RUN lein run -c config/base.yaml  \
	-c config/dmote/base.yaml \
	-c config/dmote/mx.yaml

FROM base-lein as build-macropad12
WORKDIR /usr/src/app
RUN lein run -c config/base.yaml  \
	-c config/macropad/base.yaml

FROM base-lein as build-concertina64
WORKDIR /usr/src/app
RUN lein run -c config/base.yaml  \
	-c config/concertina/base.yaml \
	-c config/concertina/assortment/base.yaml \
	-c config/concertina/assortment/reset.yaml \
	-c config/concertina/assortment/magnets/slits.yaml \
	-c config/concertina/assortment/magnets/cylinder5x2p5_centre.yaml

# Get output from build
FROM build-${CONFIG} as build

FROM scratch as scad
COPY --from=build /usr/src/app/things/scad/*.scad .
