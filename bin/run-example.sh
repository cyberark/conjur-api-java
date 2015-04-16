#!/bin/bash


# Adds the Maven computed classpath args
args="-classpath %classpath"


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
NAME="BasicUsage"
if [[ ! -z $1 ]] ; then
    NAME=$1
fi
ARGS="$ARGS net.conjur.api.examples.$NAME"
echo "JVM args=$ARGS"
mvn -q exec:exec -Dexec.executable="java" -Dexec.args="$ARGS"
