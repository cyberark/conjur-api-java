#!/bin/bash
mvn install
ARGS="-classpath %classpath"
# Uncomment the next line for verbose output
#ARGS="$ARGS -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Dorg.apache.commmons.logging.simplelog.showdatetime=true -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG"
ARGS="$ARGS net.conjur.api.examples.BasicUsage"
mvn exec:exec -Dexec.executable="java" -Dexec.args="$ARGS"