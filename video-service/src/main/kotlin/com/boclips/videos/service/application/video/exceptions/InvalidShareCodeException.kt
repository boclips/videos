package com.boclips.videos.service.application.video.exceptions

class InvalidShareCodeException(shareCode: String, videoId: String) :
    VideoServiceException("$shareCode is not a valid for the video: $videoId")
