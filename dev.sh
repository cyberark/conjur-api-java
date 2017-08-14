#!/bin/bash -ex

function finish {
	docker rm -f $pg_cid
	docker rm -f $server_cid
}
trap finish EXIT

export POSSUM_DATA_KEY="$(docker run --rm possum data-key generate)"

pg_cid=$(docker run -d postgres:9.3)

server_cid=$(docker run -d \
	--link $pg_cid:pg \
	-e DATABASE_URL=postgres://postgres@pg/postgres \
	-e RAILS_ENV=development \
  -p 3000:80 \
	possum server -a cucumber)

sleep 10

api_key=$(docker exec "$server_cid" rails r "print Credentials['cucumber:user:admin'].api_key")
echo '!!!!!!'
echo ''
echo "API Key: $api_key"
echo ''
echo '!!!!!!'

docker exec -it "$server_cid" tail -f /opt/possum/log/development.log