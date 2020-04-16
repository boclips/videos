package com.boclips.contentpartner.service.application.exceptions

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class InvalidContractException(contractId: ContentPartnerContractId) : BoclipsApiException(
    exceptionDetails = ExceptionDetails(
        error = "Invalid contract",
        message = "${contractId.value} is not a recognised contract id",
        status = HttpStatus.BAD_REQUEST
    )
)
