#!/usr/bin/env bash

set -eu

cwd="$(cd "$(dirname $0)" && pwd)"
export GRADLE_USER_HOME="${cwd}/search-service/.gradle"

(
cd source/video-service
./gradlew clean test --rerun-tasks
)

