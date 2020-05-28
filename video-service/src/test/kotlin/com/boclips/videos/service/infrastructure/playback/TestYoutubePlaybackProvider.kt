package com.boclips.videos.service.infrastructure.playback

import com.boclips.eventbus.domain.video.Captions
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.model.playback.VideoProviderMetadata
import com.boclips.videos.service.domain.model.playback.VideoProviderMetadata.YoutubeMetadata
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.service.video.plackback.PlaybackProvider
import java.io.OutputStream
import java.time.Duration
import java.util.Locale

class TestYoutubePlaybackProvider :
    PlaybackProvider {
    private val playbackById = mutableMapOf<PlaybackId, YoutubePlayback>()
    private val providerMetadataById = mutableMapOf<PlaybackId, YoutubeMetadata>()

    fun clear() {
        playbackById.clear()
        providerMetadataById.clear()
    }

    override fun retrievePlayback(playbackIds: List<PlaybackId>): Map<PlaybackId, YoutubePlayback> {
        return playbackIds
            .mapNotNull map@{ id ->
                val youtubePlayback = playbackById[id] ?: return@map null
                (id to youtubePlayback)
            }
            .toMap()
    }

    override fun getCaptions(playbackId: PlaybackId): List<Caption> {
        throw UnsupportedOperationException("YouTube captions not supported")
    }

    override fun requestCaptionsIfNotAvailable(playbackId: PlaybackId) {
        throw UnsupportedOperationException("YouTube captions not supported")
    }

    override fun removePlayback(playbackId: PlaybackId) {
    }

    override fun uploadCaptions(playbackId: PlaybackId, captions: Captions) {
        throw UnsupportedOperationException("YouTube captions not supported")
    }

    override fun overwriteCaptionContent(playbackId: PlaybackId, content: String) {
        throw UnsupportedOperationException("YouTube captions not supported")
    }

    override fun deleteAutoGeneratedCaptions(playbackId: PlaybackId, language: Locale) {
        throw UnsupportedOperationException("YouTube captions not supported")
    }

    override fun downloadFHDOrOriginalAsset(
        playbackId: PlaybackId,
        outputStream: OutputStream
    ) {
        throw UnsupportedOperationException("YouTube does not support asset downloads")
    }

    override fun getExtensionForAsset(playbackId: PlaybackId): String {
        throw UnsupportedOperationException("YouTube does not support asset downloads")
    }

    fun addVideo(youtubeId: String, thumbnailUrl: String, duration: Duration): TestYoutubePlaybackProvider {
        val playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = youtubeId)

        playbackById[playbackId] = YoutubePlayback(
            id = playbackId,
            duration = duration,
            thumbnailUrl = thumbnailUrl
        )

        return this
    }

    override fun retrieveProviderMetadata(playbackIds: List<PlaybackId>): Map<PlaybackId, VideoProviderMetadata> {
        return playbackIds.mapNotNull map@{ id ->
            val youtubeMetadata = providerMetadataById[id] ?: return@map null
            (id to youtubeMetadata)
        }.toMap()
    }

    fun addMetadata(youtubeId: String, channelName: String, channelId: String): TestYoutubePlaybackProvider {
        val playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = youtubeId)

        providerMetadataById[playbackId] = YoutubeMetadata(
            channelName = channelName,
            channelId = channelId,
            id = playbackId
        )

        return this
    }
}
