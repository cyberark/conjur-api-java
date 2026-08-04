#!/usr/bin/env bash
# Fetches a GCP identity token and project ID from the GCP metadata service.
# Usage: get_gcp_token.sh <hostId> <account> <outputDir>
set -euo pipefail

HOST_ID="${1:?hostId argument is required}"
ACCOUNT="${2:?account argument is required}"
OUTPUT_DIR="${3:?outputDir argument is required}"

METADATA_BASE="http://metadata.google.internal/computeMetadata/v1"
AUDIENCE="conjur/${ACCOUNT}/host/${HOST_ID}"

echo "Fetching GCP identity token for audience: ${AUDIENCE}"

TOKEN=$(curl -sf \
  --header "Metadata-Flavor: Google" \
  "${METADATA_BASE}/instance/service-accounts/default/identity?audience=${AUDIENCE}&format=full")

# Validate JWT format (header.payload.signature)
IFS='.' read -r -a JWT_PARTS <<< "${TOKEN}"
if [[ "${#JWT_PARTS[@]}" -lt 3 ]]; then
  echo "ERROR: Received token does not appear to be a valid JWT (expected 3 dot-separated parts)"
  exit 1
fi

echo "Fetching GCP project ID"
PROJECT_ID=$(curl -sf \
  --header "Metadata-Flavor: Google" \
  "${METADATA_BASE}/project/project-id")

mkdir -p "${OUTPUT_DIR}"
echo -n "${TOKEN}" > "${OUTPUT_DIR}/token"
echo -n "${PROJECT_ID}" > "${OUTPUT_DIR}/project-id"

echo "Wrote token to ${OUTPUT_DIR}/token"
echo "Wrote project ID (${PROJECT_ID}) to ${OUTPUT_DIR}/project-id"
