package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.infrastructure.AbstractIndexWriter
import com.boclips.search.service.infrastructure.IndexParameters
import org.elasticsearch.client.RestHighLevelClient

class CollectionIndexWriter private constructor(
    client: RestHighLevelClient,
    indexParameters: IndexParameters,
    batchSize: Int
) :
    AbstractIndexWriter<CollectionMetadata>(
        indexConfiguration = CollectionIndexConfiguration(),
        indexParameters = indexParameters,
        client = client,
        esIndex = CollectionsIndex,
        batchSize = batchSize,
        ngram = false
    ) {
    companion object {
        fun createInstance(
            client: RestHighLevelClient,
            indexParameters: IndexParameters,
            batchSize: Int
        ): CollectionIndexWriter {
            return CollectionIndexWriter(client, indexParameters, batchSize)
        }

        fun createTestInstance(
            client: RestHighLevelClient,
            batchSize: Int
        ): CollectionIndexWriter {
            return CollectionIndexWriter(client, IndexParameters(numberOfShards = 1), batchSize)
        }
    }

    override fun serializeToIndexDocument(entry: CollectionMetadata) =
        CollectionDocumentConverter().convertToDocument(entry)

    override fun getIdentifier(entry: CollectionMetadata) = entry.id
}
