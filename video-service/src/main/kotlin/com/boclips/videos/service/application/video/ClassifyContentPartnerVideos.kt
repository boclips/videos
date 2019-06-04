package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository

class ClassifyContentPartnerVideos(
    private val videoRepository: VideoRepository,
    private val classifyVideo: ClassifyVideo
)
{
    operator fun invoke(contentPartner: String) {
        videoRepository.streamAll(VideoFilter.ContentPartnerIs(contentPartner)) { videos ->
            videos.forEach { video ->
                classifyVideo(video.videoId.value)
            }
        }
    }
}
