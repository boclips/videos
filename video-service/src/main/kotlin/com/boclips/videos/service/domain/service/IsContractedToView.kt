package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.collection.Collection

// TODO: move this logic into AccessRule functionality
class IsContractedToView {
    operator fun invoke(collection: Collection, accessRule: AccessRule): Boolean {
        return when (accessRule.collectionAccess) {
            is CollectionAccessRule.RestrictedTo ->
                accessRule.collectionAccess.collectionIds.contains(collection.id)
            is CollectionAccessRule.All -> true
        }
    }
}
