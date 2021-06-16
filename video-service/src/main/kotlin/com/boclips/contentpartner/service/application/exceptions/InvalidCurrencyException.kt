package com.boclips.contentpartner.service.application.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class InvalidCurrencyException(currency: String) : BoclipsApiException(
    exceptionDetails = ExceptionDetails(
        error = "Invalid currency exception",
        message = "$currency is a malformed currency code",
        status = HttpStatus.BAD_REQUEST
    )
)
