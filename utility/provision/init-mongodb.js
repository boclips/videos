db = db.getSiblingDB('video-ingestor-db');
db.getCollectionNames().forEach(collectionName => db[collectionName].drop());

db.httpSources.insert({
    "url": "https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60003",
    "provider": "Singapore Press Holdings"
});
db.httpSources.insert({
    "url": "https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60004",
    "provider": "Singapore Press Holdings"
});
db.httpSources.insert({
    "url": "https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60005",
    "provider": "Singapore Press Holdings"
});
db.httpSources.insert({
    "url": "https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60006",
    "provider": "Singapore Press Holdings"
});
db.httpSources.insert({
    "url": "https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60007",
    "provider": "Singapore Press Holdings"
});
db.httpSources.insert({
    "url": "https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60008",
    "provider": "Singapore Press Holdings"
});
db.httpSources.insert({
    "url": "https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60009",
    "provider": "Singapore Press Holdings"
});
db.httpSources.insert({
    "url": "https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60010",
    "provider": "Singapore Press Holdings"
});
db.httpSources.insert({
    "url": "https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60011",
    "provider": "Singapore Press Holdings"
});
db.httpSources.insert({
    url: "http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knoweconomist",
    provider: "Bloomberg"
});
db.httpSources.insert({
    url: "http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowhollywoodreporter",
    provider: "Bloomberg"
});
db.httpSources.insert({
    url: "http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowlux",
    provider: "Bloomberg"
});
db.httpSources.insert({
    url: "http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowstat",
    provider: "Bloomberg"
});
db.httpSources.insert({
    url: "http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowbiz",
    provider: "Bloomberg"
});
db.httpSources.insert({
    url: "http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowfeatures",
    provider: "Bloomberg"
});
db.httpSources.insert({
    url: "http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowesp",
    provider: "Bloomberg"
});
db.httpSources.insert({
    url: "http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowall",
    provider: "Bloomberg"
});

db = db.getSiblingDB('km4');
db.getCollectionNames().forEach(collectionName => db[collectionName].drop());

var clientId = ObjectId();
var watchboclipsClientId = ObjectId();
var organizationId = ObjectId();
var watchboclipsOrganizationId = ObjectId();
var packageId = ObjectId();
var pricePolicyId = ObjectId();
var superAdminId = ObjectId();
var dummyUserId = ObjectId();
var watchboclipsUserId = ObjectId();
var licenseCode5Years = "5YR_SR";
var typeIdInstructional = 3;
var pricePolicyCode = "BASE";
var licenseId = ObjectId();

db.clients.insert({
    "_id": clientId,
    "secret": "B3stEduc4t10n4lV1d3os",
    "name": "Boclips",
    "__v": 0,
    "client_id": "boclips"
});

db.clients.insert({
    "_id": watchboclipsClientId,
    "secret": "Wh4t3v3rY0uW4ntMat3",
    "name": "BoTeachers",
    "client_id": "boteachers",
    "__v": 0
});

db.licenses.insert({
    "_id": licenseId,
    "date_created": new Date().toISOString(),
    "uuid": "624a567a-5644-438f-8330-077bcfc8d668",
    "code": licenseCode5Years,
    "description": "Term 5 year Single Region",
    "__v": 0,
    "date_updated": new Date().toISOString()
});

db.pricepolicies.insert({
    "_id": pricePolicyId,
    "date_updated": new Date().toISOString(),
    "date_created": new Date().toISOString(),
    "code": pricePolicyCode,
    "uuid": "e6516e74-4509-4d83-9859-12a4cbcecc54",
    "__v": 4
});

db.packages.insert({
    "_id": packageId,
    "name": "default",
    "date_created": new Date().toISOString(),
    "date_updated": new Date().toISOString(),
    "price_policy": pricePolicyId.valueOf(),
    "search_filters": [{
        "_refType": "Source",
        "invert_filter": true,
        "_id": "594d38ed9caf602da1d5f7b0",
        "items": ["5914252c71b7892774f8f0c7", "596747fda270460a9c07af7f"]
    }, {"_refType": "Assettype", "invert_filter": false, "_id": "594d38ed9caf602da1d5f7b1", "items": []}],
    "uuid": "908a5511-df4c-4609-9895-f0436e6f3c13",
    "__v": 2,
    "clients": [clientId.valueOf(), watchboclipsClientId.valueOf()]
});

db.organizations.insert({
    "_id": organizationId,
    "date_updated": new Date().toISOString(),
    "date_created": new Date().toISOString(),
    "name": "Knowledgemotion",
    "address": "South Bank",
    "uuid": "504bab82-6937-48a1-b941-4e28e12c9eda",
    "__v": 0,
    "package": packageId.valueOf()
});

db.organizations.insert({
    "_id": watchboclipsOrganizationId,
    "date_updated": new Date().toISOString(),
    "date_created": new Date().toISOString(),
    "name": "Play Teachers - USA",
    "address": "USA",
    "uuid": "41b73068-2e66-4824-9754-c47ffc3fa446",
    "__v": 0,
    "package": packageId
});

db.users.insert({
    "_id": superAdminId,
    "date_updated": new Date().toISOString(),
    "date_created": new Date().toISOString(),
    "email": "superadmin@boclips.com",
    "address": "",
    "organization": organizationId.valueOf(),
    "uuid": "f66c9a21-4a9f-441a-890e-99d0b86a3d8f",
    "disabled": false,
    "_registration_completed": false,
    "preferences": [],
    "role": "super",
    "password": "$2a$10$mlYj.NmRL9FfZ2fGzuaYJu2LvGLVAmqaxLECSThXJEXhMhMwtx.9e",
    "surname": "Duper",
    "name": "Super",
    "username": "super-admin",
    "__v": 0
});

