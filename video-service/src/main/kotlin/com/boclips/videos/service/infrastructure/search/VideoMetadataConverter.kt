package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.SourceType
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.ContentEnrichers
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType

object VideoMetadataConverter {
    fun convert(video: Video): VideoMetadata {
        return VideoMetadata(
            id = video.videoId.value,
            title = video.title,
            description = video.description,
            contentProvider = video.contentPartnerId,
            releaseDate = video.releasedOn,
            keywords = video.keywords,
            tags = tagsFrom(video),
            durationSeconds = video.playback.duration.seconds,
            source = convertPlaybackTypeToSourceType(video.playback.id.type),
            transcript = video.transcript
        )
    }

    private fun tagsFrom(video: Video): List<String> {
        return listOf(
            Pair("classroom", ContentEnrichers.isClassroom(video)),
            Pair("news", ContentEnrichers.isNews(video))
        ).fold(emptyList()) { acc, pair -> if (pair.second) acc.plus(pair.first) else acc }
    }

    private fun convertPlaybackTypeToSourceType(playbackType: PlaybackProviderType): SourceType =
        when (playbackType) {
            PlaybackProviderType.YOUTUBE -> SourceType.YOUTUBE
            PlaybackProviderType.KALTURA -> SourceType.BOCLIPS
        }
}
