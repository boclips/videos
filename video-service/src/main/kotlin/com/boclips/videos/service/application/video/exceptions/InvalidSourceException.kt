package com.boclips.videos.service.application.video.exceptions

class InvalidSourceException(source: String) :
    VideoServiceException("$source is not a valid source. The valid options are youtube or boclips")
