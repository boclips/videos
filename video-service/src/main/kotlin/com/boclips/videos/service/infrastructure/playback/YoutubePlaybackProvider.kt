package com.boclips.videos.service.infrastructure.playback

import com.boclips.eventbus.domain.video.Captions
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.model.playback.VideoProviderMetadata.YoutubeMetadata
import com.boclips.videos.service.domain.model.video.Caption
import com.boclips.videos.service.domain.service.video.plackback.PlaybackProvider
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import com.google.api.services.youtube.model.Video
import java.io.OutputStream
import java.time.Duration
import java.util.Locale

class YoutubePlaybackProvider(youtubeApiKey: String) :
    PlaybackProvider {
    companion object {

        const val IDS_PER_QUERY_LIMIT = 50
    }

    private val youtube = YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), null)
        .setYouTubeRequestInitializer(YouTubeRequestInitializer(youtubeApiKey))
        .setApplicationName("boclips-video-service")
        .build()

    override fun retrievePlayback(playbackIds: List<PlaybackId>): Map<PlaybackId, YoutubePlayback> {
        if (playbackIds.isEmpty()) {
            return emptyMap()
        }

        return playbackIds.chunked(IDS_PER_QUERY_LIMIT).flatMap(this::fetchPlaybacks).toMap()
    }

    override fun uploadCaptions(playbackId: PlaybackId, captions: Captions) {
        throw UnsupportedOperationException("YouTube captions not supported")
    }

    override fun overwriteCaptionContent(playbackId: PlaybackId, content: String) {
        throw UnsupportedOperationException("YouTube captions not supported")
    }

    override fun getCaptions(playbackId: PlaybackId): List<Caption> {
        throw UnsupportedOperationException("YouTube captions not supported")
    }

    override fun requestCaptionsIfNotAvailable(playbackId: PlaybackId) {
        throw UnsupportedOperationException("YouTube captions not supported")
    }

    override fun deleteAutoGeneratedCaptions(playbackId: PlaybackId, language: Locale) {
        throw UnsupportedOperationException("YouTube captions not supported")
    }

    override fun downloadHighestResolutionVideo(
        playbackId: PlaybackId,
        outputStream: OutputStream
    ) {
        throw UnsupportedOperationException("YouTube does not support asset downloads")
    }

    override fun getExtensionForAsset(playbackId: PlaybackId): String {
        throw UnsupportedOperationException("YouTube does not support asset downloads")
    }

    override fun retrieveProviderMetadata(playbackIds: List<PlaybackId>): Map<PlaybackId, YoutubeMetadata> {
        if (playbackIds.isEmpty()) {
            return emptyMap()
        }

        return playbackIds.chunked(IDS_PER_QUERY_LIMIT).flatMap(this::fetchMetadatas).toMap()
    }

    private fun fetchPlaybacks(playbackIds: List<PlaybackId>): List<Pair<PlaybackId, YoutubePlayback>> {
        val videosListByIdRequest = getVideoListByIds(playbackIds)

        return videosListByIdRequest.execute().items.map(this::convertToPlayback)
    }

    private fun getVideoListByIds(playbackIds: List<PlaybackId>): YouTube.Videos.List {
        val videosListByIdRequest = youtube.videos().list("snippet,contentDetails")
        videosListByIdRequest.id = playbackIds.joinToString(separator = ",", transform = PlaybackId::value)
        return videosListByIdRequest
    }

    private fun fetchMetadatas(playbackIds: List<PlaybackId>): List<Pair<PlaybackId, YoutubeMetadata>> {
        return getVideoListByIds(playbackIds).execute().items.map(this::convertToProviderMetadata)
    }

    private fun convertToProviderMetadata(item: Video): Pair<PlaybackId, YoutubeMetadata> {
        val playbackId = PlaybackId(PlaybackProviderType.YOUTUBE, item.id)

        return playbackId to YoutubeMetadata(
            id = playbackId,
            channelId = item.snippet.channelId,
            channelName = item.snippet.channelTitle
        )
    }

    private fun convertToPlayback(item: Video): Pair<PlaybackId, YoutubePlayback> {
        val playbackId = PlaybackId(PlaybackProviderType.YOUTUBE, item.id)

        return (playbackId to YoutubePlayback(
            id = playbackId,
            duration = Duration.parse(item.contentDetails.duration),
            thumbnailUrl = item.snippet.thumbnails.high.url
        ))
    }

    override fun removePlayback(playbackId: PlaybackId) {
    }
}
