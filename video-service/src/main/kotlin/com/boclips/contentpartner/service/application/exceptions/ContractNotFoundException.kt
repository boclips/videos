package com.boclips.contentpartner.service.application.exceptions

import com.boclips.web.exceptions.ResourceNotFoundApiException

class ContractNotFoundException(message: String) : ResourceNotFoundApiException(
    error = "Could not find contract content partner",
    message = message
)
