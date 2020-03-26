package com.boclips.videos.service.infrastructure.collection

import com.boclips.users.api.response.accessrule.AccessRuleResource
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.`in`

class MongoCollectionFilterAccessRuleAdapter {
    fun adapt(contract: AccessRuleResource): Bson {
        return when (contract) {
            is AccessRuleResource.IncludedCollections -> CollectionDocument::id `in` contract.collectionIds.map {
                ObjectId(
                    it
                )
            }
            else -> throw IllegalArgumentException("Unknown contract type: ${contract.javaClass}")
        }
    }
}
