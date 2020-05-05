package com.boclips.videos.service.presentation

import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.KalturaFactories
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.Duration


class VideoControllerCaptionsIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    lateinit var disabledVideoId: String
    lateinit var kalturaVideoId: String
    lateinit var youtubeVideoId: String

    @BeforeEach
    fun setUp() {
        kalturaVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-id-123", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofMinutes(1),
            contentProvider = "enabled-cp",
            legalRestrictions = "None",
            ageRangeMin = 5,
            ageRangeMax = 7
        ).value
    }

    @Test
    fun `Put request to a update a video's captions endpoint updates its transcript`() {
        val video = saveVideo(title = "Today Video?", playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))
        val existingCaptions = KalturaFactories.createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)"
        )
        fakeKalturaClient.createCaptionsFileWithEntryId("playback-id", existingCaptions, "previous captions content").id


        val content = """
            {
               "captions": "WEBVTT FILE\n\n1\n00:01.981 --> 00:04.682\nWe're quite content to be the odd<br>browser out.\n\n2\n00:05.302 --> 00:08.958\nWe don't have a fancy stock abbreviation <br>to go alongside our name in the press.\n\n3\n00:09.526 --> 00:11.324\nWe don't have a profit margin."
            }
        """.trimIndent()

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/videos/${video.value}/captions").asBoclipsEmployee()
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
            .andExpect(MockMvcResultMatchers.status().isOk)

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/videos/${video.value}/transcript").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(
                MockMvcResultMatchers.content().string(
                    Matchers.equalTo("""
                        We're quite content to be the odd<br>browser out.
                        We don't have a fancy stock abbreviation <br>to go alongside our name in the press.
                        We don't have a profit margin.""".trimIndent()
                    )
                )
            )
            .andExpect(MockMvcResultMatchers.header().string("Content-Disposition", Matchers.equalTo("attachment; filename=\"Today_Video_.txt\"")))
    }

    @Test
    fun `invalid caption update returns bad request`() {
        val video = saveVideo(title = "Today Video?", playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))


        val captionContent = """"DEFORMED WEBVTT FILE\n
                                1\n
                                not a valid cue\n
                                We're quite content to be the odd<br>browser out.\n
                                \n
                                2\n
                                00:05.302 --> 00:08.958\n
                                We don't have a fancy stock abbreviation <br>to go alongside our name in the press.\n
                                \n
                                3\n
                                00:09.526 --> 00:11.324\n
                                We don't have a profit margin."""".trimIndent()


        mockMvc.perform(MockMvcRequestBuilders.put("/v1/videos/${video.value}/captions").asBoclipsEmployee()
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
            {
               "captions": "$captionContent"
            }
        """.trimIndent()))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `gets a video's caption content successfully`() {
        val video = saveVideo(title = "Today Video?", playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id"))
        val existingCaptions = KalturaFactories.createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)"
        )
        val captionContent = "WEBVTT FILE\n\n1\n00:01.981 --> 00:04.682\nWe're quite content to be the odd<br>browser out.\n\n2\n00:05.302 --> 00:08.958\nWe don't have a fancy stock abbreviation <br>to go alongside our name in the press.\n\n3\n00:09.526 --> 00:11.324\nWe don't have a profit margin."
        fakeKalturaClient.createCaptionsFileWithEntryId("playback-id", existingCaptions, captionContent).id


        mockMvc.perform(MockMvcRequestBuilders.get("/v1/videos/${video.value}/captions").asBoclipsEmployee())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.equalTo(captionContent)))
    }
}