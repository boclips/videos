package com.boclips.videos.service.presentation

import com.boclips.videos.service.infrastructure.event.EventLogRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.withTeacher
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class EventControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var eventLogRepository: EventLogRepository

    @Test
    fun `posted events are being saved`() {
        mockMvc.perform(post("/v1/events")
                .withTeacher()
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{
                    "playerIdentifier": "123",
                    "videoIdentifier" : "v555",
                    "segmentStartSeconds" : 0,
                    "segmentEndSeconds" : 100,
                    "videoDurationSeconds" : 200,
                    "captureTime" : "2018-01-01T00:00:00.000Z",
                    "searchId" : "srch-123"
                    }""".trimMargin())
        )
                .andExpect(status().isCreated)

        assertThat(eventLogRepository.count()).isEqualTo(1)
    }
}
