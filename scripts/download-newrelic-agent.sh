#!/bin/sh
# Downloads the New Relic Java agent into ./newrelic/ (for docker-compose volume ./newrelic:/opt/newrelic).
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VERSION="${NR_AGENT_VERSION:-8.18.0}"
ZIP_URL="https://download.newrelic.com/newrelic/java-agent/newrelic-agent/${VERSION}/newrelic-java.zip"
TMP="$(mktemp)"
trap 'rm -f "${TMP}"' EXIT
curl -fsSL "${ZIP_URL}" -o "${TMP}"
# Zip root is a single "newrelic/" directory → repo/newrelic/newrelic.jar
unzip -qo "${TMP}" -d "${ROOT}"
JAR="${ROOT}/newrelic/newrelic.jar"
if [ ! -f "${JAR}" ]; then
  echo "Expected ${JAR} after unzip" >&2
  ls -la "${ROOT}/newrelic" 2>/dev/null || ls -la "${ROOT}" >&2
  exit 1
fi
echo "Agent ready: ${JAR}"
