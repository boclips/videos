package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.VideoPlayback

interface PlaybackProvider {
    fun retrievePlayback(playbackIds: List<PlaybackId>): Map<PlaybackId, VideoPlayback>
    fun removePlayback(playbackIds: PlaybackId)
}