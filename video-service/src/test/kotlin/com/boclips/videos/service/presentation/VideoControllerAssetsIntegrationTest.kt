package com.boclips.videos.service.presentation

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.kalturaclient.captionasset.CaptionAsset
import com.boclips.kalturaclient.captionasset.CaptionFormat
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration

class VideoControllerAssetsIntegrationTest : AbstractSpringIntegrationTest() {
    lateinit var kalturaVideoId: String

    @BeforeEach
    fun setUp() {
        kalturaVideoId = saveVideo(
            title = "6 little horses & a beautiful cow.",
            playbackId = PlaybackId(value = "entry-id-123", type = PlaybackProviderType.KALTURA)
        ).value
    }

    @Test
    fun `can fetch a video asset`() {
        fakeKalturaClient.createCaptionForVideo(
            "entry-id-123", CaptionAsset.builder().fileType(CaptionFormat.WEBVTT).build(), "what a caption!"
        )

        mockMvc.perform(get("/v1/videos/$kalturaVideoId/assets").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string("what a caption!"))
            .andExpect(header().string("Content-Disposition","attachment; filename=6-little-horses-a-beautiful-cow.vtt"))
    }

    @Test
    fun `404 if no video asset`() {
        mockMvc.perform(get("/v1/videos/$kalturaVideoId/assets").asBoclipsEmployee())
            .andExpect(status().isNotFound)
    }
}
