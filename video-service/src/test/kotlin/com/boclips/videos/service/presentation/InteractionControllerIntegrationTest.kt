package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class InteractionControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `status is 200 when there are events`() {
        eventService.saveEvent(TestFactories.createSearchEvent())

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/interactions"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", startsWith("text/plain")))
            .andExpect(content().string(startsWith(">  20")))
    }
}
