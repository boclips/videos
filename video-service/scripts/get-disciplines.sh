#!/usr/bin/env bash

set -e

if [ -z "$domain" ]
then
    echo "Must set domain"
    exit 1
fi

curl \
     -H "Authorization: Bearer $(create-keycloak-token)" \
     "https://api.$domain.com/v1/disciplines" \
     | jq .
