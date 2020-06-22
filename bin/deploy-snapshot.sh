#!/bin/bash
set -exo pipefail

# Deploy snapshot to Sonatype OSSRH (OSS Repository Hosting)
# Setup: Import our GPG key and passphrase
# 1. Deploy to snapshot repository of OSSRH
#
# Note: Snapshot releases do not need to meet Maven central requirements,
# but it is best to do so whenever possible
docker run --rm \
  -e OSSRH_USERNAME \
  -e OSSRH_PASSWORD \
  -v "$PWD:/cyberark/conjur-java-api" \
  -v "$GPG_PASSWORD:/gpg_password" \
  -v "$GPG_PRIVATE_KEY:/gpg_key" \
  -w /cyberark/conjur-java-api maven:3-jdk-8 \
  /bin/bash -ec "gpg --batch --passphrase-file /gpg_password --trust-model always --import /gpg_key
                 mvn --settings settings.xml clean deploy -Dmaven.test.skip=true -P ossrh,sign"
