package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.config.properties.ReindexProperties
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.infrastructure.AbstractIndexWriter
import com.boclips.search.service.infrastructure.IndexParameters
import org.elasticsearch.client.RestHighLevelClient

class VideoIndexWriter private constructor(
    client: RestHighLevelClient,
    indexParameters: IndexParameters,
    reindexProperties: ReindexProperties
) :
    AbstractIndexWriter<VideoMetadata>(
        VideoIndexConfiguration(),
        client,
        indexParameters,
        VideosIndex,
        reindexProperties
    ) {
    companion object {
        fun createTestInstance(client: RestHighLevelClient, reindexProperties: ReindexProperties): VideoIndexWriter =
            VideoIndexWriter(client, IndexParameters(numberOfShards = 1), reindexProperties)

        fun createInstance(
            client: RestHighLevelClient,
            indexParameters: IndexParameters,
            reindexProperties: ReindexProperties
        ): VideoIndexWriter =
            VideoIndexWriter(client, indexParameters, reindexProperties)
    }

    override fun serializeToIndexDocument(entry: VideoMetadata) = VideoDocumentConverter.fromVideo(entry)

    override fun getIdentifier(entry: VideoMetadata) = entry.id
}
