package com.boclips.videos.service.domain.service.video

import com.boclips.search.service.domain.videos.legacy.LegacyVideoMetadata
import com.boclips.videos.service.domain.model.video.Video

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
            videoTypeTitle = video.types.first().title
        )
    }
}
