package com.boclips.videos.service.domain.model.playback

import com.boclips.eventbus.domain.video.Captions
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.domain.service.video.PlaybackProvider
import java.util.*

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

    fun deleteAutoGeneratedCaptions(playbackId: PlaybackId, language: Locale) {
        return getProvider(playbackId).deleteAutoGeneratedCaptions(playbackId, language)
    }

    fun getProviderMetadata(playbackId: PlaybackId): VideoProviderMetadata? {
        return getProviderMetadata(listOf(playbackId)).values.firstOrNull()
    }

    private fun getProviderMetadata(playbackIds: List<PlaybackId>): Map<PlaybackId, VideoProviderMetadata> {
        val kalturaProviderMetadataById =
            kalturaPlaybackProvider.retrieveProviderMetadata(playbackIds.filter { playbackId -> playbackId.type == KALTURA })
        val youtubeProviderMetadataById =
            youtubePlaybackProvider.retrieveProviderMetadata(playbackIds.filter { playbackId -> playbackId.type == YOUTUBE })

        return playbackIds.mapNotNull { playbackId ->
            val providerMetadata = when (playbackId.type) {
                KALTURA -> kalturaProviderMetadataById[playbackId]
                YOUTUBE -> youtubeProviderMetadataById[playbackId]
            } ?: return@mapNotNull null
            (playbackId to providerMetadata)
        }.toMap()
    }

    private fun getProvider(playbackId: PlaybackId): PlaybackProvider {
        return when (playbackId.type) {
            KALTURA -> kalturaPlaybackProvider
            YOUTUBE -> youtubePlaybackProvider
        }
    }
}
