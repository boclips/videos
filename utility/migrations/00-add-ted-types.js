db = db.getSiblingDB('km4');

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

const tedTalksAssetTypeObjectId = addAssetType(10, 'TED Talks');
addAssetTypePrices(tedTalksAssetTypeObjectId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 5000);

const tedEdAssetTypeObjectId = addAssetType(11, 'TED-Ed');
addAssetTypePrices(tedEdAssetTypeObjectId, basePricePolicyId, BASE_PRICE_POLICY_CODE, 2000);
