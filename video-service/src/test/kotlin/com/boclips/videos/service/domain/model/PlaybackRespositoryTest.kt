package com.boclips.videos.service.domain.model

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackRespository
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class PlaybackRespositoryTest {
    lateinit var playbackRespository: PlaybackRespository

    @BeforeEach
    fun setUp() {
        val kalturaClient = TestKalturaClient()
        kalturaClient.addMediaEntry(createMediaEntry(referenceId = "ref-id-1"))

        val kalturaPlaybackProvider = KalturaPlaybackProvider(kalturaClient)
        val youtubePlaybackProvider = TestYoutubePlaybackProvider()
        youtubePlaybackProvider.addVideo("yt-123", "thumbnail", Duration.ZERO)

        playbackRespository = PlaybackRespository(kalturaPlaybackProvider, youtubePlaybackProvider)
    }

    @Test
    fun `finds streams for multiple videos`() {
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")

        val videoWithPlayback = playbackRespository.find(listOf(playbackId))

        assertThat(videoWithPlayback[playbackId]).isNotNull
    }

    @Test
    fun `skips when streams are not found for video`() {
        assertThat(
            playbackRespository.find(
                listOf(
                    PlaybackId(
                        type = PlaybackProviderType.KALTURA,
                        value = "ref-id-100"
                    )
                )
            )
        ).isEmpty()
    }

    @Test
    fun `finds streams for Kaltura and Youtube`() {
        val kalturaVideo = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")
        val youtubeVideo = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "yt-123")

        assertThat(playbackRespository.find(listOf(kalturaVideo, youtubeVideo))).hasSize(2)
    }

    @Test
    fun `removes a a video for Kaltura, does nothing for youtube`() {
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")
        val youtubeVideo = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "yt-123")

        playbackRespository.remove(playbackId)
        playbackRespository.remove(youtubeVideo)

        assertThat(playbackRespository.find(listOf(playbackId))).isEmpty()
        assertThat(playbackRespository.find(listOf(youtubeVideo))).isNotNull
    }
}