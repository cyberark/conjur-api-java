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
#docker-compose pull postgres possum
docker-compose build --pull
docker-compose up -d

# Delay to allow time for Possum to come up
# TODO: remove this once we have HEALTHCHECK in place
docker-compose run --rm test ./wait_for_server.sh

api_key=$(docker-compose exec -T possum rails r "print Credentials['cucumber:user:admin'].api_key")

# Execute tests
docker-compose run --rm -e CONJUR_CREDENTIALS="admin:$api_key" test bash -c "mvn test"