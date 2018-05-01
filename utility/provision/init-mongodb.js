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