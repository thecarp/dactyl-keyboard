# syntax = docker/dockerfile:1.2

#FROM --platform=${BUILDPLATFORM} clojure:openjdk-19-lein-alpine AS base
FROM clojure:openjdk-19-lein AS base
RUN --mount=type=cache,target=/var/cache/apt 
RUN apt-get update
RUN apt-get install -y make openscad
RUN mkdir /things

FROM base as build-base
COPY . /usr/src/app

FROM build-base as build
WORKDIR /usr/src/app
#CMD ["lein", "run", "-c", "config/base.yaml", "-c", "config/dmote/base.yaml", "-c", "config/dmote/mx.yaml"]
RUN \
#    --mount=type=cache,target=/usr/src/app \
    lein run -c config/base.yaml -c config/dmote/base.yaml -c config/dmote/mx.yaml

FROM scratch as scad
COPY --from=build /usr/src/app/things/scad/*.scad /things/scad/
