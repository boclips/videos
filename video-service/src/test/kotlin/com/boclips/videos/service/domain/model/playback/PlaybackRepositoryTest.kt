package com.boclips.videos.service.domain.model.playback

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.videos.service.domain.model.playback.VideoProviderMetadata.YoutubeMetadata
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.testsupport.TestFactories.createCaptions
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration

class PlaybackRepositoryTest {
    lateinit var playbackRepository: PlaybackRepository
    lateinit var kalturaClient: TestKalturaClient

    @BeforeEach
    fun setUp() {
        kalturaClient = TestKalturaClient()
        kalturaClient.addMediaEntry(createMediaEntry(referenceId = "ref-id-1"))

        val kalturaPlaybackProvider = KalturaPlaybackProvider(kalturaClient)
        val youtubePlaybackProvider = TestYoutubePlaybackProvider()
        youtubePlaybackProvider.addVideo("yt-123", "thumbnailUrl", Duration.ZERO)
        youtubePlaybackProvider.addMetadata("yt-123", "aChannelName", "aChannelId")

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
    fun `removes a video for Kaltura, does nothing for youtube`() {
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")
        val youtubeVideo = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "yt-123")

        playbackRepository.remove(playbackId)
        playbackRepository.remove(youtubeVideo)

        assertThat(playbackRepository.find(listOf(playbackId))).isEmpty()
        assertThat(playbackRepository.find(listOf(youtubeVideo))).isNotNull
    }

    @Test
    fun `uploads captions to Kaltura`() {
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")

        playbackRepository.uploadCaptions(playbackId, createCaptions())

        assertThat(kalturaClient.getCaptionFilesByReferenceId("ref-id-1")).isNotEmpty
    }

    @Test
    fun `throws on attempts to upload captions to youtube`() {
        val youtubeVideo = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "yt-123")

        assertThrows<UnsupportedOperationException> {
            playbackRepository.uploadCaptions(youtubeVideo, createCaptions())
        }
    }

    @Test
    fun `can get metadata for youtube channel`() {
        val youtubeVideo = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "yt-123")

        val metadata = playbackRepository.getProviderMetadata(youtubeVideo)

        assertThat(metadata!!.id.type).isEqualTo(PlaybackProviderType.YOUTUBE)
        assertThat((metadata as YoutubeMetadata).channelId).isEqualTo("aChannelId")
        assertThat((metadata).channelName).isEqualTo("aChannelName")
    }
}
