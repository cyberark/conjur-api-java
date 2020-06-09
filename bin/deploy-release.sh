#!/usr/bin/env bash
set -eo pipefail

# Strip the 'v' from the Tag Name
export TAG=${TAG_NAME:1}

# Deploy release to OSSRH
# 1. Set the version in the POM to the Tagged Version
# 2. Sign our build and deploy to OSSRH
# 3. Release our staged deployment
# Note: The autoReleaseAfterClose for the nexus-staging-maven-plugin should be
# set to "false" if we do not want releases performed automatically
docker run --rm \
  -v "$PWD:/conjurinc/api-java" \
  -w /conjurinc/api-java maven:3-jdk-8 \
  /bin/bash -c "mvn versions:set -DnewVersion=${TAG} \\
                mvn clean deploy -Dmaven.test.skip=true -P ossrh,sign \\
                mvn nexus-staging:release"
