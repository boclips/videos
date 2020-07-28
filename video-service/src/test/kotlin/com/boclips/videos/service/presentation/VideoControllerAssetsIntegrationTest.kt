package com.boclips.videos.service.presentation

import com.boclips.kalturaclient.captionasset.CaptionAsset
import com.boclips.kalturaclient.captionasset.CaptionFormat
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideoControllerAssetsIntegrationTest : AbstractSpringIntegrationTest() {
    lateinit var kalturaVideoId: String
    lateinit var youtubeVideoId: String

    @Autowired
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        kalturaVideoId = saveVideo(
            title = "6 little horses & a beautiful cow.",
            playbackId = PlaybackId(value = "entry-id-123", type = PlaybackProviderType.KALTURA)
        ).value

        youtubeVideoId = saveVideo(
            title = "6 little horses & a beautiful cow.",
            playbackId = TestFactories.createYoutubePlayback().id
        ).value
    }

    @Test
    fun `can fetch a video asset without a caption`() {

        val content = """
            {
                "captions": false
            }
        """.trimIndent()

        val contentAsByteArray = mockMvc.perform(post("/v1/videos/$kalturaVideoId/assets").asBoclipsEmployee().content(content))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/zip"))
            .andExpect(
                header().string(
                    "Content-Disposition",
                    "attachment; filename=\"6-little-horses-a-beautiful-cow.zip\""
                )
            )
            .andReturn().response.contentAsByteArray

        assertThat(contentAsByteArray).isNotEmpty()
    }

    @Test
    fun `can fetch a video asset with a caption`() {
        fakeKalturaClient.createCaptionForVideo(
                "entry-id-123", CaptionAsset.builder().fileType(CaptionFormat.WEBVTT).build(), "what a caption!"
        )

        val content = """
            {
                "captions": true
            }
        """.trimIndent()

        val contentAsByteArray = mockMvc.perform(post("/v1/videos/$kalturaVideoId/assets").asBoclipsEmployee().content(content))
                .andExpect(status().isOk)
                .andExpect(content().contentType("application/zip"))
                .andExpect(
                        header().string(
                                "Content-Disposition",
                                "attachment; filename=\"6-little-horses-a-beautiful-cow.zip\""
                        )
                )
                .andReturn().response.contentAsByteArray

        assertThat(contentAsByteArray).isNotEmpty()
    }

    @Test
    fun `404 if no video asset`() {
        val content = """
            {
            "captions": false
            }
        """.trimIndent()
        mockMvc.perform(post("/v1/videos/non-existent-video/assets").asBoclipsEmployee().content(content))
            .andExpect(status().isNotFound)
    }


    @Test
    fun `404 for non boclips hosted videos`() {
        val content = """
            {
            "captions": false
            }
        """.trimIndent()
        mockMvc.perform(post("/v1/videos/$youtubeVideoId/assets").asBoclipsEmployee().content(content))
            .andExpect(status().isNotFound)
    }
}
