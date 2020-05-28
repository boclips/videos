package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.playback.PlaybackId

class InsufficientVideoResolutionException private constructor(message: String) : Exception(message) {
    constructor(videoId: VideoId) : this("The video=$videoId has neither the original asset nor a high-res version and this operation cannot be performed")
    constructor(playbackId: PlaybackId) : this("The video with playback playback=$playbackId has neither the original asset nor a high-res version and this operation cannot be performed")
}
