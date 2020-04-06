package com.boclips.videos.service.presentation

import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.SpecificAgeRange
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asTeacher
import com.boclips.videos.service.testsupport.asUserWithRoles
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration

class VideoControllerAttachmentsIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    lateinit var kalturaVideoId: String

    @BeforeEach
    fun setUp() {
        kalturaVideoId = saveVideo(
            playbackId = PlaybackId(value = "entry-id-123", type = PlaybackProviderType.KALTURA),
            title = "powerful video about elephants",
            description = "test description 3",
            date = "2018-02-11",
            duration = Duration.ofMinutes(1),
            contentProvider = "enabled-cp",
            legalRestrictions = "None",
            ageRange = SpecificAgeRange(min = 5, max = 7)
        ).value
    }

    @Test
    fun `attach an activity to a video`() {
        mockMvc.perform(
            put("/v1/videos/$kalturaVideoId/attachments")
                .asUserWithRoles(UserRoles.UPDATE_VIDEOS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "description": "Rare attachment",
                      "linkToResource": "example.com/rare",
                      "type": "ACTIVITY"
                    }
                """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.description", equalTo("Rare attachment")))
            .andExpect(jsonPath("$._links.download.href", equalTo("example.com/rare")))
            .andExpect(jsonPath("$.type", equalTo("ACTIVITY")))

        mockMvc.perform(get("/v1/videos/$kalturaVideoId").asTeacher())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.attachments", hasSize<Int>(1)))
            .andExpect(jsonPath("$.attachments[0].id").exists())
            .andExpect(jsonPath("$.attachments[0].description").exists())
            .andExpect(jsonPath("$.attachments[0].type").exists())
            .andExpect(jsonPath("$.attachments[0]._links.download.href", equalTo("example.com/rare")))
    }
}
