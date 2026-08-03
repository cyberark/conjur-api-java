#!/usr/bin/env bash
set -ex
set -o pipefail
source bin/utils.sh

trap finish EXIT

export REGISTRY_URL=${INFRAPOOL_REGISTRY_URL:-"docker.io"}
export JDK_VERSION=${INFRAPOOL_JDK_VERSION:-8}
export CLOUD_SUMMARY_PRINTED=0
export AZURE_SUBSCRIPTION_ID="${AZURE_SUBSCRIPTION_ID:-${INFRAPOOL_AZURE_SUBSCRIPTION_ID:-}}"
export AZURE_RESOURCE_GROUP="${AZURE_RESOURCE_GROUP:-${INFRAPOOL_AZURE_RESOURCE_GROUP:-}}"
export USER_ASSIGNED_IDENTITY="${USER_ASSIGNED_IDENTITY:-${INFRAPOOL_USER_ASSIGNED_IDENTITY:-}}"
export USER_ASSIGNED_IDENTITY_CLIENT_ID="${USER_ASSIGNED_IDENTITY_CLIENT_ID:-${INFRAPOOL_USER_ASSIGNED_IDENTITY_CLIENT_ID:-}}"
export GCP_ID_TOKEN="${GCP_ID_TOKEN:-${INFRAPOOL_GCP_ID_TOKEN:-}}"
export GCP_PROJECT_ID="${GCP_PROJECT_ID:-${INFRAPOOL_GCP_PROJECT_ID:-}}"
export AWS_ACCOUNT_ID="${AWS_ACCOUNT_ID:-${INFRAPOOL_AWS_ACCOUNT_ID:-}}"
export AWS_ROLE_NAME="${AWS_ROLE_NAME:-${INFRAPOOL_AWS_ROLE_NAME:-}}"


function printCloudAuthTestSummary() {
  set +x
  echo "---------------- Cloud authenticator test summary ----------------"

  local reports_dir="target/surefire-reports"
  if [[ ! -d "${reports_dir}" ]]; then
    echo "MISSING: ${reports_dir} directory not found"
    echo "-----------------------------------------------------------------"
    set -x
    return 0
  fi

  local matches
  matches=$(
    find "${reports_dir}" -maxdepth 1 -type f -name "TEST-*.xml" | while IFS= read -r report; do
      if grep -Eq 'classname=".*(AzureAuthenticatorTest|AzureAuthenticatorTests|AzureAuthenticatorIntegrationTest|AzureAuthenticatorIntegrationTests|GCPAuthenticatorTest|GCPAuthenticatorTests|GCPAuthenticatorIntegrationTest|GCPAuthenticatorIntegrationTests|AWSIAMAuthenticatorTest|AWSIAMAuthenticatorTests|AWSIAMAuthenticatorIntegrationTest|AWSIAMAuthenticatorIntegrationTests)"' "${report}"; then
        echo "${report}"
      fi
    done
  )

  if [[ -z "${matches}" ]]; then
    echo "MISSING: no Azure/GCP authenticator test reports found"
    echo "Tip: check actual class names and package paths under src/test/java"
  else
    while IFS= read -r report; do
      local suite tests failures errors skipped
      suite=$(sed -n 's/.*name="\([^"]*\)".*/\1/p' "${report}" | head -n1)
      tests=$(sed -n 's/.*tests="\([^"]*\)".*/\1/p' "${report}" | head -n1)
      failures=$(sed -n 's/.*failures="\([^"]*\)".*/\1/p' "${report}" | head -n1)
      errors=$(sed -n 's/.*errors="\([^"]*\)".*/\1/p' "${report}" | head -n1)
      skipped=$(sed -n 's/.*skipped="\([^"]*\)".*/\1/p' "${report}" | head -n1)

      echo "FOUND: ${report}"
      echo "  suite=${suite} tests=${tests} failures=${failures} errors=${errors} skipped=${skipped}"
    done <<< "${matches}"
  fi

  echo "-----------------------------------------------------------------"
  set -x
}

function printCloudAuthTestSummaryOnce() {
  if [[ "${CLOUD_SUMMARY_PRINTED}" -eq 0 ]]; then
    printCloudAuthTestSummary
    CLOUD_SUMMARY_PRINTED=1
  fi
}

