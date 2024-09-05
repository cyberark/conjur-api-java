#!/bin/bash

set -euo pipefail

# Build tools image
#FROM maven:${maven_version}-openjdk-${java_version}-slim
docker build \
    --build-arg maven_version=3.8.4 \
    --build-arg java_version=11 \
    -t tools \
    -f tools.Dockerfile \
    .
