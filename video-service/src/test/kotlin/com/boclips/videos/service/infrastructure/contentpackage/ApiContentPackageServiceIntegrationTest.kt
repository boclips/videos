package com.boclips.videos.service.infrastructure.contentpackage

import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.contentpackage.ContentPackageId
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ApiContentPackageServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contentPackageService: ApiContentPackageService

    @Test
    fun `returns contained access rules for content package`() {
        val firstVideoId = aValidId()
        val secondVideoId = aValidId()

        saveContentPackage(
            id = "package",
            name = "package",
            AccessRuleResource.IncludedCollections(
                id = "collection-rule",
                name = "collection rule",
                collectionIds = listOf("collection-1", "collection-2")
            ),
            AccessRuleResource.IncludedVideos(
                id = "video-rule",
                name = "video rule",
                videoIds = listOf(
                    firstVideoId,
                    secondVideoId
                )
            )
        )
        val accessRules = contentPackageService.getAccessRules(
            ContentPackageId("package")
        )
        assertNotNull(accessRules)

        val collectionAccess = accessRules?.collectionAccess as? CollectionAccessRule.SpecificIds
        assertThat(
            collectionAccess?.collectionIds?.map { it.value }
        ).containsExactlyInAnyOrder("collection-1", "collection-2")

        val videoAccess = (accessRules?.videoAccess as? VideoAccess.Rules)?.accessRules
        assertNotNull(videoAccess)
        val includedIds: List<String> = videoAccess!!
            .mapNotNull { rule ->
                (rule as? VideoAccessRule.IncludedIds)
                    ?.videoIds
                    ?.map { it.value }
            }
            .flatten()
        assertNotNull(includedIds)
        assertThat(includedIds).containsExactlyInAnyOrder(
            firstVideoId,
            secondVideoId
        )
    }

    @Test
    fun `returns null for content package that is not found`() {
        ContentPackageId("unknown-id")
            .let(contentPackageService::getAccessRules)
            .let(::assertNull)
    }
}
