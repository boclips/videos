package com.boclips.videos.service.application.video

import com.boclips.kalturaclient.KalturaCaptionManager.CaptionStatus.AVAILABLE
import com.boclips.kalturaclient.KalturaCaptionManager.CaptionStatus.NOT_AVAILABLE
import com.boclips.kalturaclient.KalturaCaptionManager.CaptionStatus.PROCESSING
import com.boclips.kalturaclient.KalturaCaptionManager.CaptionStatus.REQUESTED
import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.api.response.video.CaptionStatus
import com.boclips.videos.api.response.video.VideoResource
import mu.KLogging

class VideoCaptionService(val kalturaClient: KalturaClient) {
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
}
