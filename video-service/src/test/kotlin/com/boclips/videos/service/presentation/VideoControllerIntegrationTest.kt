package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractIntegrationTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideoControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `returns 400 for invalid search request`() {
        mockMvc.perform(get("/v1/videos"))
                .andExpect(status().`is`(400))
    }
}