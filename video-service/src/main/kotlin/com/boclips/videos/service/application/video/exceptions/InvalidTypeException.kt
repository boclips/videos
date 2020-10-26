package com.boclips.videos.service.application.video.exceptions

import com.boclips.search.service.domain.channels.model.ContentType
import com.boclips.search.service.domain.videos.model.VideoType

class InvalidTypeException {
    companion object {
        fun videoType(type: String, validSources: Array<VideoType>): Throwable {
            return VideoServiceException("$type is not valid type. The valid options are ${validSources.map { it.name }}")
        }

        fun contentType(type: String, validSources: Array<ContentType>): Throwable {
            return VideoServiceException("$type is not valid type. The valid options are ${validSources.map { it.name }}")
        }
    }
}