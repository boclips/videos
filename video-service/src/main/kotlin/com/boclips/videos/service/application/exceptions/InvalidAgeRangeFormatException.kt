package com.boclips.videos.service.application.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class InvalidAgeRangeFormatException(ageRange: String) : BoclipsApiException(
    exceptionDetails = ExceptionDetails(
        error = "Invalid age range format",
        message = "$ageRange is not a recognised age range",
        status = HttpStatus.BAD_REQUEST
    )
)
