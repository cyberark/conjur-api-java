#!/bin/bash
ARGS="-classpath %classpath"
# Uncomment the next line for verbose output
ARGS="$ARGS -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Dorg.apache.commmons.logging.simplelog.showdatetime=true -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG"
if [[ -z $CONJUR_AUTHN ]] ; then
	echo "You must set CONJUR_AUTHN to username:password"
	exit 1
fi
ARGS="$ARGS net.conjur.api.examples.BasicUsage"
ARGS="$ARGS '$CONJUR_AUTHN'"
mvn -q exec:exec -Dexec.executable="java" -Dexec.args="$ARGS"