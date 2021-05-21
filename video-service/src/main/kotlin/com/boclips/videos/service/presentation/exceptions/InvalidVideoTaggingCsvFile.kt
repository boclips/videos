package com.boclips.videos.service.presentation.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class InvalidVideoTaggingCsvFile(message: String) : BoclipsApiException(
    ExceptionDetails(
        message = message,
        error = message,
        status = HttpStatus.BAD_REQUEST
    )
)
