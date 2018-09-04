package com.boclips.videos.service

import com.boclips.videos.service.testsupport.AbstractIntegrationTest
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasSize
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideoSearchE2ETest : AbstractIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `exposes search endpoint`() {
        mockMvc.perform(get("/v1/videos?query=elephants"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(2)))
                .andExpect(jsonPath("$._embedded.videos[0].title", containsString("elephants")))
                .andExpect(jsonPath("$._embedded.videos[1].title", containsString("elephants")))
    }
}
