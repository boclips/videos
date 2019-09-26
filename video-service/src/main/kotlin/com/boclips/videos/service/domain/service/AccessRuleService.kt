package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.collection.CollectionId

interface AccessRuleService {
    fun getRules(userId: String): AccessRule
}

data class AccessRule(val collectionAccess: CollectionAccessRule) {
    companion object {
        fun build(permittedIds: List<CollectionId>): AccessRule {
            return AccessRule(
                if (permittedIds.isEmpty()) {
                    CollectionAccessRule.Unspecified
                } else {
                    CollectionAccessRule.RestrictedTo(permittedIds.toSet())
                }
            )
        }
    }

    fun allowsAccessTo(collection: CollectionId): Boolean {
        return when (collectionAccess) {
            is CollectionAccessRule.RestrictedTo ->
                collectionAccess.collectionIds.contains(collection)
            is CollectionAccessRule.Unspecified -> false
        }
    }
}

sealed class CollectionAccessRule {
    data class RestrictedTo(val collectionIds: Set<CollectionId>) : CollectionAccessRule()
    object Unspecified : CollectionAccessRule()
}