function logCloudAuthDiagnostics() {
  set +x
  echo "---------------- Cloud authenticator diagnostics ----------------"

  local azure_file="src/test/java/com/cyberark/conjur/api/clients/AzureAuthenticatorTests.java"
  local gcp_file="src/test/java/com/cyberark/conjur/api/clients/GCPAuthenticatorTests.java"
  local aws_file="src/test/java/com/cyberark/conjur/api/clients/AWSIAMAuthenticatorTests.java"

  for f in "${azure_file}" "${gcp_file}" "${aws_file}"; do
    if [[ -f "${f}" ]]; then
      echo "SOURCE: FOUND ${f}"
      echo "  first_line: $(head -n 1 "${f}")"
      echo "  @Test_count: $(grep -Ec '^[[:space:]]*@Test' "${f}")"
    else
      echo "SOURCE: MISSING ${f}"
    fi
  done

  local reports_dir="target/surefire-reports"
  if [[ ! -d "${reports_dir}" ]]; then
    echo "REPORTS: MISSING ${reports_dir}"
    echo "-----------------------------------------------------------------"
    set -x
    return 0
  fi

  local matches
  matches=$(
    find "${reports_dir}" -maxdepth 1 -type f -name "TEST-*.xml" | while IFS= read -r report; do
      if grep -Eq 'classname=".*(AzureAuthenticatorTest|AzureAuthenticatorTests|AzureAuthenticatorIntegrationTest|AzureAuthenticatorIntegrationTests|GCPAuthenticatorTest|GCPAuthenticatorTests|GCPAuthenticatorIntegrationTest|GCPAuthenticatorIntegrationTests|AWSIAMAuthenticatorTest|AWSIAMAuthenticatorTests|AWSIAMAuthenticatorIntegrationTest|AWSIAMAuthenticatorIntegrationTests)"' "${report}"; then
      echo "${report}"
      fi
    done
  )

  if [[ -z "${matches}" ]]; then
    echo "REPORTS: no Azure/GCP surefire XML matches"
  else
    while IFS= read -r report; do
      local suite tests failures errors skipped
      suite=$(sed -n 's/.*name="\([^"]*\)".*/\1/p' "${report}" | head -n 1)
      tests=$(sed -n 's/.*tests="\([^"]*\)".*/\1/p' "${report}" | head -n 1)
      failures=$(sed -n 's/.*failures="\([^"]*\)".*/\1/p' "${report}" | head -n 1)
      errors=$(sed -n 's/.*errors="\([^"]*\)".*/\1/p' "${report}" | head -n 1)
      skipped=$(sed -n 's/.*skipped="\([^"]*\)".*/\1/p' "${report}" | head -n 1)
      echo "REPORT: ${report}"
      echo "  suite=${suite} tests=${tests} failures=${failures} errors=${errors} skipped=${skipped}"
    done <<< "${matches}"
  fi

  echo "-----------------------------------------------------------------"
  set -x
}

function runCloudAuthenticatorSmokeTests() {
  echo "================ CLOUD AUTH TEST SMOKE START ================"
  logCloudAuthDiagnostics

  local smoke_exit=0
  mvn -DskipTests=true test-compile
  mvn \
    -Dtest=AzureAuthenticatorTests,GCPAuthenticatorTests \
    -DfailIfNoTests=true \
    -DfailIfNoSpecifiedTests=true \
    -Dsurefire.useFile=false \
    -Dsurefire.printSummary=true \
    test || smoke_exit=$?

  logCloudAuthDiagnostics
  echo "================ CLOUD AUTH TEST SMOKE END =================="
  return "${smoke_exit}"
}

function verifyCloudAuthenticatorTestsTriggered() {
  local reports_dir="target/surefire-reports"
  local matches

  if [[ ! -d "${reports_dir}" ]]; then
    echo "ERROR: ${reports_dir} directory not found"
    return 1
  fi

  matches=$(
    find "${reports_dir}" -maxdepth 1 -type f -name "TEST-*.xml" | while IFS= read -r report; do
      if grep -Eq 'classname=".*(AzureAuthenticatorTest|AzureAuthenticatorTests|AzureAuthenticatorIntegrationTest|AzureAuthenticatorIntegrationTests|GCPAuthenticatorTest|GCPAuthenticatorTests|GCPAuthenticatorIntegrationTest|GCPAuthenticatorIntegrationTests|AWSIAMAuthenticatorTest|AWSIAMAuthenticatorTests|AWSIAMAuthenticatorIntegrationTest|AWSIAMAuthenticatorIntegrationTests)"' "${report}"; then
        echo "${report}"
      fi
    done
  )

  if [[ -z "${matches}" ]]; then
    echo "ERROR: Azure/GCP authenticator tests were not triggered by mvn test"
    return 1
  fi

  echo "Verified Azure/GCP authenticator tests:"
  echo "${matches}"
}

