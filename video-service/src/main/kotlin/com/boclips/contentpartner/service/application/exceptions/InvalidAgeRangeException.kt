package com.boclips.contentpartner.service.application.exceptions

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class InvalidAgeRangeException(ageRangeId: AgeRangeId) : BoclipsApiException(
    exceptionDetails = ExceptionDetails(
        error = "Invalid age range",
        message = "${ageRangeId.value} is not a recognised age range",
        status = HttpStatus.BAD_REQUEST
    )
)


