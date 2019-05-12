package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asIngestor
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ContentPartnerControllerTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

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
}