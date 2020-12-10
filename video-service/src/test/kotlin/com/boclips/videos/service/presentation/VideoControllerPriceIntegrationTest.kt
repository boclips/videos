package com.boclips.videos.service.presentation

import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asUserWithRoles
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideoControllerPriceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `can retrieve price of a video`() {
        val videoId = saveVideo()

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/videos/${videoId.value}")
                .asUserWithRoles(UserRoles.VIEW_VIDEOS, UserRoles.PUBLISHER)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.price.displayValue", equalTo("$600")))
            .andExpect(jsonPath("$.price.amount", equalTo(600)))
            .andExpect(jsonPath("$.price.currency", equalTo("USD")))
    }

    @Test
    fun `user with internal role has access to price`() {
        val videoId = saveVideo()

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/videos/${videoId.value}")
                .asUserWithRoles(UserRoles.VIEW_VIDEOS, UserRoles.BOCLIPS_SERVICE)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.price").exists())
    }

    @Test
    fun `user with missing roles does not have access to price`() {
        val videoId = saveVideo()

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/videos/${videoId.value}").asUserWithRoles(UserRoles.VIEW_VIDEOS)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.price").doesNotExist())
    }
}
