package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.UserId

sealed class CollectionAccessRule {
    object PublicOnly : CollectionAccessRule() {
        override fun toString() = "PublicOnly"
    }
    data class SpecificOwner(val owner: UserId) : CollectionAccessRule() {
        fun isMe(user: UserId) = owner == user
        override fun toString() = "SpecificOwner($owner)"
    }

    data class SpecificIds(val collectionIds: Set<CollectionId>) : CollectionAccessRule() {
        override fun toString() = "SpecificIds(${collectionIds.size} collections)"
    }
    object Everything : CollectionAccessRule() {
        override fun toString() = "Everything"

    }

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
