package com.boclips.videos.service.application.video

import com.boclips.kalturaclient.KalturaCaptionManager.CaptionStatus.*
import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.api.response.video.CaptionStatus
import com.boclips.videos.api.response.video.CaptionsResource
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.CaptionService
import mu.KLogging

class VideoCaptionService(
    val kalturaClient: KalturaClient,
    val captionService: CaptionService) {
    companion object : KLogging()

    fun withCaptionDetails(video: VideoResource): VideoResource {
        val status = video.playback?.id?.let { kalturaClient.getCaptionStatus(it) }
        return video.copy(
            captionStatus = when (status) {
                REQUESTED -> CaptionStatus.REQUESTED
                PROCESSING -> CaptionStatus.PROCESSING
                AVAILABLE -> CaptionStatus.AVAILABLE
                NOT_AVAILABLE -> CaptionStatus.NOT_AVAILABLE
                else -> CaptionStatus.UNKNOWN
            }
        )
    }

    fun getCaptionContent(videoId: String): CaptionsResource? {
        return captionService.getCaptionContent(VideoId(videoId))?.let { CaptionsResource(content = it) }
    }

    fun requestCaptionIfMissing(videoId: String?) {
        videoId?.let { captionService.requestCaption(VideoId(it)) }
    }
}
