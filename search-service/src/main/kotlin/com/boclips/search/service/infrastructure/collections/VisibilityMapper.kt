package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionVisibility

object VisibilityMapper {
    fun map(visibility: CollectionVisibility): String {
        return when(visibility) {
            CollectionVisibility.PUBLIC -> "public"
            CollectionVisibility.PRIVATE -> "private"
        }
    }
}