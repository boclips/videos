package com.boclips.contentpartner.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class LegalRestrictionsControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `create restrictions`() {
        val link = mockMvc.perform(post("/v1/legal-restrictions?text=my restrictions").asBoclipsEmployee())
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.text", `is`("my restrictions")))
            .andExpect(jsonPath("$._links.self.href").exists())
            .andReturnLink("self").expand()

        mockMvc.perform(get(link).asBoclipsEmployee())
            .andExpect(status().isOk)
    }

    @Test
    fun `retrieve restrictions`() {
        val link = mockMvc.perform(post("/v1/legal-restrictions?text=my restrictions").asBoclipsEmployee())
            .andExpect(status().isCreated)
            .andReturnLink("self").expand()

        mockMvc.perform(get(link).asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.text", `is`("my restrictions")))
            .andExpect(jsonPath("$._links.self.href", `is`(link)))
    }

    @Test
    fun `retrieve restrictions when dont exist`() {
        val validIdThatDoesntExist = "5d820019195d1081a0cfc4eb"
        mockMvc.perform(get("/v1/legal-restrictions/$validIdThatDoesntExist").asBoclipsEmployee())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `retrieve all restrictions`() {
        mockMvc.perform(post("/v1/legal-restrictions?text=restrictions 1").asBoclipsEmployee()).andExpect(status().isCreated)
        mockMvc.perform(post("/v1/legal-restrictions?text=restrictions 2").asBoclipsEmployee()).andExpect(status().isCreated)

        mockMvc.perform(get("/v1/legal-restrictions").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.legalRestrictions", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.legalRestrictions[0].text", startsWith("restrictions ")))
    }
}
