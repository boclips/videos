package com.boclips.videos.service.application.video.exceptions

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.web.exceptions.ResourceNotFoundApiException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class VideoPlaybackNotFound(override val message: String? = null) :
    ResourceNotFoundApiException(error = "Not Found", message = message.orEmpty()) {
    constructor(playbackId: PlaybackId) :
        this("Video playback for video '${playbackId.value}' not found for type '${playbackId.type}'")
}
