#!/usr/bin/env bash

set -x -e

git pull -r

R CMD check .

git push
