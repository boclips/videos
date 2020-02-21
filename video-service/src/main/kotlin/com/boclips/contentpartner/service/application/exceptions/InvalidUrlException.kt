package com.boclips.contentpartner.service.application.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class InvalidUrlException(url: String) : BoclipsApiException(
    exceptionDetails = ExceptionDetails(
        error = "Invalid URL exception",
        message = "$url is a malformed URL",
        status = HttpStatus.BAD_REQUEST
    )
)
