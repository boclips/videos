package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.*
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.domain.model.video.BaseVideo
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.channel.Availability
import com.boclips.videos.service.domain.service.video.ContentEnrichers

object VideoMetadataConverter {
    fun convert(video: Video, videoAvailability: Availability): VideoMetadata {
        val subjects = video.subjects.items
            .map { SubjectMetadata(id = it.id.value, name = it.name) }
            .toSet()

        return VideoMetadata(
            id = video.videoId.value,
            title = video.title,
            rawTitle = video.title,
            description = video.description,
            contentProvider = video.channel.name,
            contentPartnerId = video.channel.channelId.value,
            releaseDate = video.releasedOn,
            keywords = video.keywords,
            tags = tagsFrom(video),
            durationSeconds = video.playback.duration.seconds,
            source = convertPlaybackTypeToSourceType(video.playback.id.type),
            transcript = video.voice.transcript,
            isVoiced = video.isVoiced(),
            ageRangeMin = video.ageRange.min(),
            ageRangeMax = video.ageRange.max(),
            types = video.types.map { VideoTypeConverter.convert(it) },
            subjects = SubjectsMetadata(
                items = subjects,
                setManually = video.subjects.setManually
            ),
            promoted = video.promoted,
            meanRating = video.getRatingAverage(),
            eligibleForStream = videoAvailability.isStreamable(),
            eligibleForDownload = videoAvailability.isDownloadable(),
            attachmentTypes = attachmentTypes(video.attachments),
            deactivated = video.deactivated,
            ingestedAt = video.ingestedAt
        )
    }

    private fun tagsFrom(video: Video): List<String> {
        val tags = video.tags.map { it.tag.label }.toMutableList()

        if (ContentEnrichers.isNews(video)) {
            tags.add("news")
        }

        return tags.toList()
    }

    private fun attachmentTypes(attachments: List<Attachment>): Set<String> =
        attachments.mapTo(HashSet()) { it.type.label }

    private fun convertPlaybackTypeToSourceType(playbackType: PlaybackProviderType): SourceType =
        when (playbackType) {
            KALTURA -> SourceType.BOCLIPS
            YOUTUBE -> SourceType.YOUTUBE
        }
}
