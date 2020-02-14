package com.boclips.contentpartner.service.application.exceptions

import com.boclips.web.exceptions.ResourceNotFoundApiException

class AgeRangeNotFoundException(idValue: String) : ResourceNotFoundApiException(
    error = "Age range not found",
    message = "No age range found for this id: $idValue"
)
