package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.infrastructure.AbstractESWriteSearchService
import org.elasticsearch.client.RestHighLevelClient

class ESVideoWriteSearchService(client: RestHighLevelClient) : AbstractESWriteSearchService<VideoMetadata>(
    VideoIndexConfiguration(),
    client,
    ESVideosIndex
) {

    override fun convertToSerializableObject(entry: VideoMetadata) = ESVideo(
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
        subjects = entry.subjects
    )

    override fun getIdentifier(entry: VideoMetadata) = entry.id

}
