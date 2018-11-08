package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
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
        fakeKalturaClient.addMediaEntry(createMediaEntry("1"))

        val playbackById = kalturaPlaybackProvider.retrievePlayback(listOf("ref-id-1"))

        assertThat(playbackById).hasSize(1)
        assertThat(playbackById["ref-id-1"]).isNotNull

        val videoPlayback = playbackById["ref-id-1"] as StreamPlayback
        assertThat(videoPlayback.streamUrl).isEqualTo("https://stream/mpegdash/video-1.mp4")
        assertThat(videoPlayback.thumbnailUrl).isEqualTo("https://thumbnail/thumbnail-1.mp4")
        assertThat(videoPlayback.duration).isEqualTo(Duration.parse("PT1M"))
    }

    @Test
    fun `returns only playable videos`() {
        fakeKalturaClient.addMediaEntry(createMediaEntry("1"))

        val videosWithPlayback = kalturaPlaybackProvider.retrievePlayback(
                listOf("ref-id-1", "ref-id-100")
        )

        assertThat(videosWithPlayback).hasSize(1)
        assertThat(videosWithPlayback["ref-id-1"]).isNotNull
        assertThat(videosWithPlayback["ref-id-100"]).isNull()
    }

    @Test
    fun `removes the playback information`() {
        kalturaClient.addMediaEntry(MediaEntry.builder()
                .id("something")
                .referenceId("ref-123")
                .build())

        kalturaPlaybackProvider.removePlayback("ref-id-123")

        assertThat(kalturaPlaybackProvider.retrievePlayback(listOf("ref-id-123"))).isEmpty()
    }

}
