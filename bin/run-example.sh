#!/bin/bash


# Adds the Maven computed classpath args
NAME=$1
ARGS="-classpath %classpath"
ARGS="$ARGS net.conjur.api.examples.$NAME"
echo "JVM args=$ARGS"
mvn -q exec:exec -Dexec.executable="java" -Dexec.args="$ARGS"
