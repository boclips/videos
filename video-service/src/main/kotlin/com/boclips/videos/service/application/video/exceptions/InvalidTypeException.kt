package com.boclips.videos.service.application.video.exceptions

import com.boclips.search.service.domain.videos.model.VideoType

class InvalidTypeException(type: String, validSources: Array<VideoType>) :
    VideoServiceException("$type is not valid type. The valid options are $validSources")
