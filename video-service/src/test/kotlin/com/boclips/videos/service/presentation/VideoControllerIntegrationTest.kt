package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.hamcrest.Matchers
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideoControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `returns 400 for invalid search request`() {
        mockMvc.perform(get("/v1/videos"))
                .andExpect(status().`is`(400))
    }

    @Test
    fun `returns empty videos array when there are no results`() {
        mockMvc.perform(get("/v1/videos?query=somethingthatdoesntexistever"))
                .andExpect(status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.videos", Matchers.emptyIterable<Any>()))
    }
}