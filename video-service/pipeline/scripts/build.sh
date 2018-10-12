#!/usr/bin/env bash

set -eu

export GRADLE_USER_HOME="$(pwd)/.gradle"

version=$(cat version/version)

(
cd source
./gradlew -Pversion=${version} video-service:clean video-service:build --rerun-tasks
)

cp -a source/video-service/* dist/
