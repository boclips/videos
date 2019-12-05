package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asApiUser
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideoTypeControllerIntegrationTest : AbstractSpringIntegrationTest() {
    val targetUrl = "/v1/video-types"

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `returns available video types for authenticated API users`() {
        mockMvc.perform(get(targetUrl).asApiUser())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videoTypes", hasSize<Any>(3)))
            .andExpect(jsonPath("$._embedded.videoTypes", containsInAnyOrder("NEWS", "STOCK", "INSTRUCTIONAL")))
    }

    @Test
    fun `returns a 403 response for unauthenticated users`() {
        mockMvc.perform(get(targetUrl))
            .andExpect(status().isForbidden)
    }
}