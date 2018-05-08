db.getCollectionNames().forEach(collectionName => db[collectionName].drop());

var clientId = ObjectId();
var organizationId = ObjectId();
var packageId = ObjectId();
var pricePolicyId = ObjectId();
var superAdminId = ObjectId();
var dummyUserId = ObjectId();

db.clients.insert({
    "_id": clientId,
    "secret": "B3stEduc4t10n4lV1d3os",
    "name": "Boclips",
    "__v": 0,
    "client_id": "boclips"
});

db.pricepolicies.insert({
    "_id": pricePolicyId,
    "date_updated": new Date().toISOString(),
    "date_created": new Date().toISOString(),
    "code": "BASE",
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
    "clients": [clientId.valueOf()]
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
    "type_id": 3
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
