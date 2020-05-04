package com.boclips.videos.service.application.exceptions

import com.boclips.videos.service.application.video.exceptions.VideoServiceException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class CollectionCreationException(message: String) : VideoServiceException(message)
