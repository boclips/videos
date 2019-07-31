package com.boclips.videos.service.application.contentPartner

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class ContentPartnerConflictException(contentPartnerName: String) : BoclipsApiException(
    ExceptionDetails(
        error = "Content partner conflict",
        message = "There's already a content partner named '$contentPartnerName'",
        status = HttpStatus.CONFLICT
    )
)
