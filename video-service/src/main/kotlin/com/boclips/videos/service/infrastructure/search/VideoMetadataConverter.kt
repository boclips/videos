package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.SubjectsMetadata
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.domain.model.video.contentpartner.Availability
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.service.video.ContentEnrichers

object VideoMetadataConverter {
    fun convert(video: Video, videoAvailability: Availability): VideoMetadata {
        val subjects = video.subjects.items
            .map { SubjectMetadata(id = it.id.value, name = it.name) }
            .toSet()

        return VideoMetadata(
            id = video.videoId.value,
            title = video.title,
            description = video.description,
            contentProvider = video.contentPartner.name,
            contentPartnerId = video.contentPartner.contentPartnerId.value,
            releaseDate = video.releasedOn,
            keywords = video.keywords,
            tags = tagsFrom(video),
            durationSeconds = video.playback.duration.seconds,
            source = convertPlaybackTypeToSourceType(video.playback.id.type),
            transcript = video.transcript,
            ageRangeMin = video.ageRange.min(),
            ageRangeMax = video.ageRange.max(),
            type = VideoTypeConverter.convert(video.type),
            subjects = SubjectsMetadata(
                items = subjects,
                setManually = video.subjects.setManually
            ),
            promoted = video.promoted,
            meanRating = video.getRatingAverage(),
            eligibleForStream = videoAvailability.isStreamable(),
            eligibleForDownload = videoAvailability.isDownloadable()
        )
    }

    private fun tagsFrom(video: Video): List<String> {
        val tags = video.tags.map { it.tag.label }.toMutableList()

        if (ContentEnrichers.isNews(video)) {
            tags.add("news")
        }

        return tags.toList()
    }

    private fun convertPlaybackTypeToSourceType(playbackType: PlaybackProviderType): SourceType =
        when (playbackType) {
            KALTURA -> SourceType.BOCLIPS
            YOUTUBE -> SourceType.YOUTUBE
        }
}
