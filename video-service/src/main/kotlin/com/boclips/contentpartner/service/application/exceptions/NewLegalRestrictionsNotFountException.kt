package com.boclips.contentpartner.service.application.exceptions

import com.boclips.web.exceptions.ResourceNotFoundApiException

class NewLegalRestrictionsNotFountException(message: String) : ResourceNotFoundApiException(
    error = "Could not find legal restriction",
    message = message
)

