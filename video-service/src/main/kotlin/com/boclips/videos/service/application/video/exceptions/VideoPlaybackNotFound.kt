package com.boclips.videos.service.application.video.exceptions

import com.boclips.videos.service.presentation.video.CreateVideoRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class VideoPlaybackNotFound(override val message: String? = null) : VideoServiceException() {
    constructor(createRequest: CreateVideoRequest) :
        this("Video playback for video '${createRequest.playbackId}' not found in '${createRequest.playbackProvider}'")
}
