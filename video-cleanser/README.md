# Video Analyser

A tool to help us manage videos in our databases and Kaltura.

## Setup

```
./setup-secrets.sh <your lastpass username>
```

This will download the secret staging and production configuration. 
Make sure the session in the secret note has not expired. Should it expire, here is how you can create a new one:

```
curl -X POST https://www.kaltura.com/api_v3/service/session/action/start \
    -d "format=1" \
    -d "type=2" \
    -d "expiry=31536000" \
    -d "userId=USERNAME" \
    -d "partnerId=PARTNER_ID" \
    -d "secret=ADMIN_SECRET"
```

More information about generating the session can be found in the [Kaltura API](https://developer.kaltura.com/api-docs/Generate_API_Sessions/session/session_start).

## Development

Run all tests, excluding contract test:
```
./gradlew video-cleanser:test
```

Run contract test:
```
./gradlew video-cleanser:testContracts
```
