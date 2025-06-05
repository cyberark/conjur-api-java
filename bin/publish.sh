#!/usr/bin/env bash

# Publish a pre-release to artifactory

set -euo pipefail

# Load docker_rt function from utils.sh
# shellcheck source=/dev/null
. "$(dirname "${0}")/utils.sh"

# shellcheck disable=SC2012
target_package="$(ls -1tr target/*.jar |tail -n 1)"

# Copy built jar to ASSET_DIR so it will be attached to the Github Release

if [[ -n "${ASSET_DIR:-}" ]] && [[ -d "${ASSET_DIR:-}" ]]; then
    echo "Copying ${target_package} to Asset Dir: ${ASSET_DIR}"
    cp target/*.jar "${ASSET_DIR}"
else
    echo "ASSET_DIR is unset, unable to copy ${target_package} to ASSET_DIR for github release. ❌"
    exit 1
fi

mkdir -p maven_cache

if [[ "${MODE:-}" == "PROMOTE" ]]; then
    echo "PROMOTE build, publishing to internal artifactory and central portal (maven central)"
    maven_profiles="artifactory,central-portal,sign"
else
    echo "Release build, publishing to internal artifactory"
    maven_profiles="artifactory,sign"
fi

docker run \
    -e CENTRAL_PORTAL_USERNAME \
    -e CENTRAL_PORTAL_TOKEN \
    -e JFROG_USERNAME \
    -e JFROG_APIKEY \
    -e JFROG_URL \
    --volume "${PWD}:${PWD}" \
    --volume "${PWD}/maven_cache":/root/.m2 \
    --volume "$GPG_PASSWORD:/gpg_password" \
    --volume "$GPG_PRIVATE_KEY:/gpg_key" \
    --workdir "${PWD}" \
    tools \
        /bin/bash -ec "gpg --batch --passphrase-file /gpg_password --trust-model always --import /gpg_key
                       mvn --batch-mode  --settings settings.xml --file pom.xml deploy -Dmaven.test.skip -P ${maven_profiles}"
