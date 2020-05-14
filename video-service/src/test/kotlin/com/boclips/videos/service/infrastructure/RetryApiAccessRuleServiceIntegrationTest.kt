package com.boclips.videos.service.infrastructure

import com.boclips.users.api.factories.AccessRulesResourceFactory
import com.boclips.users.api.httpclient.test.fakes.UsersClientFake
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class RetryApiAccessRuleServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @MockBean(name = "usersClient")
    override lateinit var usersClient: UsersClientFake

    @Test
    fun `retries up to 3 times when client throws errors`() {
        val collectionsContract = AccessRuleResource.IncludedCollections(
            name = "Test Contract",
            collectionIds = listOf("test-collection-id")
        )

        whenever(usersClient.getAccessRulesOfUser(ArgumentMatchers.anyString()))
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
            .thenThrow(RuntimeException("Something bad happened"))
            .thenReturn(
                AccessRulesResourceFactory.sample(collectionsContract)
            )

        Assertions.assertThat(accessRuleService.getRules(UserFactory.sample(id = "test-user")).collectionAccess)
            .isEqualTo(
                CollectionAccessRule.SpecificIds(setOf(CollectionId("test-collection-id")))
            )
    }

    @Test
    fun `when rules cannot be obtained, revert to public access`() {
        whenever(usersClient.getAccessRulesOfUser(ArgumentMatchers.anyString()))
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
            .thenThrow(RuntimeException("Something bad happened"))
            .thenThrow(RuntimeException("Something bad happened again!"))

        Assertions.assertThat(accessRuleService.getRules(UserFactory.sample(id = "test-user")).collectionAccess)
            .isEqualTo(
                CollectionAccessRule.everything()
            )
    }
}
