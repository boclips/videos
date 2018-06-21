#!/usr/bin/env bash

lpass login $1
lpass show --notes "video-cleanser application-staging.yml" > ./src/main/resources/application-staging.yml
lpass show --notes "video-cleanser application-production.yml" > ./src/main/resources/application-production.yml