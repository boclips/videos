package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SubjectControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `create a subject`() {
        createSubject("Mathematics")
            .andExpect(status().isCreated)
    }

    @Test
    fun `returns list of subjects`() {
        createSubject("Mathematics")
        createSubject("French")

        mockMvc.perform(get("/v1/subjects"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.subjects", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.subjects[0].id").exists())
            .andExpect(jsonPath("$._embedded.subjects[0].name").exists())
            .andExpect(jsonPath("$._links.self.href").exists())
    }

    private fun createSubject(name: String): ResultActions {
        return mockMvc.perform(
            post("/v1/subjects").content(""" { "name": "$name" } """)
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
    }
}