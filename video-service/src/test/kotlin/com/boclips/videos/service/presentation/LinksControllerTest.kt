package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.hamcrest.CoreMatchers
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class LinksControllerTest : AbstractSpringIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GET returns links`() {
        mockMvc.perform(get("/v1"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._links.search.href", CoreMatchers.containsString("/videos?query=")))
                .andExpect(jsonPath("$._links.search.templated", CoreMatchers.equalTo(true)))
    }
}