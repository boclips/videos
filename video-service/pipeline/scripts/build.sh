#!/usr/bin/env bash

set -eu

GRADLE_USER_HOME="$(pwd)/.gradle"
export GRADLE_USER_HOME

version=$(cat version/tag)

(
cd source
./gradlew \
    -Pversion="$version" \
    search-service:clean \
    video-service:clean \
    video-service:build \
    ktlintCheck --debug \
    --info \
    --no-daemon \
    --rerun-tasks
)

cp -a source/video-service/* dist/
