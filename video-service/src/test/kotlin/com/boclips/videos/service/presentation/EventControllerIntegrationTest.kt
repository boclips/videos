package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.asTeacher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class EventControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `posted playback events are being saved`() {
        val videoId = TestFactories.aValidId()

        val content = """{
            "videoId":"$videoId",
            "videoIndex":135,
            "captureTime":"2019-02-21T15:34:37.186Z",
            "playerId":"f249f486-fc04-48f7-7361-4413c13a4183",
            "segmentStartSeconds":1469.128248,
            "segmentEndSeconds":1470.728248,
            "videoDurationSeconds":610
        }""".trimIndent()

        mockMvc.perform(
            post("/v1/events/playback")
                .asTeacher(email = "teacher@gmail.com")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Referer", "https://teachers.boclips.com/videos?q=abc")
                .content(content)
        )
            .andExpect(status().isCreated)

        val message = messageCollector.forChannel(topics.videoSegmentPlayed()).poll()
        assertThat(message).isNotNull
        assertThat(message.payload.toString()).contains(videoId)
        assertThat(message.payload.toString()).contains("teacher@gmail.com")
        assertThat(message.payload.toString()).contains("135")
        assertThat(message.payload.toString()).contains("f249f486-fc04-48f7-7361-4413c13a4183")
        assertThat(message.payload.toString()).contains("1469")
        assertThat(message.payload.toString()).contains("1470")
        assertThat(message.payload.toString()).contains("610")
        assertThat(message.payload.toString()).contains("https://teachers.boclips.com/videos?q=abc")
    }

    @Test
    fun `playbacks by unathorized users are saved`() {
        val videoId = TestFactories.aValidId()
        mockMvc.perform(
            post("/v1/events/playback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{
                    "playerId": "123",
                    "videoId" : "$videoId",
                    "videoIndex" : 3,
                    "segmentStartSeconds" : 0,
                    "segmentEndSeconds" : 100,
                    "videoDurationSeconds" : 200,
                    "captureTime" : "2018-01-01T00:00:00.000Z",
                    "searchId" : "srch-123"
                    }""".trimMargin()
                )
        )
            .andExpect(status().isCreated)
    }

    @Test
    fun `post no search results`() {
        mockMvc.perform(
            post("/v1/events/no-search-results")
                .asTeacher()
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{
                        "name": "Hans Muster",
                        "query" : "animal",
                        "email" : "hans@muster.com",
                        "description" : "description"
                        }""".trimMargin()
                )
        )
            .andExpect(status().isCreated)
    }
}
