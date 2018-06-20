#!/usr/bin/env bash

lpass login $1
lpass show --notes "video-cleanser application-staging.yml" > ./src/main/resources/application-staging.yml