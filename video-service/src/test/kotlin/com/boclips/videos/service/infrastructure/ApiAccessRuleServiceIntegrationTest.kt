package com.boclips.videos.service.infrastructure

import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.users.client.implementation.FakeUserServiceClient
import com.boclips.users.client.model.accessrule.ExcludedContentPartnersAccessRule
import com.boclips.users.client.model.accessrule.ExcludedVideoTypesAccessRule
import com.boclips.users.client.model.accessrule.ExcludedVideosAccessRule
import com.boclips.users.client.model.accessrule.IncludedCollectionsAccessRule
import com.boclips.users.client.model.accessrule.IncludedDistributionMethodsAccessRule
import com.boclips.users.client.model.accessrule.IncludedVideosAccessRule
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.ContentPartnerId
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class ApiAccessRuleServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Test
    fun `passes through client's response if all is well`() {
        val collectionsContract = IncludedCollectionsAccessRule().apply {
            name = "Test Contract"
            collectionIds = listOf("test-collection-id")
        }

        val videoId = ObjectId().toHexString()
        val videosContract = IncludedVideosAccessRule().apply {
            name = "Test Contract"
            videoIds = listOf(videoId)
        }

        whenever(userServiceClient.getAccessRules(anyString()))
            .thenReturn(
                listOf(collectionsContract, videosContract)
            )

        val accessRules = accessRuleService.getRules(UserFactory.sample(id = "test-user"))

        assertThat(accessRules.collectionAccess).isEqualTo(
            CollectionAccessRule.SpecificIds(setOf(CollectionId("test-collection-id")))
        )
        assertThat((accessRules.videoAccess as VideoAccess.Rules).accessRules).containsExactly(
            VideoAccessRule.IncludedIds(
                videoIds = setOf(VideoId(videoId))
            )
        )
    }

    @Test
    fun `returns default when content package cannot be found`() {
        whenever(userServiceClient.getAccessRules(anyString()))
            .thenReturn(null)

        val accessRules = accessRuleService.getRules(UserFactory.sample(id = "test-user"))

        assertThat(accessRules.collectionAccess).isEqualTo(
            CollectionAccessRule.asOwner(me = UserId(value = "test-user"))
        )
        assertThat(accessRules.videoAccess).isEqualTo(VideoAccess.Everything)
    }

    @Nested
    inner class CollectionsAccess {
        @Test
        fun `has access specific user's collections when no contracts specified`() {
            whenever(userServiceClient.getAccessRules(anyString()))
                .thenReturn(
                    emptyList()
                )

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            assertThat(accessRules.collectionAccess).isEqualTo(
                CollectionAccessRule.SpecificOwner(
                    owner = UserId(
                        value = user.id.value
                    )
                )
            )
        }

        @Test
        fun `has access to everything if is allowed to view any collection`() {
            whenever(userServiceClient.getAccessRules(anyString()))
                .thenReturn(
                    emptyList()
                )

            val user = UserFactory.sample(id = "test-user", isPermittedToViewAnyCollection = true)
            val accessRules = accessRuleService.getRules(user)

            assertThat(accessRules.collectionAccess).isEqualTo(CollectionAccessRule.Everything)
        }
    }

    @Nested
    inner class AccessingVideos {
        @Test
        fun `has access to everything when no contracts specified`() {
            whenever(userServiceClient.getAccessRules(anyString()))
                .thenReturn(
                    emptyList()
                )

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            assertThat(accessRules.videoAccess).isEqualTo(VideoAccess.Everything)
        }

        @Test
        fun `can convert ExcludedVideosAccess rule to domain`() {
            val firstId = TestFactories.createVideoId()
            val secondId = TestFactories.createVideoId()
            whenever(userServiceClient.getAccessRules(anyString()))
                .thenReturn(
                    listOf(ExcludedVideosAccessRule().apply {
                        name = "bad videos"
                        videoIds = listOf(firstId.value, secondId.value)
                    })
                )
            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsOnly(VideoAccessRule.ExcludedIds(setOf(firstId, secondId)))
        }

        @Test
        fun `can convert ExcludedVideoTypesAccess rule to domain`() {
            whenever(userServiceClient.getAccessRules(anyString()))
                .thenReturn(
                    listOf(ExcludedVideoTypesAccessRule().apply {
                        name = "bad video types"
                        videoTypes = listOf("NEWS")
                    })
                )
            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsOnly(VideoAccessRule.ExcludedContentTypes(setOf(ContentType.NEWS)))
        }

        @Test
        fun `ignores any unknown content types`() {
            whenever(userServiceClient.getAccessRules(anyString()))
                .thenReturn(
                    listOf(ExcludedVideoTypesAccessRule().apply {
                        name = "bad video types"
                        videoTypes = listOf("UNKNOWN", "NEWS")
                    })
                )
            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsExactly(VideoAccessRule.ExcludedContentTypes(setOf(ContentType.NEWS)))
        }

        @Test
        fun `access to everything if only unknown content types are specified`() {
            whenever(userServiceClient.getAccessRules(anyString()))
                .thenReturn(
                    listOf(ExcludedVideoTypesAccessRule().apply {
                        name = "bad video types"
                        videoTypes = listOf("UNKNOWN")
                    })
                )
            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            assertThat(accessRules.videoAccess is VideoAccess.Everything).isTrue()
        }

        @Test
        fun `can convert ExcludedContent types to domain`() {
            whenever(userServiceClient.getAccessRules(anyString()))
                .thenReturn(
                    listOf(ExcludedContentPartnersAccessRule().apply {
                        name = "bad video types"
                        contentPartnerIds = listOf("content-partner-1")
                    })
                )

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsOnly(
                VideoAccessRule.ExcludedContentPartners(
                    contentPartnerIds = setOf(
                        ContentPartnerId(value = "content-partner-1")
                    )
                )
            )
        }

        @Test
        fun `can convert IncludedDistributionMethod types to domain`() {
            whenever(userServiceClient.getAccessRules(anyString()))
                .thenReturn(
                    listOf(IncludedDistributionMethodsAccessRule().apply {
                        name = "bad video types"
                        distributionMethods = listOf("STREAM", "DOWNLOAD")
                    })
                )
            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules
            assertThat(videoAccess.accessRules).containsOnly(
                VideoAccessRule.IncludedDistributionMethods(
                    distributionMethods = setOf(
                        DistributionMethod.STREAM,
                        DistributionMethod.DOWNLOAD
                    )
                )
            )
        }

        @Test
        fun `can convert all video access rule types to domain`() {
            val firstId = TestFactories.createVideoId()
            val secondId = TestFactories.createVideoId()
            whenever(userServiceClient.getAccessRules(anyString()))
                .thenReturn(listOf(
                    IncludedVideosAccessRule().apply {
                        name = "good videos"
                        videoIds = listOf(firstId.value)
                    },
                    ExcludedVideosAccessRule().apply {
                        name = "bad videos"
                        videoIds = listOf(secondId.value)
                    }
                )
                )
            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            val videoAccess = accessRules.videoAccess as VideoAccess.Rules

            assertThat(videoAccess.accessRules).containsExactlyInAnyOrder(
                VideoAccessRule.IncludedIds(setOf(firstId)),
                VideoAccessRule.ExcludedIds(setOf(secondId))
            )
        }
    }

    @Test
    fun `retries up to 3 times when client throws errors`() {
        val collectionsContract = IncludedCollectionsAccessRule().apply {
            name = "Test Contract"
            collectionIds = listOf("test-collection-id")
        }

        whenever(userServiceClient.getAccessRules(anyString()))
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
            .thenThrow(RuntimeException("Something bad happened"))
            .thenReturn(
                listOf(collectionsContract)
            )

        assertThat(accessRuleService.getRules(UserFactory.sample(id = "test-user")).collectionAccess).isEqualTo(
            CollectionAccessRule.SpecificIds(setOf(CollectionId("test-collection-id")))
        )
    }

    @Test
    fun `when rules cannot be obtained, revert to public access`() {
        whenever(userServiceClient.getAccessRules(anyString()))
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
            .thenThrow(RuntimeException("Something bad happened"))
            .thenThrow(RuntimeException("Something bad happened again!"))

        assertThat(accessRuleService.getRules(UserFactory.sample(id = "test-user")).collectionAccess).isEqualTo(
            CollectionAccessRule.public()
        )
    }

    @MockBean(name = "userServiceClient")
    override lateinit var userServiceClient: FakeUserServiceClient
}
