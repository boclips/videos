package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback

class PlaybackService(val kalturaPlaybackProvider: PlaybackProvider, val youtubePlaybackProvider: PlaybackProvider) {

    fun getPlaybacks(playbackIds: List<PlaybackId>): Map<PlaybackId, VideoPlayback> {
        val kalturaPlaybackById = kalturaPlaybackProvider.retrievePlayback(playbackIds.filter { playbackId -> playbackId.type == PlaybackProviderType.KALTURA }.map { playbackId -> playbackId.value })
        val youtubePlaybackById = youtubePlaybackProvider.retrievePlayback(playbackIds.filter { playbackId -> playbackId.type == PlaybackProviderType.YOUTUBE }.map { playbackId -> playbackId.value })

        return playbackIds.mapNotNull { playbackId ->
            val playback = when (playbackId.type) {
                PlaybackProviderType.KALTURA -> kalturaPlaybackById[playbackId.value]
                PlaybackProviderType.YOUTUBE -> youtubePlaybackById[playbackId.value]
            } ?: return@mapNotNull null
            (playbackId to playback)
        }.toMap()
    }

    fun getPlayback(playbackId: PlaybackId): VideoPlayback? {
        return getPlaybacks(listOf(playbackId)).values.firstOrNull()
    }

    fun removePlayback(playbackId: PlaybackId) {
        kalturaPlaybackProvider.removePlayback(playbackId.value)
    }
}