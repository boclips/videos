package com.boclips.search.service.domain.collections.model

enum class CollectionVisibility {
    PUBLIC, PRIVATE
}

sealed class CollectionVisibilityQuery {
    companion object {
        fun publicOnly() = One(CollectionVisibility.PUBLIC)
        fun privateOnly() = One(CollectionVisibility.PRIVATE)
    }

    object All : CollectionVisibilityQuery()
    data class One(val collectionVisibility: CollectionVisibility) : CollectionVisibilityQuery()

    fun contains(item: CollectionVisibility) =
        when (this) {
            All -> true
            is One -> this.collectionVisibility == item
        }
}
