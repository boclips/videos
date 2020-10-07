package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.infrastructure.AbstractIndexWriter
import com.boclips.search.service.infrastructure.IndexParameters
import org.elasticsearch.client.RestHighLevelClient

class ChannelsIndexWriter private constructor(
    client: RestHighLevelClient,
    indexParameters: IndexParameters,
    batchSize: Int
) :
    AbstractIndexWriter<ChannelMetadata>(
        indexConfiguration = ChannelIndexConfiguration(),
        indexParameters = indexParameters,
        client = client,
        esIndex = ChannelsIndex,
        batchSize = batchSize,
        ngram = true
    ) {
    companion object {
        fun createInstance(
            client: RestHighLevelClient,
            indexParameters: IndexParameters,
            batchSize: Int
        ): ChannelsIndexWriter {
            return ChannelsIndexWriter(client, indexParameters, batchSize)
        }

        fun createTestInstance(
            client: RestHighLevelClient,
            batchSize: Int
        ): ChannelsIndexWriter {
            return ChannelsIndexWriter(client, IndexParameters(numberOfShards = 1), batchSize)
        }
    }

    override fun serializeToIndexDocument(channelMetadata: ChannelMetadata) =
        ChannelsDocumentConverter().convertToDocument(channelMetadata)

    override fun getIdentifier(entry: ChannelMetadata) = entry.id
}
