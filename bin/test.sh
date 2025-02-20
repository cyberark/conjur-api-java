#!/usr/bin/env bash
set -ex
set -o pipefail
source bin/utils.sh

trap finish EXIT

export REGISTRY_URL=${INFRAPOOL_REGISTRY_URL:-"docker.io"}
export JDK_VERSION=${INFRAPOOL_JDK_VERSION:-8}

function main() {
  finish
  runDap
  runOss
}

# Run DAP Enterprise test suite
function runDap() {
  createDAPTestEnvironment
  loadDapTestPolicy
  initializeDapCert
  runDapTests
}

# Run OSS test suite
function runOss () {
  createOssEnvironment
  loadOssPolicy
  runOssTests
  printOssProxyConfiguration
  initializeOssCert
  runOssHttpsTests
}

# Build DAP test container & start the cluster
function createDAPTestEnvironment() {
  docker compose build --pull client cuke-master test-dap --build-arg JDK_VERSION="${JDK_VERSION:-8}"
  export CONJUR_APPLIANCE_URL="https://cuke-master"
  docker compose up -d client cuke-master test-dap

  # Delay to allow time for conjur to come up
  echo 'Waiting for conjur server to be healthy'
  docker compose exec -T cuke-master /opt/conjur/evoke/bin/wait_for_conjur
}

function loadDapTestPolicy() {
  echo '-----------------------test.sh------------------------------'
  echo "Loading DAP test policy"
  echo '------------------------------------------------------------'

  dap_client_cid=$(docker compose ps -q client)

  # Get certificate from cuke-master
  ssl_cert=$(docker compose exec -T cuke-master cat /opt/conjur/etc/ssl/conjur.pem)

  docker exec \
    -e CONJUR_SSL_CERTIFICATE="$ssl_cert" \
    ${dap_client_cid} conjur authn login -u admin -p SEcret12!!!!

  # copy test-policy into a /tmp/test-policy within the client container
  docker cp test-policy ${dap_client_cid}:/tmp

  docker exec \
    -e CONJUR_SSL_CERTIFICATE="$ssl_cert" \
    ${dap_client_cid} conjur policy load root /tmp/test-policy/root.yml
}

function initializeDapCert() {
  echo '-----------------------test.sh------------------------------'
  echo "Fetch certificate for DAP using client cli"
  echo '------------------------------------------------------------'

  dap_client_cid=$(docker compose ps -q client)

  # Get the pem file from conjur server
  CONJUR_ACCOUNT="cucumber"
  CONJUR_PROXY="https://cuke-master"

  echo "remove old pem file"
  rm -rf /test-cert/*

  echo "fetch pem file from enterprise server"
  exec_command="echo yes | conjur init -u '${CONJUR_PROXY}' -a '${CONJUR_ACCOUNT}'"
  docker exec ${dap_client_cid} /bin/bash -c "$exec_command"

  echo "convert pem to der file and copy it to share memory"
  convert_command="openssl x509 \
     -outform der \
     -in /root/conjur-cucumber.pem \
     -out /test-cert/conjur-cucumber.der"
  docker exec ${dap_client_cid} ${convert_command}

  echo "import cert inside DAP test container"
  dap_test_cid=$(docker compose ps -q test-dap)

  # Import cert converted above into keystore
  JAVA_PATH=$(docker exec ${dap_test_cid} sh -c 'echo $JAVA_HOME')

  # If Java 8, append /jre to the path
  JAVA_VERSION=$(docker exec ${dap_test_cid} sh -c '$JAVA_HOME/bin/java -version 2>&1 | head -n 1')
  if [[ "$JAVA_VERSION" == *"1.8"* ]]; then
    JAVA_PATH="${JAVA_PATH}/jre"
  fi
  
  import_command="keytool \
     -import \
     -alias cuke-master -v \
     -trustcacerts \
     -noprompt \
     -keystore "${JAVA_PATH}/lib/security/cacerts" \
     -file /test-cert/conjur-cucumber.der -storepass changeit"
  docker exec ${dap_test_cid} ${import_command}
}

function runDapTests() {
  echo '-----------------------test.sh------------------------------'
  echo "Running DAP tests"
  echo '------------------------------------------------------------'

  dap_test_cid=$(docker compose ps -q test-dap)

  api_key_admin=$(docker compose exec -T client conjur user rotate_api_key)

  # Execute DAP tests
  docker exec \
  -e CONJUR_AUTHN_API_KEY=${api_key_admin} \
  -e CONJUR_AUTHN_LOGIN="admin" \
  ${dap_test_cid} \
    mvn test
}

function runOssTests() {
  echo '-----------------------test.sh------------------------------'
  echo "Running tests"
  echo '------------------------------------------------------------'

  conjur_client_cid=$(docker compose ps -q client)

  api_key_admin=$(docker compose exec -T conjur conjurctl role retrieve-key cucumber:user:admin)

  # Execute OSS tests
  docker compose run --rm \
    -e CONJUR_AUTHN_LOGIN="admin" \
    -e CONJUR_AUTHN_API_KEY="$api_key_admin" \
    test \
      bash -c "mvn test"
}

function runOssHttpsTests() {
  echo '-----------------------test.sh------------------------------'
  echo "Running https tests"
  echo '------------------------------------------------------------'

  api_key_admin=$(docker compose exec -T conjur conjurctl role retrieve-key cucumber:user:admin)
  api_key_alice=$(docker compose exec -T conjur conjurctl role retrieve-key cucumber:user:alice@test)
  api_key_myapp=$(docker compose exec -T conjur conjurctl role retrieve-key cucumber:host:test/myapp)

  echo 'api keys:'
  echo 'user admin api key = ' ${api_key_admin}
  echo 'user alice api key = ' ${api_key_alice}
  echo 'host myapp api key = ' ${api_key_myapp}
  conjur_test_cid=$(docker compose ps -q test-https)
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
