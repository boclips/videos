# Video Service

> A core service powering the educational video revolution by serving videos and collections.

It serves videos, collections and manages everything else related to video content (subjects, age ranges, video types).

## Search Service

> To find what is relevant, and to ignore what is not.

The video service is using the search service module to search for videos in elastic search.
The video search query is owned by the search service. It's goal is to return relevant and ordered results.

## Video Service Client

> To serve the ones that need it.

If you are looking to integrate with the video-service, but you don't want to handle the HTTP requests yourselves,
the video-service client is a Java  wrapper for the video service.

It currently only covers a small subset of the API endpoints, but we are looking to extend it as we go along.

The client is publicly distributed using [JitPack](https://jitpack.io/#boclips/videos).

### Releasing a new client version

Releasing a new version of the client, entails cutting a new release. The concourse pipeline has a job for just that.

This is what needs to be done:

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
