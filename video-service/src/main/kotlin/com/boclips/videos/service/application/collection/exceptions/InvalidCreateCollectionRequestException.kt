package com.boclips.videos.service.application.collection.exceptions

import com.boclips.videos.service.application.video.exceptions.VideoServiceException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidCreateCollectionRequestException(message: String) : VideoServiceException(message)