package com.boclips.videos.service.domain.service.video

import com.boclips.search.service.domain.legacy.LegacyVideoMetadata
import com.boclips.videos.service.domain.model.Video

object VideoToLegacyVideoMetadataConverter {

    fun convert(video: Video): LegacyVideoMetadata {
        return LegacyVideoMetadata(
            id = video.videoId.value,
            title = video.title,
            description = video.description,
            keywords = video.keywords,
            duration = video.playback.duration,
            contentPartnerName = video.contentPartnerName,
            contentPartnerVideoId = video.contentPartnerVideoId,
            releaseDate = video.releasedOn,
            videoTypeTitle = video.type.title
        )
    }
}