package com.boclips.videos.service.application.video.exceptions

class InvalidDurationException(duration: String) : VideoServiceException("$duration is not a valid ISO 8601 duration. Example is PT5S.")
