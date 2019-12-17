package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.AccessRulesFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class CollectionServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var collectionService: CollectionService

    @Test
    fun `can find a collection by ID`() {
        val videoId = TestFactories.createVideoId()
        val collectionId = saveCollection(videos = listOf(videoId.value), public = true)
        val collection = collectionService.find(
            collectionId,
            UserFactory.sample()
        )!!
        assertThat(collection.id).isEqualTo(collectionId)
        assertThat(collection.videos).containsExactly(videoId)
    }

    @Test
    fun `can find a collection by ID with only permitted videos included`() {
        val firstPermittedId = TestFactories.createVideoId()
        val secondPermittedId = TestFactories.createVideoId()

        val nonPermittedId = TestFactories.createVideoId()

        val collectionId = saveCollection(
            videos = listOf(
                firstPermittedId.value,
                nonPermittedId.value
            )
        )

        val collection = collectionService.find(
            collectionId, UserFactory.sample(accessRulesSupplier = {
                AccessRulesFactory.sample(
                    videoAccessRule = VideoAccessRule.SpecificIds(
                        videoIds = setOf(
                            firstPermittedId,
                            secondPermittedId
                        )
                    )
                )
            })
        )!!

        assertThat(collection.id).isEqualTo(collectionId)
        assertThat(collection.videos).containsExactly(firstPermittedId)
    }

    @Test
    fun `cannot find missing collection by ID`() {
        assertThat(
            collectionService.find(
                CollectionId("nonexistent"),
                UserFactory.sample()
            )
        ).isNull()
    }

    @Test
    fun `cannot find collection that access rules do not permit`() {
        val collectionId = saveCollection()
        assertThat(
            collectionService.find(
                collectionId, UserFactory.sample(accessRulesSupplier = {
                    AccessRulesFactory.sample(
                        collectionAccessRule = CollectionAccessRule.specificIds(
                            emptyList()
                        )
                    )
                })
            )
        ).isNull()
    }

    @Test
    fun `can find collection that we have write access to`() {
        val collectionId = saveCollection()
        val collection = collectionService.findWritable(
            collectionId,
            UserFactory.sample(isPermittedToViewAnyCollection = true)
        )!!
        assertThat(collection.id).isEqualTo(collectionId)
    }

    @Test
    fun `cannot find collection if we don't have write access to`() {
        val collectionId = saveCollection()
        val collection = collectionService.findWritable(
            collectionId,
            UserFactory.sample(isPermittedToViewAnyCollection = false)
        )

        assertThat(collection).isNull()
    }
}