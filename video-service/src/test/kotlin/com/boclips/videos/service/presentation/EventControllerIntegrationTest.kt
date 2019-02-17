package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.asBoclipsEmployee
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
        mockMvc.perform(
            post("/v1/events/playback")
                .asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Referer", "https://teachers.boclips.com/videos?q=abc")
                .content(
                    """{
                    "playerId": "123",
                    "assetId" : "$videoId",
                    "videoIndex" : 3,
                    "segmentStartSeconds" : 0,
                    "segmentEndSeconds" : 100,
                    "videoDurationSeconds" : 200,
                    "captureTime" : "2018-01-01T00:00:00.000Z",
                    "searchId" : "srch-123"
                    }""".trim()
                )
        )
            .andExpect(status().isCreated)

        val event = eventService.playbackEvent()
        assertThat(event.timestamp).isNotNull()
        assertThat(event.type).isNotNull()
        assertThat(event.user.boclipsEmployee).isTrue()
        assertThat(event.data.videoId).isEqualTo(videoId)
        assertThat(event.data.videoIndex).isEqualTo(3)
        assertThat(event.url).contains("https://teachers.boclips.com/videos?q=abc")
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
                    "assetId" : "$videoId",
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
