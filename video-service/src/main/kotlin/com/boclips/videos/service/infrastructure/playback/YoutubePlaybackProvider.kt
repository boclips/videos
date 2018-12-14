package com.boclips.videos.service.infrastructure.playback

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import com.boclips.videos.service.domain.service.PlaybackProvider
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import java.time.Duration

class YoutubePlaybackProvider(youtubeApiKey: String) : PlaybackProvider {

    private val youtube = YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), null)
            .setYouTubeRequestInitializer(YouTubeRequestInitializer(youtubeApiKey))
            .build()

    override fun retrievePlayback(playbackIds: List<PlaybackId>): Map<PlaybackId, YoutubePlayback> {
        if (playbackIds.isEmpty()) {
            return emptyMap()
        }

        val videosListByIdRequest = youtube.videos().list("snippet,contentDetails")
        videosListByIdRequest.id = playbackIds.map { playbackId -> playbackId.value }.joinToString(separator = ",")

        val response = videosListByIdRequest.execute()
        return response.items.map { item ->
            val playbackId = PlaybackId(PlaybackProviderType.YOUTUBE, item.id)
            (playbackId to YoutubePlayback(
                    playbackId = playbackId,
                    thumbnailUrl = item.snippet.thumbnails.high.url,
                    duration = Duration.parse(item.contentDetails.duration)
            ))
        }.toMap()
    }

    override fun removePlayback(videoId: PlaybackId) {
    }

}