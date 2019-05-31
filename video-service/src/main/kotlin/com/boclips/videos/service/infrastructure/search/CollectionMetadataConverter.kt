package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.collections.model.CollectionMetadata

object CollectionMetadataConverter {
    fun convert(collection: com.boclips.videos.service.domain.model.collection.Collection): CollectionMetadata {
        return CollectionMetadata(
            id = collection.id.value,
            title = collection.title
        )
    }
}
