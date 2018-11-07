package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class KalturaPlaybackProviderTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var kalturaPlaybackProvider: KalturaPlaybackProvider

    @Autowired
    lateinit var kalturaClient: TestKalturaClient

    @Test
    fun `returns playable videos`() {
        val videosWithPlayback = kalturaPlaybackProvider.getVideosWithPlayback(listOf(createVideo()))

        assertThat(videosWithPlayback).hasSize(1)
        assertThat(videosWithPlayback[0].videoPlayback).isNotNull()
        assertThat(videosWithPlayback[0].isPlayable()).isTrue()

        val videoPlayback = videosWithPlayback[0].videoPlayback!! as StreamPlayback
        assertThat(videoPlayback.streamUrl).isEqualTo("https://stream/mpegdash/video-1.mp4")
        assertThat(videoPlayback.thumbnailUrl).isEqualTo("https://thumbnail/thumbnail-1.mp4")
        assertThat(videoPlayback.duration).isEqualTo(Duration.parse("PT1M"))
    }

    @Test
    fun `returns only playable videos`() {
        val videosWithPlayback = kalturaPlaybackProvider.getVideosWithPlayback(
                listOf(
                        createVideo(playbackId = PlaybackId(playbackProviderType = PlaybackProviderType.KALTURA, playbackId = "ref-id-1")),
                        createVideo(playbackId = PlaybackId(playbackProviderType = PlaybackProviderType.KALTURA, playbackId = "ref-id-100"))
                )
        )

        assertThat(videosWithPlayback).hasSize(1)
        assertThat(videosWithPlayback[0].videoPlayback).isNotNull()
        assertThat(videosWithPlayback[0].isPlayable()).isTrue()
    }

    @Test
    fun `removes the playback information`() {
        kalturaClient.addMediaEntry(MediaEntry.builder()
                .id("something")
                .referenceId("ref-123")
                .build())

        val video = createVideo(playbackId = PlaybackId(playbackProviderType = PlaybackProviderType.KALTURA, playbackId = "ref-id-123"))
        kalturaPlaybackProvider.removePlayback(video)

        assertThat(kalturaPlaybackProvider.getVideosWithPlayback(listOf(video))).isEmpty()
    }

}
