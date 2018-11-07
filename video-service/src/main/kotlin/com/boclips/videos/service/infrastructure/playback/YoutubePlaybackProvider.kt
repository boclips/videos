package com.boclips.videos.service.infrastructure.playback

import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import com.boclips.videos.service.domain.service.PlaybackProvider
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import java.time.Duration

class YoutubePlaybackProvider(
        private val youtubeApiKey: String
) : PlaybackProvider {
    override fun retrievePlayback(videoIds: List<String>): Map<String, YoutubePlayback> {
        val youtube = YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), null)
                .setYouTubeRequestInitializer(YouTubeRequestInitializer(youtubeApiKey))
                .build()
        val videosListByIdRequest = youtube.videos().list("snippet,contentDetails")
        videosListByIdRequest.id = videoIds.joinToString(separator = ",")
        val response = videosListByIdRequest.execute()
        return response.items.map { item ->
            (item.id to YoutubePlayback(
                    youtubeId = item.id,
                    thumbnailUrl = item.snippet.thumbnails.high.url,
                    duration = Duration.parse(item.contentDetails.duration)
            ))
        }.toMap()
    }

    override fun removePlayback(videoId: String) {
    }

}