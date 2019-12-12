package com.boclips.videos.service.infrastructure

import com.boclips.users.client.implementation.FakeUserServiceClient
import com.boclips.users.client.model.contract.SelectedCollectionsContract
import com.boclips.users.client.model.contract.SelectedVideosContract
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.common.UserId
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
        val collectionsContract = SelectedCollectionsContract().apply {
            name = "Test Contract"
            collectionIds = listOf("test-collection-id")
        }

        val videoId = ObjectId().toHexString()
        val videosContract = SelectedVideosContract().apply {
            name = "Test Contract"
            videoIds = listOf(videoId)
        }

        whenever(userServiceClient.getContracts(anyString()))
            .thenReturn(listOf(collectionsContract, videosContract))

        val accessRules = accessRuleService.getRules(UserFactory.sample(id = "test-user"))

        assertThat(accessRules.collectionAccess).isEqualTo(
            CollectionAccessRule.SpecificIds(setOf(CollectionId("test-collection-id")))
        )
        assertThat(accessRules.videoAccess).isEqualTo(VideoAccessRule.SpecificIds(videoIds = setOf(VideoId(videoId))))
    }

    @Nested
    inner class CollectionsAccess {
        @Test
        fun `has access specific user's collections when no contracts specified`() {
            whenever(userServiceClient.getContracts(anyString()))
                .thenReturn(emptyList())

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            assertThat(accessRules.collectionAccess).isEqualTo(CollectionAccessRule.SpecificOwner(owner = UserId(value = user.id.value)))
        }

        @Test
        fun `has access to everything if is allowed to view any collection`() {
            whenever(userServiceClient.getContracts(anyString()))
                .thenReturn(emptyList())

            val user = UserFactory.sample(id = "test-user", isPermittedToViewAnyCollection = true)
            val accessRules = accessRuleService.getRules(user)

            assertThat(accessRules.collectionAccess).isEqualTo(CollectionAccessRule.Everything)
        }
    }

    @Nested
    inner class VideoAccess {
        @Test
        fun `has access to everything when no contracts specified`() {
            whenever(userServiceClient.getContracts(anyString()))
                .thenReturn(emptyList())

            val user = UserFactory.sample(id = "test-user")
            val accessRules = accessRuleService.getRules(user)

            assertThat(accessRules.videoAccess).isEqualTo(VideoAccessRule.Everything)
        }
    }

    @Test
    fun `retries up to 3 times when client throws errors`() {
        val collectionsContract = SelectedCollectionsContract().apply {
            name = "Test Contract"
            collectionIds = listOf("test-collection-id")
        }

        whenever(userServiceClient.getContracts(anyString()))
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
            .thenThrow(RuntimeException("Something bad happened"))
            .thenReturn(listOf(collectionsContract))

        assertThat(accessRuleService.getRules(UserFactory.sample(id = "test-user")).collectionAccess).isEqualTo(
            CollectionAccessRule.SpecificIds(setOf(CollectionId("test-collection-id")))
        )
    }

    @Test
    fun `when rules cannot be obtained, revert to public access`() {
        whenever(userServiceClient.getContracts(anyString()))
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
