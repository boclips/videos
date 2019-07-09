package com.boclips.videos.service.application.exceptions

import com.boclips.web.exceptions.ExceptionDetails
import com.boclips.web.exceptions.InvalidRequestApiException
import org.springframework.http.HttpStatus

class SubjectExistsException(subjectName: String) : InvalidRequestApiException(
    ExceptionDetails(
        error = "This subject already exists",
        message = "The subject $subjectName already exists",
        status = HttpStatus.CONFLICT
    )
)