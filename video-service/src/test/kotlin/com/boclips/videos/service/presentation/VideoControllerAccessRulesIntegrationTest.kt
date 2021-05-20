package com.boclips.videos.service.presentation

import com.boclips.users.api.factories.AccessRulesResourceFactory
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.api.response.channel.DistributionMethodResource
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.VoiceType
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
import java.util.*

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

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = userAssignedToOrganisation("api-user@gmail.com").idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(firstContractedVideo.value)))
        }

        @Test
        fun `limits search to included distribution methods`() {
            val streamContentPartner =
                saveChannel(name = "stream", distributionMethods = setOf(DistributionMethodResource.STREAM))
            val downloadContentPartner =
                saveChannel(name = "download", distributionMethods = setOf(DistributionMethodResource.DOWNLOAD))

            val streamVideo =
                saveVideo(title = "video included", existingChannelId = streamContentPartner.id.value)
            saveVideo(title = "video ignored", existingChannelId = downloadContentPartner.id.value)

            addDistributionMethodAccessRule("api-user@gmail.com", DistributionMethodResource.STREAM)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = userAssignedToOrganisation("api-user@gmail.com").idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(streamVideo.value)))
        }

        @Test
        fun `can retrieve downloadable videos when content package says so`() {
            val streamContentPartner =
                saveChannel(name = "stream", distributionMethods = setOf(DistributionMethodResource.STREAM))
            val downloadContentPartner =
                saveChannel(name = "download", distributionMethods = setOf(DistributionMethodResource.DOWNLOAD))
            val downloadAndStreamContentPartner =
                saveChannel(
                    name = "download",
                    distributionMethods = setOf(DistributionMethodResource.DOWNLOAD, DistributionMethodResource.STREAM)
                )

            saveVideo(
                title = "video no",
                existingChannelId = streamContentPartner.id.value
            )
            val downloadVideo = saveVideo(
                title = "video si",
                existingChannelId = downloadContentPartner.id.value
            )
            val downloadAndStreamVideo = saveVideo(
                title = "video si siempre",
                existingChannelId = downloadAndStreamContentPartner.id.value
            )

            addDistributionMethodAccessRule("something@publisher-boclips.com", DistributionMethodResource.DOWNLOAD)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = userAssignedToOrganisation("something@publisher-boclips.com").idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(2)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(downloadVideo.value)))
                .andExpect(jsonPath("$._embedded.videos[1].id", equalTo(downloadAndStreamVideo.value)))
        }

        @Test
        fun `removes access to videos`() {
            val video = saveVideo(title = "Some Video")
            val excludedVideo = saveVideo(title = "Bad Video")

            removeAccessToVideo("api-user@gmail.com", excludedVideo.value)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = userAssignedToOrganisation("api-user@gmail.com").idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(video.value)))
        }

        @Test
        fun `excludes certain video ContentTypes from results`() {
            val stockVideo = saveVideo(title = "Some Video", types = listOf(VideoType.STOCK))
            saveVideo(title = "Some Video 2", types = listOf(VideoType.NEWS))

            removeAccessToVideoTypes("api-user@gmail.com", VideoType.NEWS, VideoType.INSTRUCTIONAL_CLIPS)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = userAssignedToOrganisation("api-user@gmail.com").idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(stockVideo.value)))
        }

        @Test
        fun `includes certain video ContentTypes from results`() {
            saveVideo(title = "Some Video", types = listOf(VideoType.STOCK))
            val newsVideo = saveVideo(title = "Some Video 2", types = listOf(VideoType.NEWS))

            addAccessToVideoTypes("api-user@gmail.com", VideoType.NEWS)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = userAssignedToOrganisation("api-user@gmail.com").idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(newsVideo.value)))
        }

        @Test
        fun `excludes certain videos with excluded content partners from results`() {
            val allowedContentPartner = saveChannel(name = "Turna")
            val excludedContentPartner = saveChannel(name = "Tiner")

            val videoWithAllowedContentPartner =
                saveVideo(title = "Some Video", existingChannelId = allowedContentPartner.id.value)
            saveVideo(title = "Some Video", existingChannelId = excludedContentPartner.id.value)

            removeAccessToChannel("api-user@gmail.com", excludedContentPartner.id.value)

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = userAssignedToOrganisation("api-user@gmail.com").idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(videoWithAllowedContentPartner.value)))
        }

        @Test
        fun `includes voiced content only`() {
            val voicedVideo = saveVideo(title = "voice", isVoiced = true)
            saveVideo(title = "no voice", isVoiced = false)

            addAccessToVoiceType("api-user@gmail.com", VoiceType.WITH_VOICE)

            mockMvc.perform(get("/v1/videos?query=voice").asApiUser(email = userAssignedToOrganisation("api-user@gmail.com").idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(voicedVideo.value)))
        }

        @Test
        fun `setting query params does not give access if access rules do not permit access to this content`() {
            saveVideo(title = "instructional", types = listOf(VideoType.INSTRUCTIONAL_CLIPS))

            addAccessToVideoTypes("api-user@gmail.com", VideoType.NEWS)

            mockMvc.perform(
                get("/v1/videos?query=instructional&type=INSTRUCTIONAL").asApiUser(
                    email = userAssignedToOrganisation(
                        "api-user@gmail.com"
                    ).idOrThrow().value
                )
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(0)))
        }

        @Test
        fun `excludes video with certain languages`() {
            val french =
                saveVideo(title = "How did Wales lost the 6 nations?", language = Locale.FRENCH.toLanguageTag())
            saveVideo(title = "English finish 5th in the 6 nations", language = Locale.ENGLISH.toLanguageTag())

            removeAccessToLanguage("api-user@gmail.com", Locale.ENGLISH)

            mockMvc.perform(get("/v1/videos?query=nations").asApiUser(email = userAssignedToOrganisation("api-user@gmail.com").idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].id", equalTo(french.value)))
        }

        @Test
        fun `includes the videos of collection access rules`() {
            val videoId = saveVideo(title = "hello")
            saveVideo(title = "hello there")
            val collectionId = saveCollection(videos = listOf(videoId.value))

            usersClient.addAccessRules(
                "api-user@gmail.com",
                AccessRulesResourceFactory.sample(
                    AccessRuleResource.IncludedCollections(
                        id = "access-rule-id",
                        name = UUID.randomUUID().toString(),
                        collectionIds = listOf(collectionId.value)
                    )
                )
            )

            mockMvc.perform(get("/v1/videos?query=hello").asApiUser(email = userAssignedToOrganisation("api-user@gmail.com").idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("hello")))
        }

        @Test
        fun `excludes videos by playback source`() {
            saveVideo(
                title = "kaltura video",
                playbackId = PlaybackId(
                    type = PlaybackProviderType.KALTURA,
                    value = "id-${UUID.randomUUID()}"
                )
            )
            saveVideo(
                title = "youtube video",
                playbackId = PlaybackId(
                    type = PlaybackProviderType.YOUTUBE,
                    value = "id-${UUID.randomUUID()}"
                )
            )

            usersClient.addAccessRules(
                "api-user@gmail.com",
                AccessRulesResourceFactory.sample(
                    AccessRuleResource.ExcludedPlaybackSources(
                        id = "access-rule-id",
                        name = UUID.randomUUID().toString(),
                        sources = setOf("YOUTUBE")
                    )
                )
            )

            mockMvc.perform(get("/v1/videos?query=video").asApiUser(email = userAssignedToOrganisation("api-user@gmail.com").idOrThrow().value))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$._embedded.videos", hasSize<Any>(1)))
                .andExpect(jsonPath("$._embedded.videos[0].title", equalTo("kaltura video")))
        }
    }
}
