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
    fun `getVideosWithPlayback returns videos with playback`() {
        val videoWithPlayback = playbackService.getVideosWithPlayback(listOf(TestFactories.createVideo(playbackId = PlaybackId(playbackProviderType = PlaybackProviderType.KALTURA, playbackId = "ref-id-1"))))

        assertThat(videoWithPlayback.first().videoPlayback).isNotNull
        assertThat(videoWithPlayback.first().isPlayable()).isTrue()
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

        assertThat(playbackService.getVideosWithPlayback(listOf(video))).isEmpty()
    }

}