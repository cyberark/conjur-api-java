#!/usr/bin/env bash
set -eo pipefail
 
export VERSION=$(./get-version.sh)
PUBLISH=false


if [[ "$VERSION" == *"SNAPSHOT" ]]; then
        echo "Its a snapshot"
        PUBLISH=true
fi


if [ ! $PUBLISH ]; then
        LINK="https://mvnrepository.com/artifact/org.apache.kafka/kafka-streams/$VERSION"
        STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$LINK")

        if [[ $STATUS_CODE == 200 ]]; then
                echo "$VERSION HAS ALREADY BEEN RELEASED"
        elif [[ $STATUS_CODE == 404 ]]; then
                echo "$VERSION CAN BE RELEASED"
                PUBLISH=true
        else
                echo "The $LINK returned" 
                echo "Status code: $STATUS_CODE"
        fi
fi

if [ $PUBLISH ]; then
        docker run --rm -v "$PWD:/conjurinc/api-java" -e VERSION -w /conjurinc/api-java maven:3-jdk-8 /bin/bash -c \
          "mvn -X -e clean deploy -Dmaven.test.skip=true --settings=/conjurinc/api-java/settings.xml"
fi