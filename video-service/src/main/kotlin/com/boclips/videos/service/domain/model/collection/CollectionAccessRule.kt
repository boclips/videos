package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.common.UserId

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

    fun allowsAccessTo(collection: Collection): Boolean {
        return when (this) {
            PublicOnly ->
                collection.isPublic
            is SpecificIds ->
                this.collectionIds.contains(collection.id)
            is SpecificOwner ->
                collection.isPublic ||
                    this.owner == collection.owner
            Everything -> true
        }
    }
}