package com.boclips.videos.service.domain.model.playback

import com.boclips.videos.service.domain.service.PlaybackProvider

class PlaybackRespository(
        val kalturaPlaybackProvider: PlaybackProvider,
        val youtubePlaybackProvider: PlaybackProvider) {

    fun find(playbackIds: List<PlaybackId>): Map<PlaybackId, VideoPlayback> {
        val kalturaPlaybackById = kalturaPlaybackProvider.retrievePlayback(playbackIds.filter { playbackId -> playbackId.type == PlaybackProviderType.KALTURA })
        val youtubePlaybackById = youtubePlaybackProvider.retrievePlayback(playbackIds.filter { playbackId -> playbackId.type == PlaybackProviderType.YOUTUBE })

        return playbackIds.mapNotNull { playbackId ->
            val playback = when (playbackId.type) {
                PlaybackProviderType.KALTURA -> kalturaPlaybackById[playbackId]
                PlaybackProviderType.YOUTUBE -> youtubePlaybackById[playbackId]
            } ?: return@mapNotNull null
            (playbackId to playback)
        }.toMap()
    }

    fun find(playbackId: PlaybackId): VideoPlayback? {
        return find(listOf(playbackId)).values.firstOrNull()
    }

    fun remove(playbackId: PlaybackId) {
        kalturaPlaybackProvider.removePlayback(playbackId)
    }
}