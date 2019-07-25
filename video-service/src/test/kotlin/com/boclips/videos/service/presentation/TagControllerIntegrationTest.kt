package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TagControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `create a tag`() {
        createTag(label = "Explainer")
            .andExpect(status().isCreated)
    }

    @Test
    fun `returns a status of 409 (Conflict) when attempting to create a tag with an existing label`() {
        createTag("Explainer")
            .andExpect(status().isCreated)

        createTag("Explainer")
            .andExpect(status().isConflict)
    }

    @Test
    fun `gets a tag`() {
        val tagUrl = createTag("Explainer")
            .andReturn().response.getHeader("Location")!!

        mockMvc.perform(get(tagUrl).asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.label", equalTo("Explainer")))
            .andExpect(jsonPath("$._links.self.href").exists())
    }

    @Test
    fun `deletes a tag`() {
        val tagUrl = createTag("Explainer")
            .andReturn().response.getHeader("Location")!!

        mockMvc.perform(MockMvcRequestBuilders.delete(tagUrl).asBoclipsEmployee())
            .andExpect(status().isOk)
    }

    @Test
    fun `returns list of tags`() {
        createTag(label = "Explainer")
        createTag(label = "That other thing")

        mockMvc.perform(get("/v1/tags").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.tags", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.tags[0].id").exists())
            .andExpect(jsonPath("$._embedded.tags[0].label", equalTo("Explainer")))
            .andExpect(jsonPath("$._embedded.tags[0]._links.self.href").exists())
            .andExpect(jsonPath("$._links.self.href").exists())
    }

    fun createTag(label: String): ResultActions {
        return mockMvc.perform(
            post("/v1/tags").content(
                """
                {
                  "label": "$label"
                }
                """.trimIndent()
            )
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
    }
}
