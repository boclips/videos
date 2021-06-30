package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asTeacher
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration
import java.time.LocalDate

class VideoControllerTranscriptsIntegrationTest : AbstractSpringIntegrationTest() {

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

        youtubeVideoId = saveVideo(
            playbackId = PlaybackId(value = "yt-id-124", type = PlaybackProviderType.YOUTUBE),
            title = "elephants took out jobs",
            description = "it's a video from youtube",
            date = "2017-02-11",
            duration = Duration.ofMinutes(8),
            newChannelName = "enabled-cp2",
            ageRangeMin = 7,
            ageRangeMax = 10
        ).value

        disabledVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-id-125", type = PlaybackProviderType.KALTURA),
            title = "elephants eat a lot",
            description = "this video got disabled because it offended Jose Carlos Valero Sanchez",
            date = "2018-05-10",
            duration = Duration.ofMinutes(5),
            newChannelName = "disabled-cp",
            ageRangeMin = null,
            ageRangeMax = null,
            distributionMethods = emptySet()
        ).value
    }

    @Test
    fun `transcripts endpoint causes the file to download, without applying formatting`() {
        val videoId = saveVideoWithTranscript("Some content in the video.\n\nThis is another sentence that was said")

        mockMvc.perform(get("/v1/videos/$videoId/transcript").asTeacher())
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(
                content().string(
                    equalTo(
                        "Some content in the video.\n" +
                            "\n" +
                            "This is another sentence that was said"
                    )
                )
            )
            .andExpect(header().string("Content-Disposition", equalTo("attachment; filename=\"Today_Video_.txt\"")))
    }

    @Test
    fun `transcripts endpoint causes the file to download, applying formatting`() {
        val videoId = saveVideoWithTranscript("Some content in the video. This is another sentence that was said")

        mockMvc.perform(get("/v1/videos/$videoId/transcript").asTeacher())
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(
                content().string(
                    equalTo(
                        "Some content in the video.\n" +
                            "\n" +
                            "This is another sentence that was said"
                    )
                )
            )
            .andExpect(header().string("Content-Disposition", equalTo("attachment; filename=\"Today_Video_.txt\"")))
    }

    @Test
    fun `going to the transcripts endpoint for a video without transcripts returns 404`() {
        val videoId = saveVideo(
            title = "Today Video",
            date = LocalDate.now().toString(),
            types = listOf(VideoType.NEWS)
        ).value

        mockMvc.perform(get("/v1/videos/$videoId/transcript").asTeacher())
            .andExpect(status().isNotFound)
            .andExpectApiErrorPayload()
    }

    @Test
    fun `it returns a transcript URI when there is a transcript to download`() {
        val videoId = saveVideoWithTranscript()

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(videoId)))
            .andExpect(jsonPath("$._links.transcript.href", containsString("/videos/$videoId/transcript")))
    }

    @Test
    fun `it does not return a transcript uri when there is no transcript`() {
        val videoId = saveVideo(
            title = "Today Video",
            date = LocalDate.now().toString(),
            types = listOf(VideoType.NEWS)
        ).value

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(videoId)))
            .andExpect(jsonPath("$._links.transcript.href").doesNotHaveJsonPath())
    }

    @Test
    fun `transcript link is present when authenticated`() {
        val videoId = saveVideoWithTranscript()

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.transcript.href").exists())
    }

    @Test
    fun `transcript file name is without special characters`() {
        val videoId =
            saveVideoWithTranscript(title = "\rStudy links red meat consumption to heart disease and early death\r\n")

        mockMvc.perform(get("/v1/videos/$videoId/transcript").asTeacher())
            .andExpect(status().isOk)
            .andExpect(
                header().string(
                    "Content-Disposition",
                    equalTo("attachment; filename=\"Study_links_red_meat_consumption_to_heart_disease_and_early_death.txt\"")
                )
            )
    }

    private fun saveVideoWithTranscript(
        transcriptContent: String = "Some content in the video",
        title: String = "Today Video?"
    ): String {
        val videoId = saveVideo(
            title = title,
            date = LocalDate.now().toString(),
            types = listOf(VideoType.NEWS)
        ).value

        Assertions.assertNotNull(
            mongoVideosCollection().findOneAndUpdate(
                Filters.eq("title", title),
                Updates.set("transcript", transcriptContent)
            )
        )
        return videoId
    }
}
