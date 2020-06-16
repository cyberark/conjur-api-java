#!/bin/bash -ex
set -eo pipefail

# Strip the 'v' from the Tag Name
export TAG=${TAG_NAME:1}

# Deploy release to OSSRH
# 1. Set the version in the POM to the Tagged Version
# 2. Sign our build and deploy to OSSRH
# 3. Release our staged deployment
# Note: The autoReleaseAfterClose for the nexus-staging-maven-plugin should be
# set to "false" if we do not want releases published automatically
docker run --rm \
  -e OSSRH_USERNAME \
  -e OSSRH_PASSWORD \
  -v "$PWD:/conjurinc/api-java" \
  -v "$GPG_PASSWORD:/gpg_password" \
  -v "$GPG_PRIVATE_KEY:/gpg_key" \
  -w /conjurinc/api-java maven:3-jdk-8 \
  /bin/bash -c "gpg --batch --passphrase-file /gpg_password --trust-model always --import /gpg_key &&
                mvn versions:set -DnewVersion=${TAG} &&
                mvn --settings settings.xml clean deploy -Dmaven.test.skip=true -P ossrh,sign &&
                mvn nexus-staging:release"
