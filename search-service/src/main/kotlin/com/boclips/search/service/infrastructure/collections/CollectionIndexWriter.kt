package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.config.properties.ReindexProperties
import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.infrastructure.AbstractIndexWriter
import com.boclips.search.service.infrastructure.IndexParameters
import org.elasticsearch.client.RestHighLevelClient

class CollectionIndexWriter private constructor(
    client: RestHighLevelClient,
    indexParameters: IndexParameters,
    reindexProperties: ReindexProperties
) :
    AbstractIndexWriter<CollectionMetadata>(
        indexConfiguration = CollectionIndexConfiguration(),
        indexParameters = indexParameters,
        client = client,
        esIndex = CollectionsIndex,
        reindexProperties = reindexProperties
    ) {
    companion object {
        fun createInstance(
            client: RestHighLevelClient,
            indexParameters: IndexParameters,
            reindexProperties: ReindexProperties
        ): CollectionIndexWriter {
            return CollectionIndexWriter(client, indexParameters, reindexProperties)
        }

        fun createTestInstance(
            client: RestHighLevelClient,
            reindexProperties: ReindexProperties
        ): CollectionIndexWriter {
            return CollectionIndexWriter(client, IndexParameters(numberOfShards = 1), reindexProperties)
        }
    }

    override fun serializeToIndexDocument(collectionMetadata: CollectionMetadata) =
        CollectionDocumentConverter().convertToDocument(collectionMetadata)

    override fun getIdentifier(entry: CollectionMetadata) = entry.id
}
