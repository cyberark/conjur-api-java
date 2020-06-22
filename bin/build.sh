#!/usr/bin/env bash
set -eo pipefail

docker run --rm \
  -v "$PWD:/cyberark/conjur-java-api" \
  -w /cyberark/conjur-java-api maven:3-jdk-8 \
  /bin/bash -ec "mvn -X -e clean package -Dmaven.test.skip=true"
