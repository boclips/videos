package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.collections.CollectionSearchServiceAdapter
import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import org.springframework.stereotype.Component

@Component
class DefaultCollectionSearchService(
    readSearchService: ReadSearchService<CollectionMetadata, CollectionQuery>,
    writeSearchService: WriteSearchService<CollectionMetadata>
) : CollectionSearchServiceAdapter<com.boclips.videos.service.domain.model.collection.Collection>(
    readSearchService,
    writeSearchService
),
    CollectionSearchService {

    override fun convert(document: com.boclips.videos.service.domain.model.collection.Collection): CollectionMetadata {
        return CollectionMetadataConverter.convert(document)
    }
}