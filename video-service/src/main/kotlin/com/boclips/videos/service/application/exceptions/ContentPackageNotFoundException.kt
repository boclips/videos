package com.boclips.videos.service.application.exceptions

import com.boclips.web.exceptions.ExceptionDetails
import com.boclips.web.exceptions.InvalidRequestApiException
import org.springframework.http.HttpStatus

class ContentPackageNotFoundException(contentPackageId: String) : InvalidRequestApiException(
    ExceptionDetails(
        error = "This content package was not found.",
        message = "The content package $contentPackageId was not found",
        status = HttpStatus.NOT_FOUND
    )
)
