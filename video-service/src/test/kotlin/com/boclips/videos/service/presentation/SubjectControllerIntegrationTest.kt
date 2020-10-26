package com.boclips.videos.service.presentation

import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.MvcMatchers.cacheableFor
import com.boclips.videos.service.testsupport.MvcMatchers.halJson
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asTeacher
import com.boclips.videos.service.testsupport.asUserWithRoles
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.concurrent.TimeUnit.HOURS

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
    fun `returns a status of 409 (Conflict) when attempting to create a subject with an existing name`() {
        createSubject("French")
            .andExpect(status().isCreated)

        createSubject("French")
            .andExpect(status().isConflict)
    }

    @Test
    fun `gets a cacheable subject`() {
        val subjectUrl = createSubject("Mathematics")
            .andReturn().response.getHeader("Location")!!

        mockMvc.perform(get(subjectUrl))
            .andExpect(halJson())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name", equalTo("Mathematics")))
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(cacheableFor(12, HOURS))
    }

    @Test
    fun `deletes a subject`() {
        val subjectUrl = createSubject("Mathematics")
            .andReturn().response.getHeader("Location")!!

        mockMvc.perform(delete(subjectUrl).asBoclipsEmployee())
            .andExpect(status().isOk)
    }

    @Test
    fun `returns a cacheable list of subjects`() {
        val createdSubject = saveSubject("Maths")
        saveSubject("French")

        mockMvc.perform(get("/v1/subjects"))
            .andExpect(status().isOk)
            .andExpect(halJson())
            .andExpect(cacheableFor(12, HOURS))
            .andExpect(jsonPath("$._embedded.subjects", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.subjects[0].id").exists())
            .andExpect(jsonPath("$._embedded.subjects[0].name").exists())
            .andExpect(
                jsonPath(
                    "$._embedded.subjects[0]._links.self.href",
                    endsWith("/subjects/${createdSubject.id.value}")
                )
            )
            .andExpect(jsonPath("$._embedded.subjects[0]._links.update").doesNotExist())
            .andExpect(jsonPath("$._links.self").exists())
    }

    @Test
    fun `returns a link for updating subjects as a hq user`() {
        val createdSubject = saveSubject("Maths")

        mockMvc.perform(get("/v1/subjects").asUserWithRoles(UserRoles.UPDATE_SUBJECTS))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.subjects", hasSize<Any>(1)))
            .andExpect(
                jsonPath(
                    "$._embedded.subjects[0]._links.update.href",
                    endsWith("/subjects/${createdSubject.id.value}")
                )
            )
    }

    @Test
    fun `teachers cannot modify subjects`() {
        val createdSubject = saveSubject("Maths")

        mockMvc.perform(post("/v1/subjects/${createdSubject.id.value}").asTeacher())
            .andExpect(status().isForbidden)
    }

    @Test
    fun `modify subject name`() {
        val createdSubject = saveSubject("Maths")

        mockMvc.perform(
            MockMvcRequestBuilders.put("/v1/subjects/${createdSubject.id.value}").asBoclipsEmployee()
                .contentType(MediaType.APPLICATION_JSON).content(
                    """{ "name": "Mathematics" }""".trim()
                )
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/v1/subjects/${createdSubject.id.value}").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("Mathematics")))
    }

    private fun createSubject(name: String): ResultActions {
        return mockMvc.perform(
            post("/v1/subjects").content(""" { "name": "$name" } """)
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
    }
}
