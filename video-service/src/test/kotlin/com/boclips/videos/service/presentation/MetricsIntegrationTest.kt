package com.boclips.videos.service.presentation

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asIngestor
import com.boclips.videos.service.testsupport.asUserWithRoles
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration

class MetricsIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `video counter increases when we create a video`() {
        setSecurityContext("anonymousUser")

        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val contentPartnerId = saveContentPartner().id.value

        val content = """
            {
                "provider": "AP",
                "providerVideoId": "1",
                "providerId": "$contentPartnerId",
                "title": "AP title",
                "description": "AP description",
                "releasedOn": "2018-12-04T00:00:00",
                "duration": 100,
                "legalRestrictions": "none",
                "keywords": ["k1", "k2"],
                "videoTypes": ["INSTRUCTIONAL_CLIPS"],
                "playbackId": "entry-$123",
                "playbackProvider": "KALTURA"
            }
        """.trimIndent()

        mockMvc.perform(post("/v1/videos").asIngestor().contentType(MediaType.APPLICATION_JSON).content(content))
            .andExpect(status().isCreated)
            .andReturn().response.getHeader("Location")

        mockMvc.perform(get("/actuator/prometheus").asUserWithRoles())
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("boclips_created_video_count_total ")))
    }
}
