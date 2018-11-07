package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.playback.VideoPlayback

interface PlaybackProvider {
    fun retrievePlayback(videoIds: List<String>): Map<String, VideoPlayback>
    fun removePlayback(videoId: String)
}