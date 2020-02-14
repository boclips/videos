package com.boclips.contentpartner.service.application.exceptions

import com.boclips.contentpartner.service.domain.model.EduAgeRangeId
import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class InvalidEduAgeRangeException(eduAgeRangeId: EduAgeRangeId) : BoclipsApiException(
    exceptionDetails = ExceptionDetails(
        error = "Invalid age range",
        message = "${eduAgeRangeId.value} is not a recognised age range",
        status = HttpStatus.BAD_REQUEST
    )
)
