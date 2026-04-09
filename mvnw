#!/bin/sh
# Apache Maven Wrapper

set -euf

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_DIR="$BASE_DIR/.mvn/wrapper"
JAR="$WRAPPER_DIR/maven-wrapper.jar"
PROPS="$WRAPPER_DIR/maven-wrapper.properties"

if [ ! -f "$PROPS" ]; then
  echo "Missing $PROPS" 1>&2
  exit 1
fi

if [ ! -f "$JAR" ]; then
  echo "Missing $JAR. Please run bootstrap to download it." 1>&2
  exit 1
fi

exec java -jar "$JAR" "$@"

