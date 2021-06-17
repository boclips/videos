package com.boclips.videos.service.application.video

import com.boclips.kalturaclient.captionasset.CaptionFormat
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.videos.api.request.video.CaptionFormatRequest
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.KalturaFactories
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoCaptionServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoCaptionService: VideoCaptionService

    @Test
    fun `get captions in SRT format`() {
        val videoId = saveVideo(
            title = "Today Video?",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id")
        )

        val vttCaptions = KalturaFactories.createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English",
            captionFormat = CaptionFormat.WEBVTT
        )
        val vttCaptionContent = """WEBVTT

            00:00:00.500 --> 00:00:02.000
            The Web is always changing

            00:00:02.500 --> 00:00:04.300
            and the way we access it is changing
        """

        fakeKalturaClient.createCaptionForVideo("playback-id", vttCaptions, vttCaptionContent)

        val caption = videoCaptionService.getCaption(
            videoId = videoId.value,
            humanGeneratedOnly = false,
            captionFormatRequest = CaptionFormatRequest.SRT
        )!!

        val expectedSRTContent = """1
00:00:00,500 --> 00:00:02,000
The Web is always changing

2
00:00:02,500 --> 00:00:04,300
and the way we access it is changing"""

        assertThat(caption.format.toString()).isEqualTo(CaptionFormat.SRT.toString())
        assertThat(normalizeCaptionContent(caption.content)).isEqualTo(normalizeCaptionContent(expectedSRTContent))
    }

    fun normalizeCaptionContent(content: String) = content.replace("\n", "").replace(" ", "")
}
