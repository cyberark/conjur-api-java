#!/bin/bash
ARGS="-classpath %classpath"
if [[ -z $CONJUR_CREDENTIALS ]] ; then
	echo "You must set CONJUR_CREDENTIALS to username:password" >&2
	exit 1
fi
ARGS="$ARGS -Dnet.conjur.api.credentials='$CONJUR_CREDENTIALS'"
if [[ ! -z $CONJUR_ACCOUNT ]] ; then
  ARGS="$ARGS -Dnet.conjur.api.account=$CONJUR_ACCOUNT"
fi
if [[ ! -z $CONJUR_STACK ]] ; then
  ARGS="$ARGS -Dnet.conjur.api.stack=$CONJUR_STACK"
fi
ARGS="$ARGS -Dnet.conjur.api.resource.requestLogging=$CONJUR_RESOURCE_LOGGING"
ARGS="$ARGS -Dnet.conjur.api.authn.requestLogging=$CONJUR_AUTHN_LOGGING"
ARGS="$ARGS net.conjur.api.examples.BasicUsage"
echo "JVM args=$ARGS"
mvn -q exec:exec -Dexec.executable="java" -Dexec.args="$ARGS"
