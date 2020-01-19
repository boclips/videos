package com.boclips.videos.service.infrastructure.collection

import com.boclips.users.client.model.contract.Contract
import com.boclips.users.client.model.contract.SelectedCollectionsContract
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.`in`

class MongoCollectionFilterContractAdapter {
    fun adapt(contract: Contract): Bson {
        return when (contract) {
            is SelectedCollectionsContract -> CollectionDocument::id `in` contract.collectionIds.map { ObjectId(it) }
            else -> throw IllegalArgumentException("Unknown contract type: ${contract.javaClass}")
        }
    }
}
