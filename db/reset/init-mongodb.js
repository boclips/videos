function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }

    return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
}

db = db.getSiblingDB('video-ingestor-db');
db.getCollectionNames().forEach(collectionName => db[collectionName].drop());

const addFeed = (provider, url) => {
    db.httpSources.insert({
        'url': url,
        'provider': provider
    });
};

const SPH = 'Singapore Press Holdings';
addFeed(SPH, 'https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60003');
addFeed(SPH, 'https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60004');
addFeed(SPH, 'https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60005');
addFeed(SPH, 'https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60006');
addFeed(SPH, 'https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60007');
addFeed(SPH, 'https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60008');
addFeed(SPH, 'https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60009');
addFeed(SPH, 'https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60010');
addFeed(SPH, 'https://www.razor.tv/feeds/6/video?key=ddfb8d9fc3280dc023f7990f5480fad1&feed_id=60011');

const BLOOMBERG = 'Bloomberg';
addFeed(BLOOMBERG, 'http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knoweconomist');
addFeed(BLOOMBERG, 'http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowhollywoodreporter');
addFeed(BLOOMBERG, 'http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowlux');
addFeed(BLOOMBERG, 'http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowstat');
addFeed(BLOOMBERG, 'http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowbiz');
addFeed(BLOOMBERG, 'http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowfeatures');
// addFeed(BLOOMBERG, 'http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowesp');
addFeed(BLOOMBERG, 'http://know:47052956@vfeed.synd.bloomberg.com/f/nJEtBC/knowall');

db = db.getSiblingDB('km4');
db.getCollectionNames().forEach(collectionName => db[collectionName].drop());

const BASE_PRICE_POLICY_CODE = 'BASE';
const PEARSON_PRICE_POLICY_CODE = 'PEARSON';

const addClient = (clientId, secret) => {
    const id = ObjectId();
    db.clients.insert({
        _id: id,
        secret,
        name: clientId,
        client_id: clientId,
        __v: 0.0,
    });
    return id;
};

const addPricePolicy = (code) => {
    const id = ObjectId();
    db.pricepolicies.insert({
        _id: id,
        date_updated: new Date().toISOString(),
        date_created: new Date().toISOString(),
        code,
        uuid: guid(),
        __v: 0
    });
    return id;
};

const addPackage = (name, pricePolicyId, clients) => {
    const id = ObjectId();
    db.packages.insert({
        _id: id,
        name,
        date_created: new Date().toISOString(),
        date_updated: new Date().toISOString(),
        uuid: guid(),
        search_filters: [],
        clients,
        price_policy: pricePolicyId,
        __v: 0,
    });
    return id;
};

const addOrganization = (name, packageObjectId) => {
    const id = ObjectId();
    db.organizations.insert({
        _id: id,
        date_updated: new Date().toISOString(),
        date_created: new Date().toISOString(),
        name,
        address: '',
        package: packageObjectId,
        uuid: guid(),
        __v: 0,
    });
    return id;
};

const createUser = (username, email, password, role, organizationId) => {
    db.users.insert({
        _id: ObjectId(),
        date_updated: new Date().toISOString(),
        date_created: new Date().toISOString(),
        email,
        address: '',
        organization: organizationId,
        uuid: guid(),
        disabled: false,
        _registration_completed: false,
        preferences: [],
        role: 'super',
        password,
        surname: username,
        name: 'User',
        username,
        __v: 0
    });
};

const addAssetType = function (typeIdNumeric, assetTypeName) {
    const id = ObjectId();
    db.assettypes.insert({
        _id: id,
        uuid: guid(),
        type_id: typeIdNumeric,
        name: assetTypeName,
        description: assetTypeName,
        date_created: new Date().toISOString(),
        date_updated: new Date().toISOString(),
        __v: 0,
    });
    return id;
};

const addAssetTypePrices = function (assetTypeId, pricePolicyId, pricePolicyCode, basePrice) {
    db.assettypeprices.insert({
        _id: ObjectId(),
        date_updated: new Date().toISOString(),
        date_created: new Date().toISOString(),
        code: pricePolicyCode,
        base_price: basePrice,
        asset_type: assetTypeId,
        uuid: guid(),
        __v: 0,
        parent: pricePolicyId
    });
};

const addLicense = (code, description) => {
    const id = ObjectId();
    db.licenses.insert({
        _id: id,
        uuid: guid(),
        date_created: new Date().toISOString(),
        date_updated: new Date().toISOString(),
        code,
        description,
        __v: 0
    });
    return id;
};

const addLicensePrice = (licenseId, pricePolicyId, pricePolicyCode, priceVariation) => {
    db.licenseprices.insert({
        uuid: guid(),
        license: licenseId,
        parent: pricePolicyId,
        code: pricePolicyCode,
        price_variation: priceVariation,
        __v: 0,
        date_updated: new Date().toISOString(),
        date_created: new Date().toISOString(),
    });
};

const boclipsClientId = addClient('boclips', 'B3stEduc4t10n4lV1d3os');
const pearsonClientId = addClient('pearson', 'B3st3duc4t10nC0mp4ny');

const basePricePolicyId = addPricePolicy(BASE_PRICE_POLICY_CODE);
const pearsonPricePolicyId = addPricePolicy(PEARSON_PRICE_POLICY_CODE);

const defaultPackageId = addPackage('default', basePricePolicyId, [boclipsClientId]);
const pearsonPackageId = addPackage('pearson', pearsonPricePolicyId, [pearsonClientId]);

