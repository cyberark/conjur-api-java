#!/usr/bin/env bash

# we can build the image every time without wasting too much time
docker build -t conjur-api -f Dockerfile.jenkins .

mkdir -p $PWD/target

summon docker run -t \
    -v $PWD/target:/build/target \
    -e ARTIFACTORY_USERNAME \
    -e ARTIFACTORY_PASSWORD \
    conjur-api:latest
