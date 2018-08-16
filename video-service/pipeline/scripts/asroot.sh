#!/usr/bin/env bash

set -ex

adduser --disabled-password notroot </dev/null
usermod -aG root notroot
chmod -R 777 .
chown -R notroot .

su - notroot -c "cd $(pwd) && $(dirname $0)/build.sh"
