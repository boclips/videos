package com.boclips.videos.service.domain.model.video

class UnsupportedCaptionsException(video: Video) :
    RuntimeException("Captions for video ${video.videoId} is not supported")
