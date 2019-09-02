package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.infrastructure.AbstractIndexWriter
import org.elasticsearch.client.RestHighLevelClient

class VideoIndexWriter(client: RestHighLevelClient) : AbstractIndexWriter<VideoMetadata>(
    VideoIndexConfiguration(),
    client,
    VideosIndex
) {
    override fun serializeToIndexDocument(entry: VideoMetadata) = VideoDocumentConverter.fromVideo(entry)

    override fun getIdentifier(entry: VideoMetadata) = entry.id
}
