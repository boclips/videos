package com.boclips.videos.service.domain.model.video

class NoVideoAssetsException private constructor(message: String) : Exception(message) {
    constructor(videoId: VideoId) : this("The video=$videoId has no assets attached and this operation cannot be performed")
}
