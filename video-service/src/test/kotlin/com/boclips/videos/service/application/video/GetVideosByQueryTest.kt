package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.KALTURA
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType.YOUTUBE
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class GetVideosByQueryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var getVideosByQuery: GetVideosByQuery

    @Test
    fun `matching Kaltura videos are returned`() {
        saveVideo(videoId = 1, title = "a kaltura video", playbackId = PlaybackId(playbackProviderType = KALTURA, playbackId = "ref-id-1"))

        val videos = getVideosByQuery.execute("kaltura")

        assertThat(videos).isNotEmpty
        assertThat(videos.first().content.title).isEqualTo("a kaltura video")
    }

    @Test
    fun `matching Youtube videos are returned`() {
        fakeYoutubePlaybackProvider.addVideo(youtubeId = "you-123", thumbnailUrl = "https://thumb.com", duration = Duration.ofSeconds(30))

        saveVideo(videoId = 1, title = "a youtube video", playbackId = PlaybackId(playbackProviderType = YOUTUBE, playbackId = "you-123"))

        val videos = getVideosByQuery.execute("youtube")

        assertThat(videos).isNotEmpty
        assertThat(videos.first().content.title).isEqualTo("a youtube video")
    }
}