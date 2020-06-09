#!/usr/bin/env bash
set -eo pipefail

# Deploy snapshot to OSSRH
docker run --rm \
  -v "$PWD:/conjurinc/api-java" \
  -w /conjurinc/api-java maven:3-jdk-8 \
  /bin/bash -c "mvn -X -e clean deploy -Dmaven.test.skip=true -P ossrh,sign"
