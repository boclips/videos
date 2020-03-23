package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asTeacher
import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class DisciplinesControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `create a discipline`() {
        createDiscipline(code = "math", name = "Mathematics")
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
    }

    @Test
    fun `returns list of disciplines`() {
        createDiscipline(code = "math", name = "Mathematics")
        createDiscipline(code = "meth", name = "That other thing")

        mockMvc.perform(get("/v1/disciplines").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.disciplines", hasSize<Any>(2)))
            .andExpect(jsonPath("$._embedded.disciplines[0].id").exists())
            .andExpect(jsonPath("$._embedded.disciplines[0].name", equalTo("Mathematics")))
            .andExpect(jsonPath("$._embedded.disciplines[0].code", equalTo("math")))
            .andExpect(jsonPath("$._links.self.href").exists())
    }

    @Test
    fun `attaches subjects to disciplines`() {
        val subject1Url = createSubject("electromagnetism")
        val subject2Url = createSubject("thermodynamics")

        val newDisciplineResponse =
            createDiscipline(code = "physics", name = "Physics").andReturn().response.contentAsString
        val subjectsUrl = JsonPath.parse(newDisciplineResponse).read<String>("$._links.subjects.href")
        val disciplineUrl = JsonPath.parse(newDisciplineResponse).read<String>("$._links.self.href")

        mockMvc.perform(
            put(subjectsUrl).content(
                """
                $subject1Url
                $subject2Url
                """.trimIndent()
            )
                .contentType("text/uri-list")
                .asBoclipsEmployee()
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(get(disciplineUrl).asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name", equalTo("Physics")))
            .andExpect(jsonPath("$.code", equalTo("physics")))
            .andExpect(jsonPath("$.subjects[0].name", equalTo("electromagnetism")))
            .andExpect(jsonPath("$.subjects[1].name", equalTo("thermodynamics")))
            .andExpect(jsonPath("$._links.self").exists())
    }

    @Test
    fun `order of the subjects in a discipline is persisted`() {
        val subject1Url = createSubject("electromagnetism")
        val subject2Url = createSubject("thermodynamics")
        val subject3Url = createSubject("other1")
        val subject4Url = createSubject("other2")

        val newDisciplineResponse =
            createDiscipline(code = "physics", name = "Physics").andReturn().response.contentAsString
        val subjectsUrl = JsonPath.parse(newDisciplineResponse).read<String>("$._links.subjects.href")
        val disciplineUrl = JsonPath.parse(newDisciplineResponse).read<String>("$._links.self.href")

        mockMvc.perform(
            put(subjectsUrl).content(
                """
                $subject4Url
                $subject2Url
                $subject1Url
                $subject3Url
                """.trimIndent()
            )
                .contentType("text/uri-list")
                .asBoclipsEmployee()
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(get(disciplineUrl).asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name", equalTo("Physics")))
            .andExpect(jsonPath("$.code", equalTo("physics")))
            .andExpect(jsonPath("$.subjects[0].name", equalTo("other2")))
            .andExpect(jsonPath("$.subjects[1].name", equalTo("thermodynamics")))
            .andExpect(jsonPath("$.subjects[2].name", equalTo("electromagnetism")))
            .andExpect(jsonPath("$.subjects[3].name", equalTo("other1")))
            .andExpect(jsonPath("$._links.self").exists())
    }

    @Test
    fun `fetch a discipline as a Boclips employee`() {
        val disciplineResponse =
            createDiscipline(code = "humanities", name = "Humanities").andReturn().response.contentAsString
        val disciplineUrl = JsonPath.parse(disciplineResponse).read<String>("$._links.self.href")

        mockMvc.perform(get(disciplineUrl).asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name", equalTo("Humanities")))
            .andExpect(jsonPath("$.code", equalTo("humanities")))
            .andExpect(jsonPath("$._links.self").exists())
            .andExpect(jsonPath("$._links.subjects").exists())
            .andExpect(jsonPath("$._links.update").exists())
    }

    @Test
    fun `update a discipline`() {
        val disciplineResponse =
            createDiscipline(code = "humanities", name = "Humanities").andReturn().response.contentAsString
        val disciplineUrl = JsonPath.parse(disciplineResponse).read<String>("$._links.self.href")

        mockMvc.perform(
            put(disciplineUrl).content(
                """
            {"name": "Social Studies",
            "code": "social-studies"}
        """.trim()
            ).asBoclipsEmployee()
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(get(disciplineUrl).asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name", equalTo("Social Studies")))
            .andExpect(jsonPath("$.code", equalTo("social-studies")))
    }

    private fun createDiscipline(code: String, name: String): ResultActions {
        return mockMvc.perform(
            post("/v1/disciplines").content(
                """
                {
                  "code": "$code",
                  "name": "$name"
                }
                """.trimIndent()
            )
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        )
    }

    private fun createSubject(name: String): String {
        return mockMvc.perform(
            post("/v1/subjects").content(""" { "name": "$name" } """)
                .contentType(MediaType.APPLICATION_JSON)
                .asBoclipsEmployee()
        ).andReturn().response.getHeader("location")!!
    }
}
