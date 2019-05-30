#!/usr/bin/env bash
set -ex

function finish {
  echo '-----------------------test.sh------------------------------'
  echo 'Removing test environment'
  echo '------------------------------------------------------------'
  docker-compose down -v
}

trap finish EXIT

function main() {
  runDAP
  runOSS
}

# Run DAP Enterprise test suite
function runDAP() {
  createDAPTestEnvironment
  loadDAPTestPolicy
  initializeDAPCert
  runDAPTests
}

# Run OSS test suite
function runOSS () {
  createOSSTestEnvironment
  loadOSSTestPolicy
  runOSSTests
  printOSSProxyConfiguration
  initializeOSSCert
  runOSSHTTPSTests
}

# Build DAP test container & start the cluster
function createDAPTestEnvironment() {
  docker-compose build --pull client cuke-master test-dap
  export CONJUR_APPLIANCE_URL="https://cuke-master"
  docker-compose up -d client cuke-master test-dap

  # Delay to allow time for conjur to come up
  echo 'Waiting for conjur server to be healthy'
  docker-compose run --rm test ./wait_for_server.sh
}

function loadDAPTestPolicy() {
  echo '-----------------------test.sh------------------------------'
  echo "Loading DAP test policy"
  echo '------------------------------------------------------------'

  dap_client_cid=$(docker-compose ps -q client)

  # Get certificate from cuke-master
  ssl_cert=$(docker-compose exec -T cuke-master cat /opt/conjur/etc/ssl/conjur.pem)

  docker exec \
    -e CONJUR_SSL_CERTIFICATE="$ssl_cert" \
    ${dap_client_cid} conjur authn login -u admin -p secret

  # copy test-policy into a /tmp/test-policy within the client container
  docker cp test-policy ${dap_client_cid}:/tmp

  docker exec \
    -e CONJUR_SSL_CERTIFICATE="$ssl_cert" \
    ${dap_client_cid} conjur policy load root /tmp/test-policy/root.yml
}

function initializeDAPCert() {
  echo '-----------------------test.sh------------------------------'
  echo "Fetch certificate for DAP using client cli"
  echo '------------------------------------------------------------'

  dap_client_cid=$(docker-compose ps -q client)

  # Get the pem file from conjur server
  CONJUR_ACCOUNT="cucumber"
  CONJUR_PROXY="https://cuke-master"

  echo "remove old pem file"
  rm -rf /test-cert/*

  echo "fetch pem file from enterprise server"
  exec_command="echo yes | conjur init -u '${CONJUR_PROXY}' -a '${CONJUR_ACCOUNT}'"
  docker exec ${dap_client_cid} /bin/bash -c "$exec_command"

  echo "convert pem to der file and copy it to share memory"
  convert_command="openssl x509 -outform der -in /root/conjur-cucumber.pem -out /test-cert/conjur-cucumber.der"
  docker exec ${dap_client_cid} ${convert_command}

  echo "import cert inside DAP test container"
  dap_test_cid=$(docker-compose ps -q test-dap)

  # Import cert converted above into keystore
  JRE_HOME='/usr/local/openjdk-8/jre'
  import_command="keytool \
     -import \
     -alias cuke-master -v \
     -trustcacerts \
     -noprompt \
     -keystore $JRE_HOME/lib/security/cacerts \
     -file /test-cert/conjur-cucumber.der -storepass changeit"
  docker exec ${dap_test_cid} ${import_command}
}

function runDAPTests() {
  echo '-----------------------test.sh------------------------------'
  echo "Running DAP tests"
  echo '------------------------------------------------------------'

  dap_test_cid=$(docker-compose ps -q test-dap)

  api_key_admin=$(docker-compose exec -T client conjur user rotate_api_key)

  # Execute DAP tests
  docker exec \
  -e CONJUR_AUTHN_API_KEY=${api_key_admin} \
  -e CONJUR_AUTHN_LOGIN="admin" \
  ${dap_test_cid} \
    mvn test
}

function createOSSTestEnvironment() {
  echo '-----------------------test.sh------------------------------'
  echo "Creating OSS test environment"
  echo '------------------------------------------------------------'

  # Build OSS test container & start the cluster
  docker-compose build --pull client conjur postgres test test-https conjur-proxy-nginx
  export CONJUR_APPLIANCE_URL="http://conjur:3000"
  docker-compose up -d client conjur postgres test-https

  # Delay to allow time for conjur to come up
  # TODO: remove this once we have HEALTHCHECK in place
  echo 'Waiting for conjur server to be healthy'
  docker-compose run --rm test ./wait_for_server.sh
}

function loadOSSTestPolicy() {
  echo '-----------------------test.sh------------------------------'
  echo "Loading OSS test policy"
  echo '------------------------------------------------------------'

  conjur_client_cid=$(docker-compose ps -q client)

  api_key=$(docker-compose exec -T conjur rails r "print Credentials['cucumber:user:admin'].api_key")

  # Copy test-policy into a /tmp/test-policy within the possum container
  docker cp test-policy ${conjur_client_cid}:/tmp

  docker exec -e CONJUR_AUTHN_API_KEY=${api_key} \
    ${conjur_client_cid} \
    conjur policy load root /tmp/test-policy/root.yml
}

function printOSSProxyConfiguration() {
  echo '-----------------------test.sh------------------------------'
  echo "Print Nginx proxy server configuration"
  echo '------------------------------------------------------------'
  echo 'Note: in order to change configuration, you need to edit the file: default.conf '

  conjur_proxy_cid=$(docker-compose ps -q conjur-proxy-nginx)

  exec_command='nginx-debug -T'
  docker exec ${conjur_proxy_cid} ${exec_command}
}

function initializeOSSCert() {
  echo '-----------------------test.sh------------------------------'
  echo "Fetch certificate for OSS using client cli"
  echo '------------------------------------------------------------'

  conjur_client_cid=$(docker-compose ps -q client)

  # Get the pem file from conjur server
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

  # Import cert converted above into keystore
  JRE_HOME='/usr/local/openjdk-8/jre'
  import_command="keytool \
    -import \
    -alias cucumber \
    -v -trustcacerts \
    -noprompt \
    -keystore $JRE_HOME/lib/security/cacerts \
    -file /test-cert/conjur-cucumber.der -storepass changeit"
  docker exec ${conjur_test_cid} ${import_command}
}


function runOSSTests() {
  echo '-----------------------test.sh------------------------------'
  echo "Running tests"
  echo '------------------------------------------------------------'

  conjur_client_cid=$(docker-compose ps -q client)

  api_key_admin=$(docker-compose exec -T conjur rails r "print Credentials['cucumber:user:admin'].api_key")

  # Execute OSS tests
  docker-compose run --rm \
    -e CONJUR_AUTHN_LOGIN="admin" \
    -e CONJUR_AUTHN_API_KEY="$api_key_admin" \
    test \
      bash -c "mvn test"
}

function runOSSHTTPSTests() {
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