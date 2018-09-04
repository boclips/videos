package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class HealthControllerIntegrationTest: AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mvc: MockMvc

    @Test
    fun `health endpoint is available`() {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk)
    }
}