#!/usr/bin/env bash

lpass login $1
lpass show --notes "mysql2es application-staging.yml" > ./src/main/resources/application-staging.yml
lpass show --notes "mysql2es application-production.yml" > ./src/main/resources/application-production.yml