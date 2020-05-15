package com.boclips.videos.service.presentation

import com.boclips.videos.api.request.contentwarning.CreateContentWarningRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

internal class ContentWarningControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `load all content warnings`() {
        createContentWarning(CreateContentWarningRequest("Discusses drug or alcohol use"))
        createContentWarning(CreateContentWarningRequest("Discusses mental health"))

        mockMvc.perform(get("/v1/content-warnings").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.contentWarnings", hasSize<String>(2)))
            .andExpect(jsonPath("$._embedded.contentWarnings[0].label", equalTo("Discusses drug or alcohol use")))
            .andExpect(jsonPath("$._embedded.contentWarnings[1].label", equalTo("Discusses mental health")))
    }

    @Test
    fun `can create a new content warning`() {
        mockMvc.perform(
            post("/v1/content-warnings")
                .content(
                    """
                {
                  "label": "New Warning"
                }
                """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.label", equalTo("New Warning")))
    }
}