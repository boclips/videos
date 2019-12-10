package com.boclips.videos.service.infrastructure

import com.boclips.users.client.implementation.FakeUserServiceClient
import com.boclips.users.client.model.contract.SelectedCollectionsContract
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.CollectionAccessRule
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class ApiAccessRuleServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Test
    fun `passes through client's response if all is well`() {
        whenever(userServiceClient.getContracts(anyString()))
            .thenReturn(listOf(testContract))

        assertThat(accessRuleService.getRules(UserFactory.sample(id = "test-user")).collectionAccess).isEqualTo(
            CollectionAccessRule.SpecificIds(setOf(CollectionId("test-collection-id")))
        )
    }

    @Test
    fun `retries up to 3 times when client throws errors`() {
        whenever(userServiceClient.getContracts(anyString()))
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
            .thenThrow(RuntimeException("Something bad happened"))
            .thenReturn(listOf(testContract))

        assertThat(accessRuleService.getRules(UserFactory.sample(id = "test-user")).collectionAccess).isEqualTo(
            CollectionAccessRule.SpecificIds(setOf(CollectionId("test-collection-id")))
        )
    }

    val testContract = SelectedCollectionsContract().apply {
        name = "Test Contract"
        collectionIds = listOf("test-collection-id")
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
