#!/usr/bin/env bash
set -eo pipefail
 
export VERSION=$(./get-version.sh)

docker run --rm -v "$PWD:/conjurinc/api-java" -e VERSION -w /conjurinc/api-java maven:3-jdk-8 /bin/bash -c \
"mvn -X -e clean package -Dmaven.test.skip=true"
