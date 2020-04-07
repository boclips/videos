package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.legalrestrictions.ContractLegalRestrictionsRepository
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

internal class ContractLegalRestrictionsControllerTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var repository: ContractLegalRestrictionsRepository

    @Test
    fun `can fetch all legal restrictions`() {

        val restrictionOne = repository.create(text = "legal restriction 1")
        val restrictionTwo = repository.create(text = "legal restriction 2")

        mockMvc.perform(get("/v1/contract-legal-restrictions").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.restrictions", hasSize<Int>(2)))
            .andExpect(
                jsonPath(
                    "$._embedded.restrictions[*].text",
                    containsInAnyOrder(restrictionOne.text, restrictionTwo.text)
                )
            )
    }

    @Test
    fun `can create a legal restriction`() {
        val content = """
            {
                "text": "New Legal restriction"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/v1/contract-legal-restrictions").asBoclipsEmployee().contentType(
                APPLICATION_JSON
            ).content(content)
        )
            .andExpect(status().isCreated)

        mockMvc.perform(get("/v1/contract-legal-restrictions").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.restrictions", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.restrictions[*].id").exists())
            .andExpect(jsonPath("$._embedded.restrictions[*].text", containsInAnyOrder("New Legal restriction")))
    }
}