#!/usr/bin/env bash

function finish {
  echo 'Removing test environment'
  echo '-----'
  docker-compose down -v
}
trap finish EXIT

function main() {
  prepareOutputDir
  createTestEnvironment
  loadTestPolicy
  runTests
}

function prepareOutputDir() {
  # Clean then generate output folder locally
  rm -rf output
  mkdir -p output
}

function createTestEnvironment() {
  echo "Creating test environment"
  echo '-----'

  # Build test container & start the cluster
  # docker-compose pull postgres possum
  docker-compose build --pull test
  docker-compose up -d client possum postgres

  # Delay to allow time for Possum to come up
  # TODO: remove this once we have HEALTHCHECK in place
  echo 'Waiting for possum to be healthy'
  docker-compose run --rm test ./wait_for_server.sh
}

function loadTestPolicy() {
  echo "Loading test policy"
  echo '-----'

  # get possum container id
  possum_client_cid=$(docker-compose ps -q client)

  api_key=$(docker-compose exec -T possum rails r "print Credentials['cucumber:user:admin'].api_key")

  # copy test-policy into a /tmp/test-policy within the possum container
  docker cp test-policy $possum_client_cid:/tmp

  docker exec -e CONJUR_AUTHN_API_KEY=${api_key} \
    $possum_client_cid \
    /bin/bash -c "conjur policy load root /tmp/test-policy/root.yml"
}

function runTests() {
  echo "Running tests"
  echo '-----'

  api_key=$(docker-compose exec -T possum rails r "print Credentials['cucumber:user:admin'].api_key")

  # Execute tests
  docker-compose run --rm \
    -e CONJUR_CREDENTIALS="admin:$api_key" \
    test \
    bash -c "mvn test"
}

main
