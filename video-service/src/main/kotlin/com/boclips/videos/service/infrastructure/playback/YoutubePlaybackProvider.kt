package com.boclips.videos.service.infrastructure.playback

import com.boclips.events.types.Captions
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.service.video.PlaybackProvider
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import com.google.api.services.youtube.model.Video
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

    override fun deleteAutoGeneratedCaptions(playbackId: PlaybackId, language: Locale) {
        throw UnsupportedOperationException("YouTube captions not supported")
    }

    private fun fetchPlaybacks(playbackIds: List<PlaybackId>): List<Pair<PlaybackId, YoutubePlayback>> {
        val videosListByIdRequest = youtube.videos().list("snippet,contentDetails")
        videosListByIdRequest.id = playbackIds.joinToString(separator = ",", transform = PlaybackId::value)

        return videosListByIdRequest.execute().items.map(this::convertToPlayback)
    }

    private fun convertToPlayback(item: Video): Pair<PlaybackId, YoutubePlayback> {
        val playbackId = PlaybackId(PlaybackProviderType.YOUTUBE, item.id)

        return (playbackId to YoutubePlayback(
            id = playbackId,
            thumbnailUrl = item.snippet.thumbnails.high.url,
            duration = Duration.parse(item.contentDetails.duration)
        ))
    }

    override fun removePlayback(playbackId: PlaybackId) {
    }
}
