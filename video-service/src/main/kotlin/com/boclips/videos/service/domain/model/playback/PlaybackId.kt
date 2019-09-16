package com.boclips.videos.service.domain.model.playback

import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.presentation.video.CreateVideoRequest

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

        fun fromCreateVideoRequest(createVideoRequest: CreateVideoRequest): PlaybackId {
            var playbackProvider: PlaybackProviderType? =
                createVideoRequest.playbackProvider?.let { PlaybackProviderType.valueOf(it) }
            var playbackId: String? = createVideoRequest.playbackId

            if (PlaybackProviderType.KALTURA == playbackProvider) {
                if (createVideoRequest.kalturaEntryId != null) {
                    playbackId = createVideoRequest.kalturaEntryId
                } else {
                    playbackProvider = PlaybackProviderType.KALTURA_REFERENCE
                    playbackId = createVideoRequest.playbackId
                }
            } else if (PlaybackProviderType.KALTURA_REFERENCE == playbackProvider) {
                playbackId = createVideoRequest.kalturaReferenceId
            }

            return from(playbackId, playbackProvider?.name)
        }
    }
}