const knowledgeMotionOrgId = addOrganization('Knowledgemotion', defaultPackageId);
const pearsonOrgId = addOrganization('Pearson', pearsonPackageId);

createUser('super-admin', 'superadmin@boclips.com', '$2a$10$mlYj.NmRL9FfZ2fGzuaYJu2LvGLVAmqaxLECSThXJEXhMhMwtx.9e', 'super', knowledgeMotionOrgId);
createUser('dummy-user', 'standard@boclips.com', '$2a$10$ULpGmDMluFEoIx80Kx7YJuKa96khbEQ1qhcDCGFnPKZqouUp.6F2O', 'standard', knowledgeMotionOrgId);
createUser('pearson', 'pearson@pearson.com', '$2a$10$ULpGmDMluFEoIx80Kx7YJuKa96khbEQ1qhcDCGFnPKZqouUp.6F2O', 'standard', pearsonOrgId);

const otherAssetTypeId = addAssetType(0, 'Other');
addAssetTypePrices(otherAssetTypeId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 250);

const newsAssetTypeId = addAssetType(1, 'News');
addAssetTypePrices(newsAssetTypeId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 400);
addAssetTypePrices(newsAssetTypeId, pearsonPricePolicyId, 'PEARSON', 199.5);

const stockAssetTypeId = addAssetType(2, 'Stock');
addAssetTypePrices(stockAssetTypeId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 200);
addAssetTypePrices(stockAssetTypeId, pearsonPricePolicyId, 'PEARSON', 66.5);

const instructionalClipsAssetTypeId = addAssetType(3, 'Instructional Clips');
addAssetTypePrices(instructionalClipsAssetTypeId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 800.0);
addAssetTypePrices(instructionalClipsAssetTypeId, pearsonPricePolicyId, 'PEARSON', 399);

const tvClipsAssetTypeId = addAssetType(4, 'TV Clips');
addAssetTypePrices(tvClipsAssetTypeId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 800);
addAssetTypePrices(tvClipsAssetTypeId, pearsonPricePolicyId, 'PEARSON', 399);

const newsPackageAssetTypeId = addAssetType(5, 'News Package');
addAssetTypePrices(newsPackageAssetTypeId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 400);
addAssetTypePrices(newsPackageAssetTypeId, pearsonPricePolicyId, 'PEARSON', 199.5);

const ugcNewsAssetTypeId = addAssetType(6, 'UGC News');
addAssetTypePrices(ugcNewsAssetTypeId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 250);
addAssetTypePrices(ugcNewsAssetTypeId, pearsonPricePolicyId, 'PEARSON', 199.5);

const vr360StockAssetTypeId = addAssetType(7, '360 VR Stock');
addAssetTypePrices(vr360StockAssetTypeId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 800.0);
addAssetTypePrices(vr360StockAssetTypeId, pearsonPricePolicyId, 'PEARSON', 800.0);

const vr360ImmersiveAssetTypeId = addAssetType(8, '360 VR Immersive');
addAssetTypePrices(vr360ImmersiveAssetTypeId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 1000.0);
addAssetTypePrices(vr360ImmersiveAssetTypeId, pearsonPricePolicyId, 'PEARSON', 1000.0);

const shortProgrammeAssetTypeId = addAssetType(9, 'Short Programme');
addAssetTypePrices(shortProgrammeAssetTypeId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 5000.0);
addAssetTypePrices(shortProgrammeAssetTypeId, pearsonPricePolicyId, 'PEARSON', 5000.0);

const tedTalksAssetTypeObjectId = addAssetType(10, 'TED Talks');
addAssetTypePrices(tedTalksAssetTypeObjectId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 5000);

const tedEdAssetTypeObjectId = addAssetType(11, 'TED-Ed');
addAssetTypePrices(tedEdAssetTypeObjectId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 2000);

const license3yrSrId = addLicense('3YR_SR', 'Term 3 year Single Region');
addLicensePrice(license3yrSrId, basePricePolicyId, BASE_PRICE_POLICY_CODE, -0.25);

const license1yrSrId = addLicense('1YR_SR', 'Term 1 year Single Region');
addLicensePrice(license1yrSrId, basePricePolicyId, BASE_PRICE_POLICY_CODE, -0.6);

const license5yrSrId = addLicense('5YR_SR', 'Term 5 year Single Region');
addLicensePrice(license5yrSrId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 0);

const license10yrWId = addLicense('10YR_W', 'Term 10 year Worldwide');
addLicensePrice(license10yrWId, pearsonPricePolicyId, PEARSON_PRICE_POLICY_CODE, 0);

const license5yrMrId = addLicense('5YR_MR', 'Term 5 year Multi Region');
addLicensePrice(license5yrMrId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 0.15);

const license10yrSrId = addLicense('10YR_SR', 'Term 10 year Single Region');
addLicensePrice(license10yrSrId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 0.25);

const license10yrMrId = addLicense('10YR_MR', 'Term 10 year Multi Region');
addLicensePrice(license10yrMrId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 0.4);

const license3yrMrId = addLicense('3YR_MR', 'Term 3 year Multi Region');
addLicensePrice(license3yrMrId, basePricePolicyId, BASE_PRICE_POLICY_CODE, -0.15);

const license1yrMrId = addLicense('1YR_MR', 'Term 1 year Multi Region');
addLicensePrice(license1yrMrId, basePricePolicyId, BASE_PRICE_POLICY_CODE, -0.4);
