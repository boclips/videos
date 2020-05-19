package com.boclips.videos.service.domain.model.video

class InsufficientVideoResolutionException(videoId: VideoId) : Exception(
    "The video=$videoId has neither the original asset nor a high-res version and this operation cannot be performed"
)
