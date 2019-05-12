package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository
import java.util.Locale

class AnalyseContentPartnerVideos(
    private val videoRepository: VideoRepository,
    private val analyseVideo: AnalyseVideo
) {
    operator fun invoke(contentPartner: String, language: Locale?) {
        videoRepository.streamAll(VideoFilter.ContentPartnerIs(contentPartner)) { videos ->
            videos.forEach { video ->
                analyseVideo(video.videoId.value, language)
            }
        }
    }
}
