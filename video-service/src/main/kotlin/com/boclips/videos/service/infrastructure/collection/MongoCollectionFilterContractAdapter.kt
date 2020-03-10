package com.boclips.videos.service.infrastructure.collection

import com.boclips.users.client.model.accessrule.AccessRule
import com.boclips.users.client.model.accessrule.IncludedCollectionsAccessRule
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.`in`

class MongoCollectionFilterAccessRuleAdapter {
    fun adapt(contract: AccessRule): Bson {
        return when (contract) {
            is IncludedCollectionsAccessRule -> CollectionDocument::id `in` contract.collectionIds.map { ObjectId(it) }
            else -> throw IllegalArgumentException("Unknown contract type: ${contract.javaClass}")
        }
    }
}