function verifyRepoCheckoutIncludesCloudAuthTests() {
  set +x
  echo "---------------- Cloud auth check: CI checkout ----------------"
  echo "PWD: $(pwd)"
  git rev-parse HEAD || true
  git status --short || true

  local azure_path="src/test/java/com/cyberark/conjur/api/clients/AzureAuthenticatorTests.java"
  local gcp_path="src/test/java/com/cyberark/conjur/api/clients/GCPAuthenticatorTests.java"
  local aws_path="src/test/java/com/cyberark/conjur/api/clients/AWSIAMAuthenticatorTests.java"

  echo "Tracked files in checked out commit:"
  git ls-tree -r --name-only HEAD | grep -E 'AzureAuthenticatorTests\.java|GCPAuthenticatorTests\.java|AWSIAMAuthenticatorTests\.java' || echo "  none"

  for f in "${azure_path}" "${gcp_path}" "${aws_path}"; do
    if [[ -f "${f}" ]]; then
      echo "WORKSPACE: FOUND ${f}"
    else
      echo "WORKSPACE: MISSING ${f}"
    fi
  done

  ls -la src/test/java/com/cyberark/conjur/api/clients || true
  echo "----------------------------------------------------------------"
  set -x
}

function verifyDockerBuildContextIncludesCloudAuthTests() {
  set +x
  echo "---------------- Cloud auth check: Docker build context --------"

  if [[ -f ".dockerignore" ]]; then
    echo ".dockerignore:"
    cat .dockerignore
  else
    echo ".dockerignore not found"
  fi

  echo "Files present in docker build context tar stream:"
  tar -cf - . | tar -tf - | grep -E '^src/test/java/com/cyberark/conjur/api/clients/(AzureAuthenticatorTests|GCPAuthenticatorTests|AWSIAMAuthenticatorTests)\.java$' || echo "  none"

  echo "----------------------------------------------------------------"
  set -x
}

function logContainerCloudAuthSourceState() {
  set +x
  echo "---------------- Cloud auth check: runtime container -----------"
  local azure_file="src/test/java/com/cyberark/conjur/api/clients/AzureAuthenticatorTests.java"
  local gcp_file="src/test/java/com/cyberark/conjur/api/clients/GCPAuthenticatorTests.java"
  local aws_file="src/test/java/com/cyberark/conjur/api/clients/AWSIAMAuthenticatorTests.java"

  pwd
  ls -la src/test/java/com/cyberark/conjur/api/clients || true

  for f in "${azure_file}" "${gcp_file}" "${aws_file}"; do
    if [[ -f "${f}" ]]; then
      echo "CONTAINER: FOUND ${f}"
    else
      echo "CONTAINER: MISSING ${f}"
    fi
  done

  echo "----------------------------------------------------------------"
  set -x
}

