package com.boclips.videos.service.domain.model.playback

enum class PlaybackProvider {
    KALTURA,
    YOUTUBE
}

class PlaybackId(val playbackProvider: PlaybackProvider, val playbackId: String)
