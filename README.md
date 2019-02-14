# Video Service

> To power the revolution of providing the publishing and education world with educational videos.

## Search Service

> To find what is relevant, and to ignore what is not.

The video service is using the search service module to search for videos in elastic search.

### Reindexing search indices

1. Temporarily disable deployments to the target environment (staging / production) on Concourse
1. Trigger the corresponding [re-index job][re-index-jobs] in Concourse
1. Tail the `video-service` logs to follow progress
1. Re-enable deployments to the target environment

[re-index-jobs]: https://ci.boclips.com/teams/main/pipelines/boclips?groups=reindexing

## Video Service Client

> To serve the ones that need it.

Java client to obtain, manage videos and collections. Abstracts away authentication and authorization for ease of use.

The client is distributed using [JitPack](https://jitpack.io/#boclips/videos). 

### Releasing a new client version

1. Make your changes & ensure the `build-video-service` job succeeds
2. Trigger the [`cut-release-video-service-client`](https://concourse.devboclips.net/teams/main/pipelines/boclips/jobs/cut-release-video-service-client) job
3. Specify the new version in the `build.gradle` file of the project using the client

# Development

Download dependencies for development:
```
./setup
```

Build, test, lint and push the project:
```
./ship
```

Run tests:
```
./gradlew test
```
