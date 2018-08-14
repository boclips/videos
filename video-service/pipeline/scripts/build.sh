#!/usr/bin/env bash

set -eu

cwd="$(cd "$(dirname $0)" && pwd)"
export GRADLE_USER_HOME="${cwd}/video-service/.gradle"

version=$(cat version/version)

(
cd source/video-service
./gradlew -Pversion=${version} clean build --rerun-tasks
)

cp -a source/video-service/* dist/
