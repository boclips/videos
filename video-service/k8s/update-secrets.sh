#!/usr/bin/env bash

kubectl delete secret video-service -n=staging
kubectl create secret generic video-service --from-env-file <(lpass show --notes "video-service-staging-env") -n=staging

kubectl delete secret video-service -n=production
kubectl create secret generic video-service --from-env-file <(lpass show --notes "video-service-production-env") -n=production