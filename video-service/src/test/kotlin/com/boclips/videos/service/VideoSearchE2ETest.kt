package com.boclips.videos.service

import com.boclips.videos.service.infrastructure.event.EventLogRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.withTeacher
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasSize
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideoSearchE2ETest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var eventLogRepository: EventLogRepository

    @Test
    fun `search for videos`() {
        mockMvc.perform(get("/v1/videos?query=elephants").withTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(2)))
                .andExpect(jsonPath("$._embedded.videos[0].title", containsString("elephants")))

        assertThat(eventLogRepository.count()).isEqualTo(1)
    }
}
