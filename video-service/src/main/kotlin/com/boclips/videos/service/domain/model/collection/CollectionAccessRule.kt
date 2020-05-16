package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.user.UserId

sealed class CollectionAccessRule {
    data class SpecificOwner(val owner: UserId) : CollectionAccessRule() {
        override fun toString() = "SpecificOwner($owner)"
    }

    data class SpecificIds(val collectionIds: Set<CollectionId>) : CollectionAccessRule() {
        override fun toString() = "SpecificIds(${collectionIds.size} collections)"
    }

    object Everything : CollectionAccessRule() {
        override fun toString() = "Everything"
    }

    companion object {
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
            is SpecificIds ->
                this.collectionIds.contains(collection.id)
            is SpecificOwner ->
                this.owner == collection.owner
            Everything -> true
        }
    }
}
