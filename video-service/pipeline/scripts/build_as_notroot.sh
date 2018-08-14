#!/usr/bin/env bash

set -ex

adduser --disabled-password notroot </dev/null
usermod -aG root notroot

su - notroot -c "$(pwd)/$(dirname $0)/build.sh"
