#!/usr/bin/env bash

function finish {
  echo '-----------------------test.sh------------------------------'
  echo 'Removing test environment'
  echo '------------------------------------------------------------'
  docker-compose down -v
}
trap finish EXIT

function main() {
  prepareOutputDir
  createTestEnvironment
  loadTestPolicy
  runTests
  printProxyConfiguration
  initializeCert
  runHTTPSTests
}

function prepareOutputDir() {
  # Clean then generate output folder locally
  rm -rf output
  mkdir -p output
}

function createTestEnvironment() {
  echo '-----------------------test.sh------------------------------'
  echo "Creating test environment"
  echo '------------------------------------------------------------'

  # Build test container & start the cluster
  docker-compose build --pull client conjur postgres test test-https conjur-proxy-nginx
  docker-compose up -d client conjur postgres test-https

  # Delay to allow time for conjur to come up
  # TODO: remove this once we have HEALTHCHECK in place
  echo 'Waiting for conjur server to be healthy'
  docker-compose run --rm test ./wait_for_server.sh
}

function loadTestPolicy() {
  echo '-----------------------test.sh------------------------------'
  echo "Loading test policy"
  echo '------------------------------------------------------------'

  # get conjur container id
  conjur_client_cid=$(docker-compose ps -q client)

  api_key=$(docker-compose exec -T conjur rails r "print Credentials['cucumber:user:admin'].api_key")

  # copy test-policy into a /tmp/test-policy within the possum container
  docker cp test-policy ${conjur_client_cid}:/tmp

  docker exec -e CONJUR_AUTHN_API_KEY=${api_key} \
    ${conjur_client_cid} \
    /bin/bash -c "conjur policy load root /tmp/test-policy/root.yml"
}

function printProxyConfiguration() {
  echo '-----------------------test.sh------------------------------'
  echo "Print Nginx proxy server configuration"
  echo '------------------------------------------------------------'
  echo 'Note: in order to change configuration, you need to edit the file: default.conf '

  # get conjur client container id
  conjur_proxy_cid=$(docker-compose ps -q conjur-proxy-nginx)
  exec_command='nginx-debug -T'
  docker exec ${conjur_proxy_cid} ${exec_command}

}


function initializeCert() {
  echo '-----------------------test.sh------------------------------'
  echo "Fetch certificate using client cli"
  echo '------------------------------------------------------------'

  # get conjur client container id
  conjur_client_cid=$(docker-compose ps -q client)

  # get the pem file from conjur server
  CONJUR_ACCOUNT="cucumber"
  CONJUR_PROXY="https://conjur-proxy-nginx"

  echo "remove old pem file"
  rm -rf /test-cert/*

  echo "fetch pem file from proxy https server"
  exec_command='echo yes | conjur init -u '${CONJUR_PROXY}' -a '${CONJUR_ACCOUNT}' > tmp.out 2>&1'
  docker exec ${conjur_client_cid} /bin/bash -c "$exec_command"

  echo "print command output"
  print_command="cat tmp.out"
  docker exec ${conjur_client_cid} ${print_command}

  echo "convert pem to der file and copy it to share memory"
  convert_command="openssl x509 -outform der -in /root/conjur-cucumber.pem -out /test-cert/conjur-cucumber.der"
  docker exec ${conjur_client_cid} ${convert_command}

  echo "import cert inside test https container"

  # get conjur test https container id
  conjur_test_cid=$(docker-compose ps -q test-https)

  JRE_HOME='/usr/lib/jvm/java-7-openjdk-amd64/jre'
  import_command="keytool -import -alias cucumber -v -trustcacerts -noprompt \
     -keystore $JRE_HOME/lib/security/cacerts \
     -file /test-cert/conjur-cucumber.der -storepass changeit"
  docker exec ${conjur_test_cid} ${import_command}


}

function runTests() {
  echo '-----------------------test.sh------------------------------'
  echo "Running tests"
  echo '------------------------------------------------------------'

  api_key_admin=$(docker-compose exec -T conjur rails r "print Credentials['cucumber:user:admin'].api_key")

  # Execute tests
  docker-compose run --rm \
    -e CONJUR_AUTHN_LOGIN="admin" \
    -e CONJUR_AUTHN_API_KEY="$api_key_admin" \
    test \
      bash -c "mvn test"



}

function runHTTPSTests() {
  echo '-----------------------test.sh------------------------------'
  echo "Running https tests"
  echo '------------------------------------------------------------'

  api_key_admin=$(docker-compose exec -T conjur rails r "print Credentials['cucumber:user:admin'].api_key")
  api_key_alice=$(docker-compose exec -T conjur rails r "print Credentials['cucumber:user:alice@test'].api_key")
  api_key_myapp=$(docker-compose exec -T conjur rails r "print Credentials['cucumber:host:test/myapp'].api_key")

  echo 'api keys:'
  echo 'user admin api key = ' ${api_key_admin}
  echo 'user alice api key = ' ${api_key_alice}
  echo 'host myapp api key = ' ${api_key_myapp}
  conjur_test_cid=$(docker-compose ps -q test-https)
  tests_command="mvn test"

  echo "Running https tests with admin credentials"
  docker exec \
  -e CONJUR_AUTHN_LOGIN="admin" \
  -e CONJUR_AUTHN_API_KEY="$api_key_admin" \
  ${conjur_test_cid} ${tests_command}

  echo "Running https tests with user credentials"
  docker exec \
  -e CONJUR_AUTHN_LOGIN="alice@test" \
  -e CONJUR_AUTHN_API_KEY="$api_key_alice" \
  ${conjur_test_cid} ${tests_command}

  echo "Running https tests with host credentials"
  docker exec \
  -e CONJUR_AUTHN_LOGIN="host/test/myapp" \
  -e CONJUR_AUTHN_API_KEY="$api_key_myapp" \
  ${conjur_test_cid} ${tests_command}
}

main
