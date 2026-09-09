#!/usr/bin/env bash

# Publish artifacts to GitHub Releases (always) and Maven Central (PROMOTE only)

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

if [[ "${MODE:-}" == "PROMOTE" ]]; then
    echo "PROMOTE build — publishing signed artifacts to Maven Central"
    mkdir -p maven_cache
    docker run \
        -e CENTRAL_PORTAL_USERNAME \
        -e CENTRAL_PORTAL_TOKEN \
        --volume "${PWD}:${PWD}" \
        --volume "${PWD}/maven_cache":/root/.m2 \
        --volume "$GPG_PASSWORD:/gpg_password" \
        --volume "$GPG_PRIVATE_KEY:/gpg_key" \
        --workdir "${PWD}" \
        tools \
            /bin/bash -ec "MAVEN_GPG_KEY=\$(cat /gpg_key) MAVEN_GPG_PASSPHRASE=\$(cat /gpg_password) mvn --batch-mode --settings settings.xml --file pom.xml deploy -Dmaven.test.skip -P central-portal,sign"
else
    echo "Release build — JAR attached to GitHub release via ASSET_DIR. No Maven deploy needed."
fi
