package com.boclips.videos.service.application.analytics

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails

class InvalidEventException(message: String) :
    BoclipsApiException(ExceptionDetails(error = "Invalid Event", message = message))
