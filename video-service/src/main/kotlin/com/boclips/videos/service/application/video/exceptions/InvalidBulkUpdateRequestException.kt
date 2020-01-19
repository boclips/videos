package com.boclips.videos.service.application.video.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidBulkUpdateRequestException(message: String) : VideoServiceException(message)
