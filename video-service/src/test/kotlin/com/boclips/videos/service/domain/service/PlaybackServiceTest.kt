package com.boclips.videos.service.domain.service

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class PlaybackServiceTest {
    lateinit var playbackService: PlaybackService

    @BeforeEach
    fun setUp() {
        val kalturaClient = TestKalturaClient()
        kalturaClient.addMediaEntry(createMediaEntry(referenceId = "ref-id-1"))

        val kalturaPlaybackProvider = KalturaPlaybackProvider(kalturaClient)
        val youtubePlaybackProvider = TestYoutubePlaybackProvider()
        youtubePlaybackProvider.addVideo("yt-123", "thumbnail", Duration.ZERO)

        playbackService = PlaybackService(kalturaPlaybackProvider, youtubePlaybackProvider)
    }

    @Test
    fun `getVideoWithPlayback returns a video with playback`() {
        val videoWithPlayback = playbackService.getVideoWithPlayback(TestFactories.createVideo(playbackId = PlaybackId(playbackProviderType = PlaybackProviderType.KALTURA, playbackId = "ref-id-1")))

        assertThat(videoWithPlayback.videoPlayback).isNotNull()
        assertThat(videoWithPlayback.isPlayable()).isTrue()
    }

    @Test
    fun `getVideoWithPlayback throws an exception when playback not found`() {
        val video = TestFactories.createVideo(playbackId = PlaybackId(playbackProviderType = PlaybackProviderType.KALTURA, playbackId = "ref-id-100"))

        assertThatThrownBy {
            playbackService.getVideoWithPlayback(video)
        }.isInstanceOf(VideoPlaybackNotFound::class.java)
    }

    @Test
    fun `getVideosWithPlayback skips an item when playback not found`() {
        val video = TestFactories.createVideo(playbackId = PlaybackId(playbackProviderType = PlaybackProviderType.KALTURA, playbackId = "ref-id-100"))

        assertThat(playbackService.getVideosWithPlayback(listOf(video))).isEmpty()
    }

    @Test
    fun `getVideosWithPlayback populates Playback information from Youtube and Kaltura`() {
        val kalturaVideo = TestFactories.createVideo(playbackId = PlaybackId(playbackProviderType = PlaybackProviderType.KALTURA, playbackId = "ref-id-1"))
        val youtubeVideo = TestFactories.createVideo(playbackId = PlaybackId(playbackProviderType = PlaybackProviderType.YOUTUBE, playbackId = "yt-123"))

        assertThat(playbackService.getVideosWithPlayback(listOf(kalturaVideo, youtubeVideo))).hasSize(2)
    }

    @Test
    fun `removes a video`() {
        val video = TestFactories.createVideo(playbackId = PlaybackId(playbackProviderType = PlaybackProviderType.KALTURA, playbackId = "ref-id-1"))
        playbackService.removePlayback(video)

        assertThatThrownBy {
            playbackService.getVideoWithPlayback(video)
        }.isInstanceOf(VideoPlaybackNotFound::class.java)
    }

}