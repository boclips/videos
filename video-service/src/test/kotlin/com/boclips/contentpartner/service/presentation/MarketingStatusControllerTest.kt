package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.ContentPartnerStatus
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asUserWithRoles
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class MarketingStatusControllerTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    val getStatusesLink = "/v1/marketing-statuses"

    @Test
    fun `can fetch all marketing statuses`() {
        mockMvc.perform(get(getStatusesLink).asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$._embedded.statuses",
                    hasSize<Int>(ContentPartnerStatus.values().size)
                )
            )
            .andExpect(jsonPath("$._links.self.href", endsWith(getStatusesLink)))
    }

    @Test
    fun `does not have access to marketing statuses without correct role`() {
        mockMvc.perform(get(getStatusesLink).asUserWithRoles())
            .andExpect(status().isForbidden)
    }
}