package com.boclips.videos.service.infrastructure.search

import com.boclips.kalturaclient.MediaEntry
import com.boclips.kalturaclient.streams.StreamFormat
import com.boclips.videos.service.domain.model.Video
import java.time.LocalDate

object VideoInformationAggregator {

    fun convert(videos: List<ElasticSearchVideo>, mediaEntries: Map<String, MediaEntry>): List<Video> {
        return videos
                .mapNotNull {
                    val mediaEntry = mediaEntries[it.referenceId] ?: return@mapNotNull null
                    convert(elasticSearchVideo = it, mediaEntry = mediaEntry)
                }
    }

    fun convert(elasticSearchVideo: ElasticSearchVideo, mediaEntry: MediaEntry): Video {
        return Video(
                id = elasticSearchVideo.id,
                title = elasticSearchVideo.title,
                description = elasticSearchVideo.description,
                contentProvider = elasticSearchVideo.source,
                duration = mediaEntry.duration,
                releasedOn = LocalDate.parse(elasticSearchVideo.date),
                streamUrl = mediaEntry.streams.withFormat(StreamFormat.MPEG_DASH),
                thumbnailUrl = mediaEntry.thumbnailUrl
        )
    }

}