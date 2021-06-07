package com.boclips.videos.service.application.video

import com.boclips.kalturaclient.KalturaCaptionManager.CaptionStatus.*
import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.api.response.video.CaptionStatus
import com.boclips.videos.api.response.video.CaptionsResource
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.CaptionService
import mu.KLogging

class VideoCaptionService(
    val kalturaClient: KalturaClient,
    val captionService: CaptionService
) {
    companion object : KLogging()

    fun withCaptionDetails(video: VideoResource): VideoResource {
        val status = video.playback?.id?.let { kalturaClient.getCaptionStatus(it) }
        return video.copy(
            captionStatus = when (status) {
                REQUESTED -> CaptionStatus.REQUESTED
                PROCESSING -> CaptionStatus.PROCESSING
                HUMAN_GENERATED_AVAILABLE -> CaptionStatus.HUMAN_GENERATED_AVAILABLE
                AUTO_GENERATED_AVAILABLE -> CaptionStatus.AUTO_GENERATED_AVAILABLE
                NOT_AVAILABLE -> CaptionStatus.NOT_AVAILABLE
                else -> CaptionStatus.UNKNOWN
            }
        )
    }

    fun getCaption(videoId: String, humanGeneratedOnly: Boolean): Caption? {
        return captionService.getCaption(VideoId(videoId), humanGeneratedOnly)
    }

    fun requestCaptionIfMissing(videoId: String?) {
        videoId?.let { captionService.requestCaption(VideoId(it)) }
    }
}
