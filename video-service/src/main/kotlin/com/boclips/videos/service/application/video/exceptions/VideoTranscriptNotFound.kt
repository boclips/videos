package com.boclips.videos.service.application.video.exceptions

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.web.exceptions.ResourceNotFoundApiException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class VideoTranscriptNotFound(val videoId: VideoId) :
    ResourceNotFoundApiException(
        error = "Not Found",
        message = "Video transcript for video '${videoId.value}' not found."
    )