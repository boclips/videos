package com.boclips.videos.service.infrastructure.accessrules

import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ApiAccessRulesConverterTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var apiAccessRulesConverter: ApiAccessRulesConverter

    @Test
    fun `converts empty list to everything video access`() {
        val accessRules = apiAccessRulesConverter.toVideoAccess(emptyList())
        assertThat(accessRules).isEqualTo(VideoAccess.Everything)
    }

    @Test
    fun `converts empty list to everything collection access`() {
        val accessRules = apiAccessRulesConverter.toCollectionAccess(emptyList())
        assertThat(accessRules).isEqualTo(CollectionAccessRule.Everything)
    }

    @Test
    fun `converts access rules to video access`() {
        val firstVideoId = aValidId()
        val secondVideoId = aValidId()

        val accessRules = apiAccessRulesConverter.toVideoAccess(
            AccessRuleResource.IncludedVideos(
                name = "my-rule",
                videoIds = listOf(firstVideoId, secondVideoId)
            ).let(::listOf)
        )
        assertThat(accessRules).isEqualTo(
            VideoAccess.Rules(
                VideoAccessRule.IncludedIds(
                    setOf(
                        VideoId(firstVideoId),
                        VideoId(secondVideoId)
                    )
                ).let(::listOf)
            )
        )
    }

    @Test
    fun `converts access rules to everything collection access with superuser, even with collection ID restrictions`() {
        val accessRules = apiAccessRulesConverter.toCollectionAccess(
            listOf(
                AccessRuleResource.IncludedCollections(
                    name = "my rule",
                    collectionIds = listOf("collection-1", "collection-2")
                )
            ),
            UserFactory.sample(isPermittedToViewAnyCollection = true)
        )
        assertThat(accessRules).isEqualTo(CollectionAccessRule.Everything)
    }

    @Test
    fun `converts access rules to specific collection ID access without user`() {
        val accessRules = apiAccessRulesConverter.toCollectionAccess(
            listOf(
                AccessRuleResource.IncludedCollections(
                    name = "my rule",
                    collectionIds = listOf("collection-1", "collection-2")
                )
            )
        )
        assertThat(accessRules).isEqualTo(
            CollectionAccessRule.SpecificIds(
                setOf(
                    CollectionId("collection-1"),
                    CollectionId("collection-2")
                )
            )
        )
    }

    @Test
    fun `converts access rules to everything access with user and no access rules`() {
        val accessRules = apiAccessRulesConverter.toCollectionAccess(
            emptyList(),
            UserFactory.sample(id = "me")
        )
        assertThat(accessRules).isEqualTo(
            CollectionAccessRule.Everything
        )
    }

    @Test
    fun `converts access rules to specific collection ID access with user`() {
        val accessRules = apiAccessRulesConverter.toCollectionAccess(
            listOf(
                AccessRuleResource.IncludedCollections(
                    name = "my rule",
                    collectionIds = listOf("collection-1", "collection-2")
                )
            ),
            UserFactory.sample(id = "me")
        )
        assertThat(accessRules).isEqualTo(
            CollectionAccessRule.SpecificIds(
                setOf(
                    CollectionId("collection-1"),
                    CollectionId("collection-2")
                )
            )
        )
    }
}