function main() {
  finish
  if [[ "${RUN_AZURE_TESTS:-false}" == "true" ]]; then
    : "${AZURE_SUBSCRIPTION_ID:?AZURE_SUBSCRIPTION_ID is required}"
    : "${AZURE_RESOURCE_GROUP:?AZURE_RESOURCE_GROUP is required}"
    : "${USER_ASSIGNED_IDENTITY:?USER_ASSIGNED_IDENTITY is required}"
    : "${USER_ASSIGNED_IDENTITY_CLIENT_ID:?USER_ASSIGNED_IDENTITY_CLIENT_ID is required}"
  fi
  if [[ "${RUN_AWS_TESTS:-false}" == "true" ]]; then
    : "${AWS_ACCOUNT_ID:?AWS_ACCOUNT_ID is required}"
    : "${AWS_ROLE_NAME:?AWS_ROLE_NAME is required}"
  fi
  verifyRepoCheckoutIncludesCloudAuthTests
  verifyDockerBuildContextIncludesCloudAuthTests
  # The enterprise appliance (cuke-master) does not support configuring
  # authn-azure via environment variables. Azure integration tests run
  # against OSS Conjur only, where CONJUR_AUTHENTICATORS is set.
  if [[ "${RUN_AZURE_TESTS:-false}" != "true" ]] && [[ "${RUN_GCP_TESTS:-false}" != "true" ]] && [[ "${RUN_AWS_TESTS:-false}" != "true" ]]; then
    runDap
  fi
  runOss
  printCloudAuthTestSummaryOnce
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
  # Skip HTTPS proxy tests when running Azure, GCP, or AWS tests — the nginx proxy binds
  # host ports that may already be in use on the cloud ExecutorV2 agent.
  if [[ "${RUN_AZURE_TESTS:-false}" != "true" ]] && [[ "${RUN_GCP_TESTS:-false}" != "true" ]] && [[ "${RUN_AWS_TESTS:-false}" != "true" ]]; then
    printOssProxyConfiguration
    initializeOssCert
    runOssHttpsTests
  fi
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

function buildMvnTestCommand() {
  if [[ -n "${TEST_FILTER:-}" ]]; then
    echo "mvn -Dtest=${TEST_FILTER} -DfailIfNoTests=true -DfailIfNoSpecifiedTests=true test"
  else
    echo "mvn test"
  fi
}

function runDapTests() {
  echo '-----------------------test.sh------------------------------'
  echo "Running DAP tests"
  echo '------------------------------------------------------------'

  dap_test_cid=$(docker compose ps -q test-dap)
  set +x
  api_key_admin=$(docker compose exec -T client conjur user rotate_api_key)
  set -x
  mvn_test_cmd="$(buildMvnTestCommand)"
  tests_command="$(declare -f logContainerCloudAuthSourceState verifyCloudAuthenticatorTestsTriggered); logContainerCloudAuthSourceState && ${mvn_test_cmd} && verifyCloudAuthenticatorTestsTriggered && mvn jacoco:report"

  local cloud_env_args=(
    -e RUN_AZURE_TESTS="${RUN_AZURE_TESTS:-}"
    -e TEST_FILTER="${TEST_FILTER:-}"
    -e AZURE_SUBSCRIPTION_ID="${AZURE_SUBSCRIPTION_ID:-}"
    -e AZURE_RESOURCE_GROUP="${AZURE_RESOURCE_GROUP:-}"
    -e USER_ASSIGNED_IDENTITY="${USER_ASSIGNED_IDENTITY:-}"
    -e USER_ASSIGNED_IDENTITY_CLIENT_ID="${USER_ASSIGNED_IDENTITY_CLIENT_ID:-}"
    -e RUN_GCP_TESTS="${RUN_GCP_TESTS:-}"
    -e GCP_ID_TOKEN="${GCP_ID_TOKEN:-}"
    -e GCP_PROJECT_ID="${GCP_PROJECT_ID:-}"
    -e RUN_AWS_TESTS="${RUN_AWS_TESTS:-}"
    -e AWS_ACCOUNT_ID="${AWS_ACCOUNT_ID:-}"
    -e AWS_ROLE_NAME="${AWS_ROLE_NAME:-}"
  )

  set +x
  docker exec \
    -e CONJUR_AUTHN_API_KEY="${api_key_admin}" \
    -e CONJUR_AUTHN_LOGIN="admin" \
    "${cloud_env_args[@]}" \
    "${dap_test_cid}" \
    bash -c "${tests_command}"
  set -x
}

function runOssTests() {
  echo '-----------------------test.sh------------------------------'
  echo "Running tests"
  echo '------------------------------------------------------------'

  set +x
  api_key_admin=$(docker compose exec -T conjur conjurctl role retrieve-key cucumber:user:admin)
  set -x
  mvn_test_cmd="$(buildMvnTestCommand)"

  local cloud_env_args=(
    -e RUN_AZURE_TESTS="${RUN_AZURE_TESTS:-}"
    -e TEST_FILTER="${TEST_FILTER:-}"
    -e AZURE_SUBSCRIPTION_ID="${AZURE_SUBSCRIPTION_ID:-}"
    -e AZURE_RESOURCE_GROUP="${AZURE_RESOURCE_GROUP:-}"
    -e USER_ASSIGNED_IDENTITY="${USER_ASSIGNED_IDENTITY:-}"
    -e USER_ASSIGNED_IDENTITY_CLIENT_ID="${USER_ASSIGNED_IDENTITY_CLIENT_ID:-}"
    -e RUN_GCP_TESTS="${RUN_GCP_TESTS:-}"
    -e GCP_ID_TOKEN="${GCP_ID_TOKEN:-}"
    -e GCP_PROJECT_ID="${GCP_PROJECT_ID:-}"
    -e RUN_AWS_TESTS="${RUN_AWS_TESTS:-}"
    -e AWS_ACCOUNT_ID="${AWS_ACCOUNT_ID:-}"
    -e AWS_ROLE_NAME="${AWS_ROLE_NAME:-}"
  )

  set +x
  docker compose run --rm \
    -e CONJUR_AUTHN_LOGIN="admin" \
    -e CONJUR_AUTHN_API_KEY="$api_key_admin" \
    "${cloud_env_args[@]}" \
    test \
    bash -c "$(declare -f logContainerCloudAuthSourceState verifyCloudAuthenticatorTestsTriggered); logContainerCloudAuthSourceState && ${mvn_test_cmd} && verifyCloudAuthenticatorTestsTriggered && mvn jacoco:report"
  set -x
}

function runOssHttpsTests() {
  echo '-----------------------test.sh------------------------------'
  echo "Running https tests"
  echo '------------------------------------------------------------'

  set +x
  api_key_admin=$(docker compose exec -T conjur conjurctl role retrieve-key cucumber:user:admin)
  api_key_alice=$(docker compose exec -T conjur conjurctl role retrieve-key cucumber:user:alice@test)
  api_key_myapp=$(docker compose exec -T conjur conjurctl role retrieve-key cucumber:host:test/myapp)
  set -x

  conjur_test_cid=$(docker compose ps -q test-https)
  mvn_test_cmd="$(buildMvnTestCommand)"
  tests_command="$(declare -f logContainerCloudAuthSourceState verifyCloudAuthenticatorTestsTriggered); logContainerCloudAuthSourceState && ${mvn_test_cmd} && verifyCloudAuthenticatorTestsTriggered && mvn jacoco:report"

  local cloud_env_args=(
    -e RUN_AZURE_TESTS="${RUN_AZURE_TESTS:-}"
    -e TEST_FILTER="${TEST_FILTER:-}"
    -e AZURE_SUBSCRIPTION_ID="${AZURE_SUBSCRIPTION_ID:-}"
    -e AZURE_RESOURCE_GROUP="${AZURE_RESOURCE_GROUP:-}"
    -e USER_ASSIGNED_IDENTITY="${USER_ASSIGNED_IDENTITY:-}"
    -e USER_ASSIGNED_IDENTITY_CLIENT_ID="${USER_ASSIGNED_IDENTITY_CLIENT_ID:-}"
    -e RUN_GCP_TESTS="${RUN_GCP_TESTS:-}"
    -e GCP_ID_TOKEN="${GCP_ID_TOKEN:-}"
    -e GCP_PROJECT_ID="${GCP_PROJECT_ID:-}"
    -e RUN_AWS_TESTS="${RUN_AWS_TESTS:-}"
    -e AWS_ACCOUNT_ID="${AWS_ACCOUNT_ID:-}"
    -e AWS_ROLE_NAME="${AWS_ROLE_NAME:-}"
  )

  set +x
  docker exec -e CONJUR_AUTHN_LOGIN="admin"       -e CONJUR_AUTHN_API_KEY="$api_key_admin" "${cloud_env_args[@]}" ${conjur_test_cid} bash -c "${tests_command}"
  docker exec -e CONJUR_AUTHN_LOGIN="alice@test"   -e CONJUR_AUTHN_API_KEY="$api_key_alice" "${cloud_env_args[@]}" ${conjur_test_cid} bash -c "${tests_command}"
  docker exec -e CONJUR_AUTHN_LOGIN="host/test/myapp" -e CONJUR_AUTHN_API_KEY="$api_key_myapp" "${cloud_env_args[@]}" ${conjur_test_cid} bash -c "${tests_command}"
  set -x
}

main
