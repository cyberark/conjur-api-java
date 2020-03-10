#!/usr/bin/env bash
set -eo pipefail
LATEST_TAG="$(git describe --abbrev=0 --tags)" #get the most recent git tag
CURRENT_TAG="$(git describe --exact 2> /dev/null || true)" #get the git tag (if any) that matches the current commit
LATEST_VERSION="${LATEST_TAG:1}" #strip the 'v' from the tag name
if [ -n "$CURRENT_TAG" ]; then #when we're building a commit matching the latest tag
    VERSION="$LATEST_VERSION"
else
    VERSION="${LATEST_VERSION}-SNAPSHOT"
fi
echo "$VERSION"
>&2 echo "Version selected: '""$VERSION""'"