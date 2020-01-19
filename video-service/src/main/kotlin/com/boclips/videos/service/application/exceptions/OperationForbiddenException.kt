package com.boclips.videos.service.application.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.FORBIDDEN)
class OperationForbiddenException(message: String? = null) : RuntimeException(message)
