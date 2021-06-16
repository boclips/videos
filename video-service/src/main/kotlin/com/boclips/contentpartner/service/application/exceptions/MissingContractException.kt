package com.boclips.contentpartner.service.application.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class MissingContractException() : BoclipsApiException(
    exceptionDetails = ExceptionDetails(
        error = "Missing Contract",
        message = "Non Youtube channels must have a contract, please contact the acquisition team to create one",
        status = HttpStatus.BAD_REQUEST
    )
)
