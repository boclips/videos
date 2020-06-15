package com.boclips.contentpartner.service.application.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class ChannelConflictException(channelName: String) : BoclipsApiException(
    ExceptionDetails(
        error = "Channel conflict",
        message = "There's already a channel named '$channelName'",
        status = HttpStatus.CONFLICT
    )
)
