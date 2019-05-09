package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.SourceType
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.ContentEnrichers
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType

object VideoMetadataConverter {
    fun convert(video: VideoAsset): VideoMetadata {
        return VideoMetadata(
            id = video.assetId.value,
            title = video.title,
            description = video.description,
            contentProvider = video.contentPartnerId,
            releaseDate = video.releasedOn,
            keywords = video.keywords,
            tags = tagsFrom(video),
            durationSeconds = video.duration.seconds,
            source = convertPlaybackTypeToSourceType(video.playbackId.type)
        )
    }

    private fun tagsFrom(video: VideoAsset): List<String> {
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
