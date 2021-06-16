package com.boclips.videos.service.presentation

import com.boclips.kalturaclient.KalturaCaptionManager
import com.boclips.kalturaclient.captionasset.CaptionAsset
import com.boclips.kalturaclient.captionasset.CaptionFormat
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.KalturaFactories
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asTeacher
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
            newChannelName = "enabled-cp",
            legalRestrictions = "None",
            ageRangeMin = 5,
            ageRangeMax = 7
        ).value
    }

    @Test
    fun `Put request to a update a video's captions endpoint updates its transcript`() {
        val video = saveVideo(
            title = "Today Video?",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id")
        )
        val existingCaptions = KalturaFactories.createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)"
        )
        fakeKalturaClient.createCaptionForVideo("playback-id", existingCaptions, "previous captions content").id

        val content = """
            {
               "captions": "WEBVTT\r\n\r\nNOTE Confidence: 0.8984\r\n\r\n00:00:00.000 --> 00:00:04.060\r\nWhile regaling you with daring stories from her youth,\r\n\r\nNOTE Confidence: 0.8984\r\n\r\n00:00:04.060 --> 00:00:07.080\r\nit might be hard to believe your\r\n\r\nNOTE Confidence: 0.8984\r\n\r\n00:00:07.080 --> 00:00:10.110\r\ngrandmother used to be a trapeze artist.\r\n\r\n"
            }
        """.trimIndent()

        mockMvc.perform(
            MockMvcRequestBuilders.put("/v1/videos/${video.value}/captions").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isOk)

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/videos/${video.value}/transcript").asTeacher())
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(
                content().string(
                    Matchers.equalTo(
                        """
                        While regaling you with daring stories from her youth,
                        it might be hard to believe your
                        grandmother used to be a trapeze artist.
                        """.trimIndent()
                    )
                )
            )
            .andExpect(
                header()
                    .string("Content-Disposition", Matchers.equalTo("attachment; filename=\"Today_Video_.txt\""))
            )
    }

    @Test
    fun `invalid caption update returns bad request`() {
        val video = saveVideo(
            title = "Today Video?",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id")
        )

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
                                We don't have a profit margin."
        """.trimIndent()

        mockMvc.perform(
            MockMvcRequestBuilders.put("/v1/videos/${video.value}/captions").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
            {
               "captions": "$captionContent"
            }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `gets a video's caption content successfully`() {
        val video = saveVideo(
            title = "Today Video?",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id")
        )
        val existingCaptions = KalturaFactories.createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)"
        )
        val captionContent =
            "WEBVTT FILE\n\n1\n00:01.981 --> 00:04.682\nWe're quite content to be the odd<br>browser out.\n\n2\n00:05.302 --> 00:08.958\nWe don't have a fancy stock abbreviation <br>to go alongside our name in the press.\n\n3\n00:09.526 --> 00:11.324\nWe don't have a profit margin."
        fakeKalturaClient.createCaptionForVideo("playback-id", existingCaptions, captionContent).id

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/videos/${video.value}/captions").asBoclipsEmployee())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content", Matchers.equalTo(captionContent)))
    }

    @Test
    fun `requests captions for video successfully`() {
        val video = saveVideo(
            title = "Today Video?",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id")
        )

        mockMvc.perform(
            MockMvcRequestBuilders.put("/v1/videos/${video.value}/captions?generated=true").asBoclipsEmployee()
        )
            .andExpect(status().isAccepted)
            .andExpect(content().string(""))

        assertThat(fakeKalturaClient.getCaptionStatus("playback-id")).isEqualTo(KalturaCaptionManager.CaptionStatus.REQUESTED)
    }

    @Test
    fun `returns a conflict when captions already available`() {
        val video = saveVideo(
            title = "Today Video?",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id")
        )
        fakeKalturaClient.createCaptionForVideo(
            "playback-id",
            CaptionAsset.builder().label("wow what a caption").build(),
            "hhi"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.put("/v1/videos/${video.value}/captions?generated=true").asBoclipsEmployee()
        )
            .andExpect(status().isConflict)
    }

    @Test
    fun `returns captions and content disposition header when download query param set`() {
        val video = saveVideo(
            title = "Today Video?",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id")
        )
        val existingCaptions = KalturaFactories.createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)",
            captionFormat = CaptionFormat.SRT
        )
        val captionContent =
            "WEBVTT FILE\n\n1\n00:01.981 --> 00:04.682\nWe're quite content to be the odd<br>browser out.\n\n2\n00:05.302 --> 00:08.958\nWe don't have a fancy stock abbreviation <br>to go alongside our name in the press.\n\n3\n00:09.526 --> 00:11.324\nWe don't have a profit margin."
        fakeKalturaClient.createCaptionForVideo("playback-id", existingCaptions, captionContent).id

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/videos/${video.value}/captions?download=true"))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(
                header().string(
                    "Content-Disposition",
                    Matchers.equalTo("attachment; filename=\"Today_Video_.srt\"")
                )
            )
            .andExpect(content().string(Matchers.equalTo(captionContent)))
    }

    @Test
    fun `returns only human generated captions when specified`() {
        val video = saveVideo(
            title = "Today Video?",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "playback-id")
        )
        val autoCaptions = KalturaFactories.createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English (auto-generated)",
            captionFormat = CaptionFormat.SRT
        )
        val humanCaptions = KalturaFactories.createKalturaCaptionAsset(
            language = KalturaLanguage.ENGLISH,
            label = "English",
            captionFormat = CaptionFormat.SRT
        )
        val autoCaptionContent =
            "WEBVTT FILE\n\n1\n00:01.981 --> 00:04.682\nWe're quite content to be the odd<br>browser out.\n\n2\n00:05.302 --> 00:08.958\nWe don't have a fancy stock abbreviation <br>to go alongside our name in the press.\n\n3\n00:09.526 --> 00:11.324\nWe don't have a profit margin."

        val humanCaptionContent = "1\n00:00:00,000 --> 00:00:02,390\n[MUSIC PLAYING]"

        fakeKalturaClient.createCaptionForVideo("playback-id", autoCaptions, autoCaptionContent)
        fakeKalturaClient.createCaptionForVideo("playback-id", humanCaptions, humanCaptionContent)

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/videos/${video.value}/captions?human-generated=true").asBoclipsEmployee())
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content", Matchers.equalTo(humanCaptionContent)))
    }
}
