package com.boclips.videos.service.domain.model

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class PlaybackRepositoryTest {
    lateinit var playbackRepository: PlaybackRepository

    @BeforeEach
    fun setUp() {
        val kalturaClient = TestKalturaClient()
        kalturaClient.addMediaEntry(createMediaEntry(referenceId = "ref-id-1"))

        val kalturaPlaybackProvider = KalturaPlaybackProvider(kalturaClient)
        val youtubePlaybackProvider = TestYoutubePlaybackProvider()
        youtubePlaybackProvider.addVideo("yt-123", "thumbnail", Duration.ZERO)

        playbackRepository = PlaybackRepository(kalturaPlaybackProvider, youtubePlaybackProvider)
    }

    @Test
    fun `finds streams for multiple videos`() {
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")

        val videoWithPlayback = playbackRepository.find(listOf(playbackId))

        assertThat(videoWithPlayback[playbackId]).isNotNull
    }

    @Test
    fun `skips when streams are not found for video`() {
        assertThat(
            playbackRepository.find(
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

        assertThat(playbackRepository.find(listOf(kalturaVideo, youtubeVideo))).hasSize(2)
    }

    @Test
    fun `removes a a video for Kaltura, does nothing for youtube`() {
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")
        val youtubeVideo = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "yt-123")

        playbackRepository.remove(playbackId)
        playbackRepository.remove(youtubeVideo)

        assertThat(playbackRepository.find(listOf(playbackId))).isEmpty()
        assertThat(playbackRepository.find(listOf(youtubeVideo))).isNotNull
    }
}