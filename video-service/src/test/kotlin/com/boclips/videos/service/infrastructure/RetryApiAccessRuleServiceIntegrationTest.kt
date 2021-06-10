package com.boclips.videos.service.infrastructure

import com.boclips.users.api.factories.AccessRulesResourceFactory
import com.boclips.users.api.httpclient.test.fakes.UsersClientFake
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
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

        whenever(usersClient.getAccessRulesOfUser(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
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
    fun `when rules cannot be obtained, revert to public access and protect private channels`() {
        val privateChannelId = saveChannel(private = true).id.value
        whenever(usersClient.getAccessRulesOfUser(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
            .thenThrow(HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
            .thenThrow(RuntimeException("Something bad happened"))
            .thenThrow(RuntimeException("Something bad happened again!"))

        val rules = accessRuleService.getRules(UserFactory.sample(id = "test-user"))

        Assertions.assertThat(rules.collectionAccess)
            .isEqualTo(
                CollectionAccessRule.everything()
            )
        Assertions.assertThat(rules.videoAccess)
            .isEqualTo(
                VideoAccess.Everything(setOf(ChannelId(privateChannelId)))
            )
    }
}
