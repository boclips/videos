package com.boclips.videos.service.application.video.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails

open class VideoServiceException(message: String?) :
    BoclipsApiException(ExceptionDetails(error = "Invalid request", message = message.orEmpty())) {
    constructor() : this(null)
}
