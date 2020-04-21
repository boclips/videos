package com.boclips.videos.service.presentation

import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asApiUser
import com.boclips.videos.service.testsupport.asBoclipsEmployee
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
            addsAccessToStreamingVideos("api-user@gmail.com")

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
                saveVideo(title = "video included", contentProviderId = streamContentPartner.contentPartnerId.value)
            saveVideo(title = "video ignored", contentProviderId = downloadContentPartner.contentPartnerId.value)

            addsAccessToStreamingVideos("api-user@gmail.com")

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(streamVideo.value)))
        }

        @Test
        fun `excludes blacklisted videos from results`() {
            val video = saveVideo(title = "Some Video")
            val excludedVideo = saveVideo(title = "Blacklisted Video")

            addsAccessToStreamingVideos("api-user@gmail.com")
            removeAccessToVideo("api-user@gmail.com", excludedVideo.value)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(video.value)))
        }

        @Test
        fun `excludes certain video ContentTypes from results`() {
            val stockVideo = saveVideo(title = "Some Video", type = ContentType.STOCK)
            saveVideo(title = "Some Video", type = ContentType.NEWS)

            addsAccessToStreamingVideos("api-user@gmail.com")
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
                saveVideo(title = "Some Video", contentProviderId = allowedContentPartner.contentPartnerId.value)
            saveVideo(title = "Some Video", contentProviderId = excludedContentPartner.contentPartnerId.value)

            addsAccessToStreamingVideos("api-user@gmail.com")
            removeAccessToContentPartner("api-user@gmail.com", excludedContentPartner.contentPartnerId.value)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = "api-user@gmail.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(videoWithAllowedContentPartner.value)))
        }

        @Test
        fun `ignore access rules when backoffice user requests it`() {
            saveVideo(title = "A non-contracted video")
            val video = saveVideo(title = "Contracted video")

            addAccessToVideoIds("api-user@boclips.com", video.value)

            mockMvc.perform(get("/v1/videos?query=video&ignore_access_rules=true").asBoclipsEmployee(email = "api-user@boclips.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(2)))

            addAccessToVideoIds("api-user@someone-else.com", video.value)
            addsAccessToStreamingVideos("api-user@someone-else.com")

            mockMvc.perform(get("/v1/videos?query=video&ignore_access_rules=true").asApiUser(email = "api-user@someone-else.com"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
        }
    }
}
