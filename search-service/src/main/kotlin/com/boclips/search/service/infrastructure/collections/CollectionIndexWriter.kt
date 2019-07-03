package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.infrastructure.AbstractIndexWriter
import org.elasticsearch.client.RestHighLevelClient

class CollectionIndexWriter(client: RestHighLevelClient) : AbstractIndexWriter<CollectionMetadata>(
    CollectionIndexConfiguration(),
    client,
    CollectionsIndex
) {
    override fun serializeToIndexDocument(entry: CollectionMetadata) = CollectionDocument(
        id = entry.id,
        title = entry.title,
        subjects = entry.subjectIds
    )

    override fun getIdentifier(entry: CollectionMetadata) = entry.id
}
