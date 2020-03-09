package com.boclips.contentpartner.service.application.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class ContentPartnerHubspotIdExceptions(hubspotId: String) : BoclipsApiException(
    ExceptionDetails(
        error = "Hubspot ID conflict",
        message = "There's already a content partner with this hubspot ID '$hubspotId'",
        status = HttpStatus.CONFLICT
    )
)


