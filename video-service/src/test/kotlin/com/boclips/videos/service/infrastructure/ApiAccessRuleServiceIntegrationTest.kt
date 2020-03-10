package com.boclips.videos.service.infrastructure

import com.boclips.users.client.implementation.FakeUserServiceClient
import com.boclips.users.client.model.accessrule.ContentPackage
import com.boclips.users.client.model.accessrule.IncludedCollectionsAccessRule
import com.boclips.users.client.model.accessrule.IncludedVideosAccessRule
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
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

        whenever(userServiceClient.getContentPackage(anyString()))
            .thenReturn(ContentPackage().apply {
                name = "blah"
                accessRules = listOf(collectionsContract, videosContract)
            })

        val accessRules = accessRuleService.getRules(UserFactory.sample(id = "test-user"))

        assertThat(accessRules.collectionAccess).isEqualTo(
            CollectionAccessRule.SpecificIds(setOf(CollectionId("test-collection-id")))
        )
        assertThat((accessRules.videoAccess as VideoAccess.Rules).accessRules).containsExactly(
            VideoAccessRule.SpecificIds(
                videoIds = setOf(VideoId(videoId))
            )
        )
    }

    @Test
    fun `returns default when content package cannot be found`() {
        whenever(userServiceClient.getContentPackage(anyString()))
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
            whenever(userServiceClient.getContentPackage(anyString()))
                .thenReturn(
                    ContentPackage().apply {
                        name = "blah"
                        accessRules = emptyList()
                    })

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
            whenever(userServiceClient.getContentPackage(anyString()))
                .thenReturn(
                    ContentPackage().apply {
                        name = "blah"
                        accessRules = emptyList()
                    })

            val user = UserFactory.sample(id = "test-user", isPermittedToViewAnyCollection = true)
            val accessRules = accessRuleService.getRules(user)

            assertThat(accessRules.collectionAccess).isEqualTo(CollectionAccessRule.Everything)
        }
    }

    @Nested
    inner class AccessingVideos {
        @Test
        fun `has access to everything when no contracts specified`() {
            whenever(userServiceClient.getContentPackage(anyString()))
                .thenReturn(ContentPackage().apply {
                    name = "blah"
                    accessRules = emptyList()
                })

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            assertThat(accessRules.videoAccess).isEqualTo(VideoAccess.Everything)
        }
    }

    @Test
    fun `retries up to 3 times when client throws errors`() {
        val collectionsContract = IncludedCollectionsAccessRule().apply {
            name = "Test Contract"
            collectionIds = listOf("test-collection-id")
        }

        whenever(userServiceClient.getContentPackage(anyString()))
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
            .thenThrow(RuntimeException("Something bad happened"))
            .thenReturn(ContentPackage().apply {
                name = "blah"
                accessRules = listOf(collectionsContract)
            })

        assertThat(accessRuleService.getRules(UserFactory.sample(id = "test-user")).collectionAccess).isEqualTo(
            CollectionAccessRule.SpecificIds(setOf(CollectionId("test-collection-id")))
        )
    }

    @Test
    fun `when rules cannot be obtained, revert to public access`() {
        whenever(userServiceClient.getContentPackage(anyString()))
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
