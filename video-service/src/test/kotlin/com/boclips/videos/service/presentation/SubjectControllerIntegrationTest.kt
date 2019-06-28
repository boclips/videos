package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SubjectControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `create a subject`() {
        createSubject("Mathematics")
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", containsString("/subjects/")))
    }

    @Test
    fun `gets a subject`() {
        val subjectUrl = createSubject("Mathematics")
            .andReturn().response.getHeader("Location")!!

        mockMvc.perform(get(subjectUrl))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name", equalTo("Mathematics")))
            .andExpect(jsonPath("$._links.self.href").exists())
    }

    @Test
    fun `deletes a subject`() {
        val subjectUrl = createSubject("Mathematics")
            .andReturn().response.getHeader("Location")!!

        mockMvc.perform(delete(subjectUrl).asBoclipsEmployee())
            .andExpect(status().isOk)
    }

    @Test
    fun `returns list of subjects`() {
        createSubject("Mathematics")
        createSubject("French")

        mockMvc.perform(get("/v1/subjects").asTeacher())
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