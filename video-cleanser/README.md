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