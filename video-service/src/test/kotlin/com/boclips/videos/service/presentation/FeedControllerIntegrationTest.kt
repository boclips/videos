package com.boclips.videos.service.presentation

import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asApiUser
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class FeedControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Test
    fun `can fetch all videos`() {
        val video1 = saveVideo(title = "1")
        val video2 = saveVideo(title = "2")
        val video3 = saveVideo(title = "3")

        mockMvc.perform(get("/v1/feed/videos").asApiUser())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(3)))
            .andExpect(
                jsonPath(
                    "$._embedded.videos[*].id", containsInAnyOrder(
                        video1.value,
                        video2.value,
                        video3.value
                    )
                )
            )
    }

    @Test
    fun `can page through using next link`() {
        saveVideo(title = "1")
        saveVideo(title = "2")
        val video3 = saveVideo(title = "3")

        val nextLink = mockMvc.perform(get("/v1/feed/videos?size=2").asApiUser())
            .andReturnLink("next")

        mockMvc.perform(get(nextLink.expand()).asApiUser())
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(1)))
            .andExpect(
                jsonPath(
                    "$._embedded.videos[*].id", containsInAnyOrder(
                        video3.value
                    )
                )
            )
    }

    @Test
    fun `next link does not exist when no results return`() {
        mockMvc.perform(get("/v1/feed/videos?size=2").asApiUser())
            .andExpect(jsonPath("$._embedded.videos", hasSize<Int>(0)))
            .andExpect(jsonPath("$._links.next").doesNotExist())
    }
}
