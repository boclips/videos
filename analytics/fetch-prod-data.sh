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

HOST='Production0-shard-0/production0-shard-00-00-jvwjy.mongodb.net:27017,production0-shard-00-01-jvwjy.mongodb.net:27017,production0-shard-00-02-jvwjy.mongodb.net:27017'

EXPORT_COMMAND="mongoexport \
    --ssl \
    --host \"$HOST\" \
    --db=km4 \
    --authenticationDatabase admin \
    --username \"$MONGO_USER\" \
    --password \"$MONGO_PASSWORD\" \
    --type=csv"

eval "${EXPORT_COMMAND} --collection=analytics.requests --fields=referrer,user,date_created --out=all-searches.csv --query=\"{referrer: {\\\$regex: \\\".*/search/.+\\\"}, user: {\\\$ne: null}}\""

eval "${EXPORT_COMMAND} --collection=users --fields=_id,email,name,surname --out=all-users.csv"
