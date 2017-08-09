#!/usr/bin/env bash

function finish {
  docker-compose down -v
}
trap finish EXIT

echo "Running tests"

# Clean then generate output folder locally
rm -rf output
mkdir -p output

# Build test container & start the cluster
docker-compose pull postgres possum
docker-compose build --pull
docker-compose up -d

# Delay to allow time for Possum to come up
# TODO: remove this once we have HEALTHCHECK in place
docker-compose run --rm test ./wait_for_server.sh

# get possum container id
possum_cid=$(docker-compose ps -q possum)
#
# copy test-policy into a /tmp/test-policy within the possum container
docker cp test-policy $possum_cid:/tmp
# run your policies from within the possum container
docker exec -i $possum_cid  /bin/bash -c "conjurctl policy load root /tmp/test-policy/root.yml &&
    conjurctl policy load cucumber /tmp/test-policy/cucumber.yml"

api_key=$(docker-compose exec -T possum rails r "print Credentials['cucumber:user:admin'].api_key")

# Execute tests
docker-compose run --rm -e CONJUR_CREDENTIALS="admin:$api_key" test bash