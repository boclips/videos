package com.boclips.videos.service.application.video.exceptions

import com.boclips.web.exceptions.ExceptionDetails
import com.boclips.web.exceptions.InvalidRequestApiException
import org.springframework.http.HttpStatus

class VideoExists(val contentPartnerId: String, val contentPartnerVideoId: String) : InvalidRequestApiException(
    ExceptionDetails(
        error = "This video already exists",
        message = "The video $contentPartnerVideoId for the content partner $contentPartnerId already exists",
        status = HttpStatus.CONFLICT
    )
)
