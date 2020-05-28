package com.boclips.videos.service.presentation

import com.boclips.videos.api.response.channel.DistributionMethodResource
import com.boclips.videos.service.domain.model.video.ContentType
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
        fun `limits search results to included videoIds`() {
            saveVideo(title = "A non-contracted video")
            val firstContractedVideo = saveVideo(title = "Contracted video")
            val secondContractedVideo = saveVideo(title = "This is a movie about something else")

            addAccessToVideoIds("api-user@gmail.com", firstContractedVideo.value, secondContractedVideo.value)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(firstContractedVideo.value)))
        }

        @Test
        fun `limits search to included distribution methods`() {
            val streamContentPartner =
                saveContentPartner(name = "stream", distributionMethods = setOf(DistributionMethodResource.STREAM))
            val downloadContentPartner =
                saveContentPartner(name = "download", distributionMethods = setOf(DistributionMethodResource.DOWNLOAD))

            val streamVideo =
                saveVideo(title = "video included", contentProviderId = streamContentPartner.id.value)
            saveVideo(title = "video ignored", contentProviderId = downloadContentPartner.id.value)

            addsAccessToStreamingVideos("api-user@gmail.com", DistributionMethodResource.STREAM)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(streamVideo.value)))
        }

        @Test
        fun `excludes blacklisted videos from results`() {
            val video = saveVideo(title = "Some Video")
            val excludedVideo = saveVideo(title = "Blacklisted Video")

            removeAccessToVideo("api-user@gmail.com", excludedVideo.value)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(video.value)))
        }

        @Test
        fun `excludes certain video ContentTypes from results`() {
            val stockVideo = saveVideo(title = "Some Video", type = ContentType.STOCK)
            saveVideo(title = "Some Video 2", type = ContentType.NEWS)

            addAccessToVideoTypes("api-user@gmail.com", ContentType.NEWS, ContentType.INSTRUCTIONAL_CLIPS)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(stockVideo.value)))
        }

        @Test
        fun `excludes certain videos with excluded content partners from results`() {
            val allowedContentPartner = saveContentPartner(name = "Turna")
            val excludedContentPartner = saveContentPartner(name = "Tiner")

            val videoWithAllowedContentPartner =
                saveVideo(title = "Some Video", contentProviderId = allowedContentPartner.id.value)
            saveVideo(title = "Some Video", contentProviderId = excludedContentPartner.id.value)

            removeAccessToContentPartner("api-user@gmail.com", excludedContentPartner.id.value)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(videoWithAllowedContentPartner.value)))
        }
    }
}
