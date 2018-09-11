package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.withTeacher
import org.hamcrest.Matchers.*
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class VideoControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `search for videos`() {
        mockMvc.perform(get("/v1/videos?query=powerful").withTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("powerful video about elephants")))
                .andExpect(jsonPath("$._embedded.videos[0].description", equalTo("test description 3")))
                .andExpect(jsonPath("$._embedded.videos[0].releasedOn", equalTo("2018-02-11")))
                .andExpect(jsonPath("$._embedded.videos[0].duration", equalTo("PT1M2S")))
                .andExpect(jsonPath("$._embedded.videos[0].contentProvider", equalTo("cp")))
    }

    @Test
    fun `returns 401 for anonymous search request`() {
        mockMvc.perform(get("/v1/videos"))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `returns 400 for invalid search request`() {
        mockMvc.perform(get("/v1/videos").withTeacher())
                .andExpect(status().`is`(400))
    }

    @Test
    fun `returns empty videos array when there are no results`() {
        mockMvc.perform(get("/v1/videos?query=somethingthatdoesntexistever").withTeacher())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", emptyIterable<Any>()))
    }
}