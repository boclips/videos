package com.boclips.videos.service.presentation

import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class DistributionMethodsControllerTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `can fetch distribution methods`() {
        mockMvc.perform(get("/v1/distribution-methods"))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(
                jsonPath(
                    "$._embedded.distributionMethods[0]", equalTo(DistributionMethodResource.DOWNLOAD.toString())
                )
            )
            .andExpect(
                jsonPath(
                    "$._embedded.distributionMethods[1]", equalTo(DistributionMethodResource.STREAM.toString())
                )
            )
    }
}