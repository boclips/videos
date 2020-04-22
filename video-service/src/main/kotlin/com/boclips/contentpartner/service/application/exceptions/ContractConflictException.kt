package com.boclips.contentpartner.service.application.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class ContractConflictException(contractName: String) : BoclipsApiException(
    ExceptionDetails(
        error = "Contract conflict",
        message = "There's already a contract named '$contractName'",
        status = HttpStatus.CONFLICT
    )
)
