package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.infrastructure.AbstractIndexWriter
import com.boclips.search.service.infrastructure.IndexParameters
import org.elasticsearch.client.RestHighLevelClient

class VideoIndexWriter private constructor(
    client: RestHighLevelClient,
    indexParameters: IndexParameters,
    batchSize: Int
) :
    AbstractIndexWriter<VideoMetadata>(
        VideoIndexConfiguration(),
        client,
        indexParameters,
        VideosIndex,
        batchSize,
        ngram = false
    ) {
    companion object {
        fun createTestInstance(client: RestHighLevelClient, batchSize: Int): VideoIndexWriter =
            VideoIndexWriter(client, IndexParameters(numberOfShards = 1), batchSize)

        fun createInstance(
            client: RestHighLevelClient,
            indexParameters: IndexParameters,
            batchSize: Int
        ): VideoIndexWriter =
            VideoIndexWriter(client, indexParameters, batchSize)
    }

    override fun serializeToIndexDocument(entry: VideoMetadata) = VideoDocumentConverter.fromVideo(entry)

    override fun getIdentifier(entry: VideoMetadata) = entry.id
}
