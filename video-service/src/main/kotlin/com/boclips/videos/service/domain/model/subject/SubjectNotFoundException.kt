package com.boclips.videos.service.domain.model.subject

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class SubjectNotFoundException(subjectId: String) : BoclipsApiException(
    ExceptionDetails(
        error = "Not found",
        message = "Subject not found for id '$subjectId'",
        status = HttpStatus.NOT_FOUND
    )
)
