#!/usr/bin/env bash

set -x -e

pushd ..
  Rscript install-dependencies.R
  R CMD install .
popd



Rscript make-report.R
