package com.boclips.videos.service.config.security

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CorsConfigIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `accepts requests from trusted origins (CORS)`() {
        mockMvc.perform(MockMvcRequestBuilders
                .options("/v1/videos?query=powerful")
                .header("Origin", "http://localhost:8081"))
                .andExpect(status().isOk)

        mockMvc.perform(MockMvcRequestBuilders
                .options("/v1/videos?query=powerful")
                .header("Origin", "https://educators.staging-boclips.com"))
                .andExpect(status().isOk)

        mockMvc.perform(MockMvcRequestBuilders
                .options("/v1/videos?query=powerful")
                .header("Origin", "https://educators.testing-boclips.com"))
                .andExpect(status().isOk)

        mockMvc.perform(MockMvcRequestBuilders
                .options("/v1/videos?query=powerful")
                .header("Origin", "https://educators.boclips.com"))
                .andExpect(status().isOk)

        mockMvc.perform(MockMvcRequestBuilders
                .options("/v1/videos?query=powerful")
                .header("Origin", "https://example.com"))
                .andExpect(status().isForbidden)
    }
}