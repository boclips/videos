[![concourse](https://concourse.devboclips.net/api/v1/pipelines/boclips/jobs/build-video-service/badge)]()

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
the video-service client HTTP wrapper for the video service.

The client is publicly distributed using [JitPack](https://jitpack.io/#boclips/videos).

### Releasing a new client version

A new client is cut whenever CI tags the comment, which happens before any form of validation. 
This means we can cut potentially broken versions of the client. We should change that. 

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
