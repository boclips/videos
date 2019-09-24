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
                    CollectionAccessRule.All
                } else {
                    CollectionAccessRule.RestrictedTo(permittedIds.toSet())
                }
            )
        }
    }
}

sealed class CollectionAccessRule {
    data class RestrictedTo(val collectionIds: Set<CollectionId>) : CollectionAccessRule()
    object All : CollectionAccessRule()
}
