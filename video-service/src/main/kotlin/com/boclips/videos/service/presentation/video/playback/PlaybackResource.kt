package com.boclips.videos.service.presentation.video.playback

import java.time.Duration

abstract class PlaybackResource(val type: String) {
    var thumbnailUrl: String? = null
    var duration: Duration? = null
}