#!/bin/bash
ARGS="-classpath %classpath"
VERBOSE="-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Dorg.apache.commmons.logging.simplelog.showdatetime=true -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG"
while getopts ":v" opt; do
  echo "got $opt" >&2

  case $opt in
    v)
      echo "Verbose logging enabled" >&2
      ARGS="$ARGS $VERBOSE"
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
  esac
done
shift $((OPTIND-1))
if [ $# -eq 0 ] ; then
    echo "You must provide the name of the example to run" >&2
    exit 1
fi

NAME=$1

if [[ $NAME != *.* ]] ; then
    NAME="net.conjur.api.examples.$NAME"
fi

if [[ -z $CONJUR_AUTHN ]] ; then
	echo "You must set CONJUR_AUTHN to username:password" >&2
	exit 1
fi

echo "running example $NAME"

ARGS="$ARGS $NAME"
ARGS="$ARGS '$CONJUR_AUTHN'"
mvn -q exec:exec -Dexec.executable="java" -Dexec.args="$ARGS"