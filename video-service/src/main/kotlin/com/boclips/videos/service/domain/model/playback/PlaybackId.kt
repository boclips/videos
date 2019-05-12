package com.boclips.videos.service.domain.model.playback

import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound

data class PlaybackId(val type: PlaybackProviderType, val value: String) {
    companion object {
        fun from(providerVideoId: String?, providerName: String?): PlaybackId {
            if (providerVideoId != null && providerName != null) {
                return PlaybackId(
                    type = PlaybackProviderType.valueOf(providerName),
                    value = providerVideoId
                )
            } else {
                throw VideoPlaybackNotFound(message = "Illegal playback id with $providerName and $providerVideoId")
            }
        }
    }
}
