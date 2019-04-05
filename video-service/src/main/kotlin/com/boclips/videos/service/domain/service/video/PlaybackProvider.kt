package com.boclips.videos.service.domain.service.video

import com.boclips.events.types.Captions
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.VideoPlayback

interface PlaybackProvider {
    fun retrievePlayback(playbackIds: List<PlaybackId>): Map<PlaybackId, VideoPlayback>
    fun removePlayback(playbackId: PlaybackId)
    fun uploadCaptions(playbackId: PlaybackId, captions: Captions)
}
