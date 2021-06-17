package com.boclips.videos.service.application.video

import com.boclips.kalturaclient.KalturaCaptionManager.CaptionStatus.AUTO_GENERATED_AVAILABLE
import com.boclips.kalturaclient.KalturaCaptionManager.CaptionStatus.HUMAN_GENERATED_AVAILABLE
import com.boclips.kalturaclient.KalturaCaptionManager.CaptionStatus.NOT_AVAILABLE
import com.boclips.kalturaclient.KalturaCaptionManager.CaptionStatus.PROCESSING
import com.boclips.kalturaclient.KalturaCaptionManager.CaptionStatus.REQUESTED
import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.api.request.video.CaptionFormatRequest
import com.boclips.videos.api.response.video.CaptionStatus
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.model.video.CaptionFormat
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.CaptionConverter
import com.boclips.videos.service.domain.service.video.CaptionService
import mu.KLogging

class VideoCaptionService(
    private val kalturaClient: KalturaClient,
    private val captionService: CaptionService,
    private val captionConverter: CaptionConverter
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

    fun getCaption(
        videoId: String,
        humanGeneratedOnly: Boolean,
        captionFormatRequest: CaptionFormatRequest?
    ): Caption? {
        val requestedCaptionFormat = when (captionFormatRequest) {
            CaptionFormatRequest.SRT -> CaptionFormat.SRT
            CaptionFormatRequest.VTT -> CaptionFormat.WEBVTT
            null -> null
        }

        return captionService.getCaption(VideoId(videoId), humanGeneratedOnly)?.let { retrievedCaption ->
            if (requestedCaptionFormat != null) {
                val convertedCaptionContent = captionConverter.convert(
                    retrievedCaption.content,
                    from = retrievedCaption.format,
                    to = requestedCaptionFormat
                )
                retrievedCaption.copy(content = convertedCaptionContent, format = requestedCaptionFormat)
            } else {
                retrievedCaption
            }
        }
    }

    fun requestCaptionIfMissing(videoId: String?) {
        videoId?.let { captionService.requestCaption(VideoId(it)) }
    }
}
