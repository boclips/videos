package com.boclips.videos.service.infrastructure.collection

import com.boclips.users.client.model.contract.Contract
import com.boclips.users.client.model.contract.SelectedContentContract
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.`in`
import java.lang.IllegalArgumentException

class MongoCollectionFilterContractAdapter {
    fun adapt(contract: Contract): Bson {
        return when (contract) {
            is SelectedContentContract -> CollectionDocument::id `in` contract.collectionIds.map { ObjectId(it) }
            else -> throw IllegalArgumentException("Unknown contract type: ${contract.javaClass}")
        }
    }
}