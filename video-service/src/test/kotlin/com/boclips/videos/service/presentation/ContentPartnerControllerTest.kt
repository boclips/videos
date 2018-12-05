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
    fun `video lookup by provider id returns 200 when asset exists`() {
        saveVideo(100L, contentProvider = "ted", contentProviderId = "abc")

        mockMvc.perform(head("/v1/content_partners/ted/partner_video_id/abc").asIngestor())
                .andExpect(status().isOk)
    }

    @Test
    fun `video lookup by provider id returns 404 when asset does not exist`() {
        mockMvc.perform(head("/v1/content_partners/ted/partner_video_id/xyz").asIngestor())
                .andExpect(status().isNotFound)
    }
}