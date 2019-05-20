package com.boclips.videos.service.application.video.exceptions

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.web.exceptions.ResourceNotFoundApiException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class VideoNotFoundException(val id: VideoId? = null) : ResourceNotFoundApiException(error = "Not Found", message = id?.let { "Video $it not found" } ?: "Video not found")