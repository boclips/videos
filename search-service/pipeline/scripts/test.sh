#!/usr/bin/env bash

set -eu

export GRADLE_USER_HOME="$(pwd)/.gradle"

adduser --disabled-password notroot </dev/null
usermod -aG root notroot
chown -R notroot:notroot /elasticsearch-7.9.3

su -c /elasticsearch-7.9.3/bin/elasticsearch notroot &

while curl 'localhost:9200/_cluster/health?wait_for_status=yellow&timeout=1s' --fail
do
  echo 'waiting..'
  sleep 1
done


cwd="$(cd "$(dirname $0)" && pwd)"

(
cd source
./gradlew\
   search-service:clean\
   search-service:testWithoutSolr\
    --rerun-tasks
)
