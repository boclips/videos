package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.service.video.ContentEnrichers

object VideoMetadataConverter {
    fun convert(video: Video): VideoMetadata {
        return VideoMetadata(
            id = video.videoId.value,
            title = video.title,
            description = video.description,
            contentProvider = video.contentPartner.name,
            releaseDate = video.releasedOn,
            keywords = video.keywords,
            tags = tagsFrom(video),
            durationSeconds = video.playback.duration.seconds,
            source = convertPlaybackTypeToSourceType(video.playback.id.type),
            transcript = video.transcript,
            ageRangeMin = video.ageRange.min(),
            ageRangeMax = video.ageRange.max(),
            type = VideoTypeConverter.convert(video.type),
            subjects = video.subjects.map {SubjectMetadata(id = it.id.value, name = it.name)}.toSet(),
            promoted = video.promoted
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
            KALTURA -> SourceType.BOCLIPS
            YOUTUBE -> SourceType.YOUTUBE
        }
}
