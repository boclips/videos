package com.boclips.videos.service.domain.service

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
    fun `getPlaybacks returns playback`() {
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")
        val videoWithPlayback = playbackRespository.getPlaybacks(listOf(playbackId))

        assertThat(videoWithPlayback[playbackId]).isNotNull
    }

    @Test
    fun `getPlaybacks skips an item when playback not found`() {
        assertThat(playbackRespository.getPlaybacks(listOf(PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-100")))).isEmpty()
    }

    @Test
    fun `getPlaybacks populates Playback information from Youtube and Kaltura`() {
        val kalturaVideo = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")
        val youtubeVideo = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "yt-123")

        assertThat(playbackRespository.getPlaybacks(listOf(kalturaVideo, youtubeVideo))).hasSize(2)
    }

    @Test
    fun `removes a video`() {
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1")
        playbackRespository.removePlayback(playbackId)

        assertThat(playbackRespository.getPlaybacks(listOf(playbackId))).isEmpty()
    }

}