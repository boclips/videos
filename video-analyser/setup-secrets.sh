#!/usr/bin/env bash

lpass login $1
lpass show --notes "video-videoanalyser application-staging.yml" > ./src/main/resources/application-staging.yml
lpass show --notes "video-videoanalyser application-production.yml" > ./src/main/resources/application-production.yml