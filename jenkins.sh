#!/usr/bin/env bash
set -x
registry='registry.tld'
appliance_image="$registry/conjur-appliance-cuke-master:4.6-stable"
appliance_name='cuke-master'
cidfile=tmp/cidfile


function cleanup {
    docker rm -f $(cat $cidfile)
    docker rm -f $appliance_name
    rm $cidfile
}

trap cleanup EXIT

# make a place for the appliance cidfile
mkdir -p tmp

# Start an appliance
docker run --name "$appliance_name" \
    -p 443:443 -p 636:636 -d "$appliance_image"


# we can build the image every time without wasting too much time
docker build -t conjur-api -f Dockerfile.jenkins .

mkdir -p $PWD/target

summon docker run -t \
    -v $PWD/target:/build/target \
    --link "$appliance_name" \
    --cidfile "$cidfile" \
    -e ARTIFACTORY_USERNAME \
    -e ARTIFACTORY_PASSWORD \
    -e CONJUR_APPLIANCE_URL='https://cuke-master/api' \
    -e CONJUR_AUTHN_LOGIN=admin \
    -e CONJUR_AUTHN_API_KEY=secret \
    -e CONJUR_JUNIT_APPLIANCE_AVAILABLE=true \
    -e CONJUR_JAVA_API_DISABLE_HOSTNAME_VERIFICATION=true \
    -e CONJUR_ACCOUNT=cucumber \
    conjur-api:latest
