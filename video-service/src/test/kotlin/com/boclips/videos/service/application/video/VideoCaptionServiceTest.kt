package com.boclips.videos.service.application.video

import com.boclips.kalturaclient.KalturaCaptionManager
import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.api.response.video.CaptionStatus
import com.boclips.videos.service.testsupport.PlaybackResourceFactory
import com.boclips.videos.service.testsupport.VideoResourceFactory
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoCaptionServiceTest {
    val kalturaClient = mock<KalturaClient>()
    val target = VideoCaptionService(kalturaClient)

    @Test
    fun `maps status - PROCESSING`() { //No need to check all cases as it is typed
        whenever(kalturaClient.getCaptionStatus("playback-id"))
            .thenReturn(KalturaCaptionManager.CaptionStatus.PROCESSING)

        val output = target.withCaptionDetails(
            VideoResourceFactory.sample(playback = PlaybackResourceFactory.sample(id = "playback-id"))
        )

        assertThat(output.captionStatus).isEqualTo(CaptionStatus.PROCESSING)
    }

    @Test
    fun `maps status - null`() {
        whenever(kalturaClient.getCaptionStatus("playback-id"))
            .thenReturn(null)

        val output = target.withCaptionDetails(
            VideoResourceFactory.sample(playback = PlaybackResourceFactory.sample(id = "playback-id"))
        )

        assertThat(output.captionStatus).isEqualTo(CaptionStatus.UNKNOWN)
    }
}
