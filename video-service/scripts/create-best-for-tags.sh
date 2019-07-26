#!/usr/bin/env bash

set -e

if [ -z "$domain" ]
then
    echo "Must set domain"
    exit 1
fi

create_tag() {
curl -i -X POST  \
     -d "{\"label\":\"$1\"}" \
     -H "Authorization: Bearer $(create-keycloak-token)" "https://api.$domain.com/v1/tags" \
     -H "Content-Type: application/json"
}

create_tag "Brain break"
create_tag "Context builder"
create_tag "Experiment"
create_tag "Explainer"
create_tag "Hook"
create_tag "Review"
create_tag "Other"
