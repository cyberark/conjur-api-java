#!/bin/bash -ex
set -eo pipefail

# Deploy snapshot to OSSRH
docker run --rm \
  -e OSSRH_USERNAME \
  -e OSSRH_PASSWORD \
  -v "$PWD:/conjurinc/api-java" \
  -v "$GPG_PASSWORD:/gpg_password" \
  -v "$GPG_PRIVATE_KEY:/gpg_key" \
  -w /conjurinc/api-java maven:3-jdk-8 \
  /bin/bash -c "gpg --batch --passphrase-file /gpg_password --trust-model always --import /gpg_key &&
                mvn --settings settings.xml clean deploy -Dmaven.test.skip=true -P ossrh,sign"
