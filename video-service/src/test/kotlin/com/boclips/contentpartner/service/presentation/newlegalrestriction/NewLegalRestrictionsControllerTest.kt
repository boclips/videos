package com.boclips.contentpartner.service.presentation.newlegalrestriction

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.bson.types.ObjectId
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

internal class NewLegalRestrictionsControllerTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `can fetch all legal restrictions`() {
        mockMvc.perform(get("/v1/new-legal-restrictions").asBoclipsEmployee())
            .andExpect(status().isOk)
    }

    @Test
    fun `can get one type of legal restrictions`() {
        val objectId1 = ObjectId().toString()
        val objectId2 = ObjectId().toString()

        val content = """
            {
                "id": "contentPartner",
                "restrictions": [{"id": "$objectId1", "text": "restriction one"}, {"id":"$objectId2", "text": "restriction two"}]
            }
        """.trimIndent()

        mockMvc.perform(
            post("/v1/new-legal-restrictions").asBoclipsEmployee().contentType(
                APPLICATION_JSON
            ).content(content)
        )
            .andExpect(status().isCreated)

        mockMvc.perform(get("/v1/new-legal-restrictions/type/contentPartner").asBoclipsEmployee())
            .andExpect(status().isOk)
    }

    @Test
    fun `can create a legal restriction`() {
        val objectId1 = ObjectId().toString()
        val objectId2 = ObjectId().toString()

        val content = """
            {
                "id": "contentPartner",
                "restrictions": [{"id": "$objectId1", "text": "restriction one"}, {"id":"$objectId2", "text": "restriction two"}]
            }
        """.trimIndent()

        mockMvc.perform(
            post("/v1/new-legal-restrictions").asBoclipsEmployee().contentType(
                APPLICATION_JSON
            ).content(content)
        )
            .andExpect(status().isCreated)

        mockMvc.perform(get("/v1/new-legal-restrictions").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded[*].id").exists())
            .andExpect(jsonPath("$._embedded[*].restrictions").exists())
            .andExpect(
                jsonPath(
                    "$._embedded[*].restrictions[*].id",
                    hasSize<Int>(2)
                )
            ).andExpect(
                jsonPath(
                    "$._embedded[*].restrictions[*].text",
                    containsInAnyOrder("restriction one", "restriction two")
                )
            )
    }
}