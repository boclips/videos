#!/usr/bin/env bash

set -eu

cwd="$(cd "$(dirname $0)" && pwd)"

# shellcheck disable=SC1090
source "$cwd/../../../infrastructure/concourse/kubernetes-credential-functions.sh"

staging_secret_name=$(secret_name staging)
production_secret_name=$(secret_name production)
staging_token="$(token staging $staging_secret_name)"
staging_ca="$(ca staging $staging_secret_name)"
production_token="$(token production $production_secret_name)"
production_ca="$(ca production $production_secret_name)"
kubernetes_server="https://35.189.87.155"

fly --target ci \
    set-pipeline \
    --pipeline video-service \
    --config $cwd/pipeline.yml \
    --var gcr-key="$(lpass show concourse-docker-service-account-key --notes)" \
    --var videos-repo-key="$(lpass show concourse-videos-repo-key --notes)" \
    --var infrastructure-repo-key="$(lpass show concourse-infrastructure-key --notes)" \
    --var versions-repo-key="$(lpass show concourse-versions-repo-key --notes)" \
    --var kubernetes-server="$kubernetes_server" \
    --var kubernetes-staging-token="$staging_token" \
    --var kubernetes-staging-ca="$staging_ca" \
    --var kubernetes-production-token="$production_token" \
    --var kubernetes-production-ca="$production_ca"
