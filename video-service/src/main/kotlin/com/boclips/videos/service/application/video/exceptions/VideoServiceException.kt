package com.boclips.videos.service.application.video.exceptions

abstract class VideoServiceException(message: String?) : RuntimeException(message) {
    constructor() : this(null)
}