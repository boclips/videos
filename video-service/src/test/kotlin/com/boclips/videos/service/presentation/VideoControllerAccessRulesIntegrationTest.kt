package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asApiUser
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideoControllerAccessRulesIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Nested
    inner class VideoSearch {
        @Test
        fun `limits search results to contracted IDs`() {
            saveVideo(title = "A non-contracted video")
            val firstContractedVideo = saveVideo(title = "Contracted video")
            val secondContractedVideo = saveVideo(title = "This is a movie about something else")

            createSelectedVideosAccessRules(firstContractedVideo.value, secondContractedVideo.value)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(firstContractedVideo.value)))
        }
    }

    @Nested
    inner class SingleVideo {
        @Test
        fun `returns a 404 response when user accesses a video they don't have a contract for`() {
            val idUserIsNotContractedTo = saveVideo(title = "A non-contracted video")
            val idUserIsContractedTo = saveVideo(title = "Contracted video")

            createSelectedVideosAccessRules(idUserIsContractedTo.value)

            mockMvc.perform(get("/v1/videos/$idUserIsNotContractedTo").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `returns the video if user has a access to it`() {
            val videoId = saveVideo(title = "Contracted video")

            createSelectedVideosAccessRules(videoId.value)

            mockMvc.perform(get("/v1/videos/$videoId").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
        }
    }
}
