#!/usr/bin/env bash

set -eu

export GRADLE_USER_HOME="$(pwd)/.gradle"

cwd="$(cd "$(dirname $0)" && pwd)"

(
cd source
./gradlew clean test --rerun-tasks
)

