package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.infrastructure.AbstractIndexWriter
import org.elasticsearch.client.RestHighLevelClient

class VideoIndexWriter(client: RestHighLevelClient) : AbstractIndexWriter<VideoMetadata>(
    VideoIndexConfiguration(),
    client,
    VideosIndex
) {
    override fun serializeToIndexDocument(entry: VideoMetadata) = VideoDocument(
        id = entry.id,
        title = entry.title,
        description = entry.description,
        contentProvider = entry.contentProvider,
        releaseDate = entry.releaseDate,
        keywords = entry.keywords,
        tags = entry.tags,
        durationSeconds = entry.durationSeconds,
        source = entry.source.name,
        transcript = entry.transcript,
        ageRangeMax = entry.ageRangeMax,
        ageRangeMin = entry.ageRangeMin,
        subjectIds = entry.subjects.map { subject -> subject.id }.toSet()
    )

    override fun getIdentifier(entry: VideoMetadata) = entry.id
}
