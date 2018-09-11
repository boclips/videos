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

class UserControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `get user info`() {
        mockMvc.perform(get("/v1/user").withTeacher())
                .andExpect(status().isOk)
    }

    @Test
    fun `returns 401 for anonymous search request`() {
        mockMvc.perform(get("/v1/user"))
                .andExpect(status().isUnauthorized)
    }

}