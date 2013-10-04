#!/bin/bash
cmd="mvn clean test"
if [[ ! -z "$CONJUR_STACK" ]] ; then
 cmd="$cmd -Dnet.conjur.api.stack=$CONJUR_STACK"
fi
if [[ ! -z "$CONJUR_ACCOUNT" ]] ; then
 cmd="$cmd -Dnet.conjur.api.account=$CONJUR_ACCOUNT"
fi
if [[ ! -z "$CONJUR_CREDENTIALS" ]] ; then
 cmd="$cmd -Dnet.conjur.api.credentials=$CONJUR_CREDENTIALS"
fi
echo "running mvn as $cmd"
$cmd