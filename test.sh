#!/usr/bin/env bash
set -ex
set -o pipefail
source utils.sh

trap finish EXIT

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
  createOssTestEnvironment
  loadOssTestPolicy
  runOssTests
  printOssProxyConfiguration
  initializeOssCert
  runOssHttpsTests
}

main
