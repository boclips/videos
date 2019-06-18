package com.boclips.videos.service.domain.model.video

import com.boclips.web.exceptions.ResourceNotFoundApiException

class IllegalVideoIdentifierException(message: String) : ResourceNotFoundApiException("Video not found", message)
