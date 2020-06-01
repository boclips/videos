package com.boclips.videos.service.domain.service.collection

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.collection.CreateDefaultCollectionCommand
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
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
                discoverable = false,
                description = "A long long time ago, in a land far far far away",
                subjects = emptySet()
            ),
            videos = emptyList(),
            user = UserFactory.sample(id = "123")
        )!!

        val retrievedCollection = collectionRepository.find(createdCollection.id)!!

        assertThat(retrievedCollection.owner).isEqualTo(
            UserId(
                value = "123"
            )
        )
        assertThat(retrievedCollection.title).isEqualTo("Wow, did you see that?")
        assertThat(retrievedCollection.createdByBoclips).isFalse()
        assertThat(retrievedCollection.discoverable).isFalse()
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
                discoverable = false,
                description = "A long long time ago, in a land far far far away",
                subjects = emptySet()
            ),
            videos = listOf(firstVideoId, secondVideoId),
            user = UserFactory.sample(id = "123")
        )!!

        assertThat(createdCollection.videos).containsExactlyInAnyOrder(firstVideoId, secondVideoId)
    }

    @Test
    fun `can create a default collection`() {
        val rawUserId = "some_Id"
        val owner = UserId(rawUserId)
        val createDefaultCollectionCommand = CreateDefaultCollectionCommand(owner)

        val result = collectionCreationService.create(createDefaultCollectionCommand)

        assertThat(result).isNotNull
        assertThat(result.owner).isEqualTo(owner)
        assertThat(result.title).isEqualTo(CreateDefaultCollectionCommand.TITLE)

        val indexedCollection = collectionIndex.search(
            searchRequest = PaginatedSearchRequest(query = CollectionQuery(phrase = "Watch later"))
        )
        assertThat(indexedCollection.elements.size).isEqualTo(1)
    }

    @Test
    fun `returned collection does not contain videos that access rules do not permit`() {
        val videoId = TestFactories.createVideoId()
        val createdCollection = collectionCreationService.create(
            CreateCollectionCommand(
                owner = UserId(value = "123"),
                title = "Wow, did you see that?",
                createdByBoclips = false,
                discoverable = false,
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
