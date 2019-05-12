package com.boclips.videos.service.application.exceptions

import com.boclips.videos.service.application.video.exceptions.VideoServiceException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
open class InvalidCreateRequestException(message: String) : VideoServiceException(message)

class NonNullableFieldCreateRequestException(fieldName: String) :
    InvalidCreateRequestException("$fieldName cannot be null") {
    companion object {
        fun <T> getOrThrow(fieldValue: T?, fieldName: String): T =
            fieldValue ?: throw NonNullableFieldCreateRequestException(fieldName)
    }
}