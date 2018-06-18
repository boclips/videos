# Utility to enhance data quality

## Configuring the client

The client can run in different environments. Copy `application.yml` and amend the environment information.

## Generating Kaltura Session

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

## Listing media entries with time-window
```
curl -X POST https://www.kaltura.com/api_v3/service/media/action/list \
    -d "ks=$KALTURA_SESSION" \
    -d "userSecret=USERNAME" \
    -d "name=Knowledgemotion" \
    -d "format=1" \
    -d "filter[createdAtGreaterThanOrEqual]=1527465600" \
    -d "filter[createdAtLessThanOrEqual]=1527765520"
```

More information about getting a list of all media entries can be found [here](https://developer.kaltura.com/console/Ingest_and_Upload_Media/media/media_list?query=list)