db.users.insert({
    "_id": dummyUserId,
    "date_updated": new Date().toISOString(),
    "date_created": new Date().toISOString(),
    "email": "standard@boclips.com",
    "address": "",
    "organization": organizationId.valueOf(),
    "uuid": "f66c9a21-4a9f-441a-890e-99d0b86a3d6f",
    "disabled": false,
    "_registration_completed": false,
    "preferences": [],
    "role": "standard",
    "password": "$2a$10$ULpGmDMluFEoIx80Kx7YJuKa96khbEQ1qhcDCGFnPKZqouUp.6F2O",
    "surname": "Dummy",
    "name": "User",
    "username": "dummy-user",
    "__v": 0
});

db.users.insert({
    "_id": watchboclipsUserId,
    "date_updated": new Date().toISOString(),
    "date_created": new Date().toISOString(),
    "email": "teacher@gmail.com",
    "city": "London",
    "extra_fields": {
        "school_name": "The Boclips School",
        "school_type": "Secondary",
        "teacher_advisory_panel": true,
        "_id": ObjectId()
    },
    "organization": watchboclipsOrganizationId,
    "original_registration": ObjectId(),
    "uuid": "e9e7cb45-1a6d-411d-b19b-795bf3184843",
    "disabled": false,
    "_registration_completed": true,
    "preferences": [],
    "role": "teacher",
    "password": "$2a$10$ULpGmDMluFEoIx80Kx7YJuKa96khbEQ1qhcDCGFnPKZqouUp.6F2O",
    "surname": "Chaos",
    "name": "Professor",
    "username": "teacher",
    "__v": 0,
    "_self_registration": true
});

db.assettypes.insert({
    "date_created": new Date().toISOString(),
    "uuid": "86ba3dea-5c0e-480d-bac3-b8e152029daf",
    "name": "Other",
    "description": "Other",
    "__v": 0,
    "date_updated": new Date().toISOString(),
    "type_id": 0
});
db.assettypes.insert({
    "date_created": new Date().toISOString(),
    "uuid": "c0eff993-4501-4abc-9cb0-bc8269d26c89",
    "name": "News",
    "description": "News",
    "__v": 0,
    "date_updated": new Date().toISOString(),
    "type_id": 1
});
db.assettypes.insert({
    "date_created": new Date().toISOString(),
    "uuid": "8359dcbc-e17b-4318-850a-32137ac807b1",
    "name": "Stock",
    "description": "Stock",
    "__v": 0,
    "date_updated": new Date().toISOString(),
    "type_id": 2
});
db.assettypes.insert({
    "date_created": new Date().toISOString(),
    "uuid": "2063e39c-199f-41d2-9a1f-e984cd558c46",
    "name": "Instructional Clips",
    "description": "Instructional Clips",
    "__v": 0,
    "date_updated": new Date().toISOString(),
    "type_id": typeIdInstructional
});
db.assettypes.insert({
    "date_created": new Date().toISOString(),
    "uuid": "80bafe64-3607-4614-9c22-d516f28460dd",
    "name": "TV Clips",
    "description": "TV Clips",
    "__v": 0,
    "date_updated": new Date().toISOString(),
    "type_id": 4
});
db.assettypes.insert({
    "date_created": new Date().toISOString(),
    "uuid": "a3309ae4-7c5a-46de-8ca4-77ea13fb9492",
    "name": "News Package",
    "description": "News Package",
    "__v": 0,
    "date_updated": new Date().toISOString(),
    "type_id": 5
});
db.assettypes.insert({
    "date_created": new Date().toISOString(),
    "uuid": "a625831a-71ab-4360-9f2e-415d75e90f36",
    "name": "UGC News",
    "description": "UGC News",
    "__v": 0,
    "date_updated": new Date().toISOString(),
    "type_id": 6
});
db.assettypes.insert({
    "date_updated": new Date().toISOString(),
    "date_created": new Date().toISOString(),
    "name": "360 VR Stock",
    "description": "360 VR Stock",
    "type_id": 7,
    "uuid": "8339db9e-0019-4d04-a255-1602334d6936",
    "__v": 0
});
db.assettypes.insert({
    "date_updated": new Date().toISOString(),
    "date_created": new Date().toISOString(),
    "uuid": "vVHc31q5pB9nXU4UdSeNM7O5WIhOyTLLWWpeTDCcAPaqeFCbwo6BlwzEnyNlKLmZmhHyz9elVsfPTeMQBKnbc9ts570eOJ98pCbb",
    "name": "360 VR Immersive",
    "description": "360 VR Immersive",
    "type_id": 8,
});
db.assettypes.insert({
    "uuid": "6cb95077-e467-43a6-881f-da8b872fbfce",
    "name": "Short Programme",
    "description": "Short Programme",
    "date_created": new Date().toISOString(),
    "type_id": 9,
    "__v": 0
});

db.assettypeprices.insert({
    "_id": ObjectId(),
    "date_updated": new Date().toISOString(),
    "date_created": new Date().toISOString(),
    "code": pricePolicyCode,
    "base_price": 150,
    "asset_type": ObjectId("5b110f0e1ae60f2c7915b564"),
    "uuid": "f375a264-ab9a-4037-b849-27eaeb1506cc",
    "__v": 0,
    "parent": pricePolicyId
});

db.licenseprices.insert({
    "date_updated": new Date().toISOString(),
    "date_created": new Date().toISOString(),
    "price_variation": 0.15,
    "license": licenseId,
    "uuid": "8066827f-d8e3-4a60-9afa-ae7b71f05889",
    "__v": 0,
    "code": pricePolicyCode,
    "parent": pricePolicyId
});
