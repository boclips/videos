package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.BoundedAgeRange
import com.boclips.videos.service.domain.model.UnboundedAgeRange
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.ContentType
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
            contentProvider = "enabled-cp",
            legalRestrictions = "None",
            ageRange = BoundedAgeRange(min = 5, max = 7)
        ).value

        youtubeVideoId = saveVideo(
            playbackId = PlaybackId(value = "yt-id-124", type = PlaybackProviderType.YOUTUBE),
            title = "elephants took out jobs",
            description = "it's a video from youtube",
            date = "2017-02-11",
            duration = Duration.ofMinutes(8),
            contentProvider = "enabled-cp2",
            ageRange = BoundedAgeRange(min = 7, max = 10)
        ).value

        disabledVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-id-125", type = PlaybackProviderType.KALTURA),
            title = "elephants eat a lot",
            description = "this video got disabled because it offended Jose Carlos Valero Sanchez",
            date = "2018-05-10",
            duration = Duration.ofMinutes(5),
            contentProvider = "disabled-cp",
            ageRange = UnboundedAgeRange,
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
            type = ContentType.NEWS
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
            type = ContentType.NEWS
        ).value

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", equalTo(videoId)))
            .andExpect(jsonPath("$._links.transcript.href").doesNotHaveJsonPath())
    }

    @Test
    fun `transcript link is not present when not authenticated`() {
        val videoId = saveVideoWithTranscript()

        mockMvc.perform(get("/v1/videos/$videoId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.transcript").doesNotExist())
    }

    @Test
    fun `transcript link is present when authenticated`() {
        val videoId = saveVideoWithTranscript()

        mockMvc.perform(get("/v1/videos/$videoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.transcript.href").exists())
    }

    private fun saveVideoWithTranscript(transcriptContent: String = "Some content in the video"): String {
        val videoId = saveVideo(
            title = "Today Video?",
            date = LocalDate.now().toString(),
            type = ContentType.NEWS
        ).value

        Assertions.assertNotNull(
            mongoVideosCollection().findOneAndUpdate(
                Filters.eq("title", "Today Video?"),
                Updates.set("transcript", transcriptContent)
            )
        )
        return videoId
    }
}