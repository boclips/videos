package com.boclips.videos.service.domain.service

import com.boclips.security.utils.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.common.UserId

interface AccessRuleService {
    fun getRules(user: User): AccessRule
}

data class AccessRule(val collectionAccess: CollectionAccessRule) {
    fun allowsAccessTo(collection: Collection): Boolean {
        return when (collectionAccess) {
            CollectionAccessRule.PublicOnly ->
                collection.isPublic
            is CollectionAccessRule.SpecificIds ->
                collectionAccess.collectionIds.contains(collection.id)
            is CollectionAccessRule.SpecificOwner ->
                collection.isPublic ||
                    collectionAccess.owner == collection.owner
            CollectionAccessRule.Everything -> true
        }
    }

    fun canAccessUsersCollections(targetOwner: UserId): Boolean {
        return when (this.collectionAccess) {
            is CollectionAccessRule.Everything -> true
            is CollectionAccessRule.SpecificOwner -> this.collectionAccess.owner == targetOwner
            is CollectionAccessRule.SpecificIds -> true // specific ID access bypasses collection visibility
            else -> false
        }
    }

    fun isPublic(): Boolean {
        return when (this.collectionAccess) {
            is CollectionAccessRule.PublicOnly -> true
            else -> false
        }
    }

    fun allowsEverything(): Boolean {
        return when (this.collectionAccess) {
            is CollectionAccessRule.Everything -> true
            else -> false
        }
    }
}

sealed class CollectionAccessRule {
    object PublicOnly : CollectionAccessRule()
    data class SpecificOwner(val owner: UserId) : CollectionAccessRule() {
        fun isMe(user: UserId) = owner == user
    }
    data class SpecificIds(val collectionIds: Set<CollectionId>) : CollectionAccessRule()
    object Everything : CollectionAccessRule()

    companion object {
        fun public(): CollectionAccessRule {
            return PublicOnly
        }

        fun asOwner(me: UserId): CollectionAccessRule {
            return SpecificOwner(owner = me)
        }

        fun specificIds(collections: List<CollectionId>): CollectionAccessRule {
            return SpecificIds(collections.toSet())
        }

        fun everything(): CollectionAccessRule {
            return Everything
        }
    }
}
