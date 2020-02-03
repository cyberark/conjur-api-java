#!/bin/bash -e
source utils.sh
trap finish EXIT

createOssTestEnvironment
loadOssTestPolicy

service_cid="conjur-api-java_conjur_1"
api_key=$(docker exec $service_cid conjurctl role retrieve-key cucumber:user:admin)
echo '!!!!!!'
echo ''
echo "API Key: $api_key"
echo ''
echo '!!!!!!'

docker logs --tail 0 -f "$service_cid"