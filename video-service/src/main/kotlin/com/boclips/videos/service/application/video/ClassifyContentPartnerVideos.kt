package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository
import mu.KLogging

class ClassifyContentPartnerVideos(
    private val videoRepository: VideoRepository,
    private val classifyVideo: ClassifyVideo
)
{
    companion object : KLogging()

    operator fun invoke(contentPartner: String?) {
        logger.info { "Requesting subject classification for all instructional videos: $contentPartner" }
        val filter = contentPartner?.let { VideoFilter.ContentPartnerIs(it) } ?: VideoFilter.LegacyTypeIs(LegacyVideoType.INSTRUCTIONAL_CLIPS)
        videoRepository.streamAll(filter) { videos ->
            videos
                .forEach { video ->
                    classifyVideo(video.videoId.value)
                }
        }
        logger.info { "Requested subject classification for all instructional videos: $contentPartner" }
    }
}
