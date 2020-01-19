package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.infrastructure.AbstractIndexWriter
import com.boclips.search.service.infrastructure.IndexParameters
import org.elasticsearch.client.RestHighLevelClient

class VideoIndexWriter private constructor(client: RestHighLevelClient, indexParameters: IndexParameters) :
    AbstractIndexWriter<VideoMetadata>(
        VideoIndexConfiguration(),
        client,
        indexParameters,
        VideosIndex
    ) {
    companion object {
        fun createTestInstance(client: RestHighLevelClient): VideoIndexWriter =
            VideoIndexWriter(client, IndexParameters(numberOfShards = 1))

        fun createInstance(client: RestHighLevelClient, indexParameters: IndexParameters): VideoIndexWriter =
            VideoIndexWriter(client, indexParameters)
    }

    override fun serializeToIndexDocument(entry: VideoMetadata) = VideoDocumentConverter.fromVideo(entry)

    override fun getIdentifier(entry: VideoMetadata) = entry.id
}
