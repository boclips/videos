package com.boclips.videos.service.presentation

import com.boclips.videos.service.infrastructure.event.types.PlaybackEvent
import com.boclips.videos.service.infrastructure.event.EventLogRepository
import com.boclips.videos.service.infrastructure.event.types.SearchEvent
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asReporter
import com.boclips.videos.service.testsupport.asTeacher
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.ZonedDateTime

class EventControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var eventLogRepository: EventLogRepository

    @Test
    fun `posted playback events are being saved`() {
        mockMvc.perform(post("/v1/events")
                .asTeacher()
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{
                    "playerId": "123",
                    "videoId" : "v555",
                    "segmentStartSeconds" : 0,
                    "segmentEndSeconds" : 100,
                    "videoDurationSeconds" : 200,
                    "captureTime" : "2018-01-01T00:00:00.000Z",
                    "searchId" : "srch-123"
                    }""".trimMargin())
        )
                .andExpect(status().isCreated)

        assertThat(eventLogRepository.count()).isEqualTo(1)
        assertThat(eventLogRepository.findAll().first().timestamp).isNotNull()
        assertThat(eventLogRepository.findAll().first().type).isNotNull()
        assertThat(eventLogRepository.findAll().first().data).isNotNull()
    }

    @Test
    fun `post and retrieve no search results`() {
        mockMvc.perform(post("/v1/events/no-search-results")
                .asTeacher()
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{
                        "name": "Hans Muster",
                        "query" : "animal",
                        "email" : "hans@muster.com",
                        "description" : "description"
                        }""".trimMargin()))
                .andExpect(status().isCreated)

        mockMvc.perform(get("/v1/events/no-search-results")
                .asReporter())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded[*]", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.events[0].createdAt").exists())
                .andExpect(jsonPath("$._embedded.events[0].name").exists())
                .andExpect(jsonPath("$._embedded.events[0].email").exists())
                .andExpect(jsonPath("$._embedded.events[0].query").exists())
                .andExpect(jsonPath("$._embedded.events[0].description").exists())
                .andExpect(jsonPath("$._embedded.events[0].type").doesNotExist())
    }

    @Test
    fun `status is 200 when there are events`() {
        eventService.saveEvent(SearchEvent(ZonedDateTime.now(), "search-id", "query", 10))
        eventService.saveEvent(PlaybackEvent(
                playerId = "player-id",
                captureTime = ZonedDateTime.now(),
                searchId = "search-id",
                segmentStartSeconds = 10,
                segmentEndSeconds = 20,
                videoDurationSeconds = 50,
                videoId = "video-id"
        ))
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/events/status"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.healthy", `is`(true)))
                .andExpect(jsonPath("$.latestSearch", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.latestPlaybackInSearch", not(isEmptyOrNullString())))
    }

    @Test
    fun `status is 500 when there are no events`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/events/status"))
                .andExpect(status().isServiceUnavailable)
                .andExpect(jsonPath("$.healthy", `is`(false)))
                .andExpect(jsonPath("$.latestSearch", isEmptyOrNullString()))
                .andExpect(jsonPath("$.latestPlaybackInSearch", isEmptyOrNullString()))
                .andExpect(jsonPath("$.latestPlaybackStandalone", isEmptyOrNullString()))
    }

}
