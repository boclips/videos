package com.boclips.videos.service.application.video.exceptions

open class VideoServiceException(message: String?) : RuntimeException(message) {
    constructor() : this(null)
}