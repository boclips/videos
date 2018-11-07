package com.boclips.videos.service.domain.service

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.infrastructure.playback.KalturaPlaybackProvider
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test

class PlaybackServiceTest {
    lateinit var playbackService: PlaybackService

    @Before
    fun setUp() {
        val kalturaClient = TestKalturaClient()
        kalturaClient.addMediaEntry(createMediaEntry(referenceId = "ref-id-1"))

        val kalturaPlaybackProvider = KalturaPlaybackProvider(kalturaClient)

        playbackService = PlaybackService(kalturaPlaybackProvider)
    }

    @Test
    fun `returns playable video`() {
        val videoWithPlayback = playbackService.getVideoWithPlayback(TestFactories.createVideo(playbackId = PlaybackId(playbackProviderType = PlaybackProviderType.KALTURA, playbackId = "ref-id-1")))

        assertThat(videoWithPlayback.videoPlayback).isNotNull()
        assertThat(videoWithPlayback.isPlayable()).isTrue()
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