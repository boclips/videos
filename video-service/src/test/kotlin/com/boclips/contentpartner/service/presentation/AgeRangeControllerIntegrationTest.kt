package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.contentpartner.service.testsupport.ContentPartnerFactory
import com.boclips.videos.service.testsupport.asApiUser
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AgeRangeControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var ageRangeRepository: AgeRangeRepository

    @Test
    fun `post new age range creates a new age range`() {
        val ageRangeUrl = mockMvc.perform(
            post("/v1/age-ranges").contentType(MediaType.APPLICATION_JSON).content(
                """
                {
                    "id": "id1",
                    "label" : "label1",
                    "min": 3,
                    "max": 5
                }
                """.trimIndent()
            ).asBoclipsEmployee()
        ).andExpect(status().isCreated).andReturn().response.getHeader("Location")!!


        mockMvc.perform(get(ageRangeUrl).asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo("id1")))
            .andExpect(jsonPath("$.label", equalTo("label1")))
            .andExpect(jsonPath("$.min", equalTo(3)))
            .andExpect(jsonPath("$.max", equalTo(5)))
    }

    @Test
    fun `can create an age range without an upper bound`() {
        val ageRangeUrl = mockMvc.perform(
            post("/v1/age-ranges").contentType(MediaType.APPLICATION_JSON).content(
                """
                {
                    "id": "id1",
                    "label" : "label1",
                    "min": 3,
                    "max": null
                }
                """.trimIndent()
            ).asBoclipsEmployee()
        ).andExpect(status().isCreated).andReturn().response.getHeader("Location")!!


        mockMvc.perform(get(ageRangeUrl).asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$.id", equalTo("id1")))
            .andExpect(jsonPath("$.label", equalTo("label1")))
            .andExpect(jsonPath("$.min", equalTo(3)))
            .andExpect(jsonPath("$.max", equalTo(null)))
    }

    @Test
    fun `returns a 400 response when mandatory fields are not present`() {
        mockMvc.perform(
            post("/v1/age-ranges").contentType(MediaType.APPLICATION_JSON).content(
                """
                {
                    "id": "id1"
                }
                """.trimIndent()
            ).asBoclipsEmployee()
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `returns all age ranges`() {
        val ageRange1 = ContentPartnerFactory.createAgeRange(id = "id1")
        val ageRange2 = ContentPartnerFactory.createAgeRange(id = "id2")


        ageRangeRepository.create(ageRange = ageRange1)
        ageRangeRepository.create(ageRange = ageRange2)


        mockMvc.perform(get("/v1/age-ranges").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.ageRanges", hasSize<Any>(2)))
    }

    @Test
    fun `returns 403 if user doesn't have a right permission`() {
        val ageRange1 = ContentPartnerFactory.createAgeRange(id = "id1")
        val ageRange2 = ContentPartnerFactory.createAgeRange(id = "id2")


        ageRangeRepository.create(ageRange = ageRange1)
        ageRangeRepository.create(ageRange = ageRange2)


        mockMvc.perform(get("/v1/age-ranges").asApiUser())
            .andExpect(status().isForbidden)
    }
}
