package com.boclips.videos.service.domain.model.playback

interface PlaybackProvider {
    fun retrievePlayback(videoIds: List<String>): Map<String, VideoPlayback>
    fun removePlayback(videoId: String)
}