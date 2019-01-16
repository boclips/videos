# Video Service

## How to reindex Elastic Search

1. Temporarily disable deployments to the target environment (staging / production) on Concourse
1. Trigger the corresponding [re-index job][re-index-jobs] in Concourse
1. Tail the `video-service` logs to follow progress
1. Re-enable deployments to the target environment

[re-index-jobs]: https://ci.boclips.com/teams/main/pipelines/boclips?groups=reindexing