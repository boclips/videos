package com.boclips.videos.service.domain.model.playback

import com.boclips.events.types.Captions
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.*
import com.boclips.videos.service.domain.service.video.PlaybackProvider

class PlaybackRepository(
    private val kalturaPlaybackProvider: PlaybackProvider,
    private val youtubePlaybackProvider: PlaybackProvider
) {

    fun find(playbackIds: List<PlaybackId>): Map<PlaybackId, VideoPlayback> {
        val kalturaPlaybackById =
            kalturaPlaybackProvider.retrievePlayback(playbackIds.filter { playbackId -> playbackId.type == KALTURA })
        val youtubePlaybackById =
            youtubePlaybackProvider.retrievePlayback(playbackIds.filter { playbackId -> playbackId.type == YOUTUBE })

        return playbackIds.mapNotNull { playbackId ->
            val playback = when (playbackId.type) {
                KALTURA -> kalturaPlaybackById[playbackId]
                YOUTUBE -> youtubePlaybackById[playbackId]
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

    fun uploadCaptions(playbackId: PlaybackId, captions: Captions) {
        return getProvider(playbackId).uploadCaptions(playbackId, captions)
    }

    private fun getProvider(playbackId: PlaybackId): PlaybackProvider {
        return when(playbackId.type) {
            KALTURA -> kalturaPlaybackProvider
            YOUTUBE -> youtubePlaybackProvider
        }
    }
}
