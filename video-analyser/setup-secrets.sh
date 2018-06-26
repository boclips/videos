#!/usr/bin/env bash

lpass login $1
lpass show --notes "video-analyser application-staging.yml" > ./src/main/resources/application-staging.yml
lpass show --notes "video-analyser application-production.yml" > ./src/main/resources/application-production.yml