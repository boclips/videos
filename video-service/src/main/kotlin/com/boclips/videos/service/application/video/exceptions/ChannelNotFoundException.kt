package com.boclips.videos.service.application.video.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class ChannelNotFoundException(channelId: String) : BoclipsApiException(
    exceptionDetails = ExceptionDetails(
        error = "Could not find channel",
        message = "Invalid channelId: $channelId",
        status = HttpStatus.BAD_REQUEST
    )
)
