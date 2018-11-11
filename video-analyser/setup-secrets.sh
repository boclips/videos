#!/usr/bin/env bash

lpass show --notes "asset-analyser application-staging.yml" > ./src/main/resources/application-staging.yml
lpass show --notes "asset-analyser application-production.yml" > ./src/main/resources/application-production.yml