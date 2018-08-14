#!/usr/bin/env bash

set -x

MONGO_USER=$1
MONGO_PASSWORD=$2

function fail_usage {
    echo "usage: $0 USER PASSWORD" 2>&1
    exit 1
}

if [ ! "$MONGO_USER" ]
then
    fail_usage
fi

if [ ! "$MONGO_PASSWORD" ]
then
    fail_usage
fi

COMMAND="mongo \
    --host \"mongodb://boclips-staging-cluster-shard-00-00-lbj4p.gcp.mongodb.net:27017,boclips-staging-cluster-shard-00-01-lbj4p.gcp.mongodb.net:27017,boclips-staging-cluster-shard-00-02-lbj4p.gcp.mongodb.net:27017/backend-db?replicaSet=boclips-staging-cluster-shard-0\" \
    --ssl \
    --authenticationDatabase admin \
    --username \"$MONGO_USER\" \
    --password \"$MONGO_PASSWORD\""

if [ -t 0 ]; then
    eval "${COMMAND}"
else
    cat | eval "${COMMAND}"
fi
