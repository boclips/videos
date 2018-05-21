#!/usr/bin/env bash

set -x -e

pushd ..
  R CMD install .
popd



Rscript make-report.R
