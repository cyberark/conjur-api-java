#!/bin/bash

set -euo pipefail

# This container is used to package the library - leave the Java version arg at
# 8 to ensure we support Java 8 which has LTS until 2030
docker build \
    --build-arg maven_version=3 \
    --build-arg java_version=8 \
    -t tools \
    -f tools.Dockerfile \
    .
