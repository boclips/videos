package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository

class ClassifyContentPartnerVideos(
    private val videoRepository: VideoRepository,
    private val classifyVideo: ClassifyVideo
)
{
    operator fun invoke(contentPartner: String?) {
        val filter = contentPartner?.let { VideoFilter.ContentPartnerIs(it) } ?: VideoFilter.IsSearchable
        videoRepository.streamAll(filter) { videos ->
            videos.forEach { video ->
                if(video.type == LegacyVideoType.INSTRUCTIONAL_CLIPS) {
                    classifyVideo(video.videoId.value)
                }
            }
        }
    }
}
