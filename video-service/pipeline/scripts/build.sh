#!/usr/bin/env bash

set -eu

GRADLE_USER_HOME="$(pwd)/.gradle"
export GRADLE_USER_HOME

version=$(cat version/version)

(
cd source
./gradlew \
    -Pversion="$version" \
    search-service:clean \
    video-service:clean \
    video-service:build \
    dependencyCheckAnalyze \
    --info \
    --no-daemon \
    --rerun-tasks
)

cp -a source/video-service/* dist/
