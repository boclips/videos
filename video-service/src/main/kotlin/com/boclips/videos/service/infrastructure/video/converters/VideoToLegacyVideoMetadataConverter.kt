package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.search.service.domain.videos.legacy.LegacyVideoMetadata
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoType

object VideoToLegacyVideoMetadataConverter {

    fun convert(video: Video): LegacyVideoMetadata {
        return LegacyVideoMetadata(
            id = video.videoId.value,
            title = video.title,
            description = video.description,
            keywords = video.keywords,
            duration = video.playback.duration,
            contentPartnerName = video.channel.name,
            contentPartnerVideoId = video.videoReference,
            releaseDate = video.releasedOn,
            videoTypeTitle = resolveTitle(video.types.first())
        )
    }

    private fun resolveTitle(videoType: VideoType) = when (videoType) {
        VideoType.NEWS -> "News"
        VideoType.STOCK -> "Stock"
        VideoType.INSTRUCTIONAL_CLIPS -> "Instructional Clips"
    }
}
