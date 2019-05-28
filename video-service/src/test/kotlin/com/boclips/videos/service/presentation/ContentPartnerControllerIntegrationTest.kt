package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import com.boclips.videos.service.testsupport.asIngestor
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ContentPartnerControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var contentPartnerRepository: ContentPartnerRepository

    @Test
    fun `video lookup by provider id returns 200 when video exists`() {
        saveVideo(contentProvider = "ted", contentProviderId = "abc")

        mockMvc.perform(head("/v1/content-partners/ted/videos/abc").asIngestor())
            .andExpect(status().isOk)
    }

    @Test
    fun `video lookup by provider id returns 404 when video does not exist`() {
        mockMvc.perform(head("/v1/content-partners/ted/videos/xyz").asIngestor())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `create a content partner`() {
        val content = """
            {
                "name": "TED",
                "ageRange":
                    {
                        "min": 11,
                        "max": 18
                    }
            }
        """

        mockMvc.perform(post("/v1/content-partners").asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(content))
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
    }

    @Test
    fun `updating a content partner`() {
        val originalContent = """
            {
                "name": "TED-ED",
                "ageRange":
                    {
                        "min": 10,
                        "max": 19
                    }
            }
        """
        val updatedContent = """
            {
                "name": "TED",
                "ageRange":
                    {
                        "min": 11,
                        "max": 18
                    }
            }
        """

        val cpUrl = mockMvc.perform(post("/v1/content-partners").asBoclipsEmployee()
            .contentType(MediaType.APPLICATION_JSON).content(originalContent))
            .andReturn().response.getHeaders("Location").first()

        mockMvc.perform(put(cpUrl).asBoclipsEmployee().contentType(MediaType.APPLICATION_JSON).content(updatedContent))
            .andExpect(status().isNoContent)

        mockMvc.perform(get(cpUrl).asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name", equalTo("TED")))
            .andExpect(jsonPath("$.ageRange.min", equalTo(11)))
            .andExpect(jsonPath("$.ageRange.max", equalTo(18)))
    }
}