package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.collections.CollectionSearchAdapter
import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.videos.service.domain.service.collection.CollectionSearchService

class DefaultCollectionSearch(
    indexReader: IndexReader<CollectionMetadata, CollectionQuery>,
    indexWriter: IndexWriter<CollectionMetadata>
) : CollectionSearchAdapter<com.boclips.videos.service.domain.model.collection.Collection>(
    indexReader,
    indexWriter
),
    CollectionSearchService {

    override fun convert(document: com.boclips.videos.service.domain.model.collection.Collection): CollectionMetadata {
        return CollectionMetadataConverter.convert(document)
    }
}