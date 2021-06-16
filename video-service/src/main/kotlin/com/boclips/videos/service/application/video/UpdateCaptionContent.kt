package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.CaptionService
import mu.KLogging

class UpdateCaptionContent(private val captionService: CaptionService) {
    companion object : KLogging()

    operator fun invoke(videoId: String, captionContent: String) {
        captionService.updateCaptionContent(VideoId(videoId), captionContent)
    }
}
