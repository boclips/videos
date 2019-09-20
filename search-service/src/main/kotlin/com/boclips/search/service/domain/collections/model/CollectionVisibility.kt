package com.boclips.search.service.domain.collections.model

enum class CollectionVisibility {
    PUBLIC, PRIVATE;

    companion object {
        val ALL = listOf(PUBLIC, PRIVATE)
    }
}
