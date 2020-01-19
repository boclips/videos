package com.boclips.videos.service.application.exceptions

import com.boclips.web.exceptions.ExceptionDetails
import com.boclips.web.exceptions.InvalidRequestApiException
import org.springframework.http.HttpStatus

class TagExistsException(tagName: String) : InvalidRequestApiException(
    ExceptionDetails(
        error = "This tag already exists",
        message = "The tag $tagName already exists",
        status = HttpStatus.CONFLICT
    )
)
