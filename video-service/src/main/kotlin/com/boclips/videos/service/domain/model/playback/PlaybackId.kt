package com.boclips.videos.service.domain.model.playback

import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound

data class PlaybackId(val type: PlaybackProviderType, val value: String) {
    companion object {
        fun from(playbackId: String?, playbackProviderName: String?): PlaybackId {
            if (playbackId != null && playbackProviderName != null) {
                return PlaybackId(
                    type = PlaybackProviderType.valueOf(playbackProviderName),
                    value = playbackId
                )
            } else {
                throw VideoPlaybackNotFound(message = "Illegal playback id with $playbackProviderName and $playbackId")
            }
        }
    }
}
