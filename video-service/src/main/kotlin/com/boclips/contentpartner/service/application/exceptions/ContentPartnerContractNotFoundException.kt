package com.boclips.contentpartner.service.application.exceptions

import com.boclips.web.exceptions.ResourceNotFoundApiException

class ContentPartnerContractNotFoundException(message: String) : ResourceNotFoundApiException(
    error = "Could not find content partner",
    message = message
)
