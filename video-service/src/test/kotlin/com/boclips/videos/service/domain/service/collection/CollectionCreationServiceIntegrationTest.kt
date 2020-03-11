package com.boclips.videos.service.domain.service.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.AccessRulesFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CollectionCreationServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var collectionCreationService: CollectionCreationService

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Test
    fun `can create a collection`() {
        val createdCollection = collectionCreationService.create(
            CreateCollectionCommand(
                owner = UserId(value = "123"),
                title = "Wow, did you see that?",
                createdByBoclips = false,
                public = false,
                description = "A long long time ago, in a land far far far away",
                subjects = emptySet()
            ),
            videos = emptyList(),
            user = UserFactory.sample(id = "123")
        )!!

        val retrievedCollection = collectionRepository.find(createdCollection.id)!!

        assertThat(retrievedCollection.owner).isEqualTo(UserId(value = "123"))
        assertThat(retrievedCollection.title).isEqualTo("Wow, did you see that?")
        assertThat(retrievedCollection.createdByBoclips).isFalse()
        assertThat(retrievedCollection.isPublic).isFalse()
        assertThat(retrievedCollection.description).isEqualTo("A long long time ago, in a land far far far away")
        assertThat(retrievedCollection.subjects).isEmpty()

        assertThat(retrievedCollection).isEqualTo(createdCollection)
    }

    @Test
    fun `can create a collection with videos`() {
        val firstVideoId = saveVideo()
        val secondVideoId = saveVideo()
        val createdCollection = collectionCreationService.create(
            CreateCollectionCommand(
                owner = UserId(value = "123"),
                title = "Wow, did you see that?",
                createdByBoclips = false,
                public = false,
                description = "A long long time ago, in a land far far far away",
                subjects = emptySet()
            ),
            videos = listOf(firstVideoId, secondVideoId),
            user = UserFactory.sample(id = "123")
        )!!

        assertThat(createdCollection.videos).containsExactlyInAnyOrder(firstVideoId, secondVideoId)
    }

    @Test
    fun `returned collection does not contain videos that access rules do not permit`() {
        val videoId = TestFactories.createVideoId()
        val createdCollection = collectionCreationService.create(
            CreateCollectionCommand(
                owner = UserId(value = "123"),
                title = "Wow, did you see that?",
                createdByBoclips = false,
                public = false,
                description = "A long long time ago, in a land far far far away",
                subjects = emptySet()
            ),
            videos = listOf(videoId),
            user = UserFactory.sample(
                id = "123",
                accessRulesSupplier = {
                    AccessRulesFactory.sample(
                        videoAccess = VideoAccess.Rules(
                            listOf(
                                VideoAccessRule.IncludedIds(
                                    videoIds = emptySet()
                                )
                            )
                        )
                    )
                }
            )
        )!!

        assertThat(createdCollection.videos).isEmpty()
    }
}
