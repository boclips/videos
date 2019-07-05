package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.CollectionSearchQuery
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser

class CreateCollectionTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var collectionService: CollectionService

    @Test
    @WithMockUser("this-user")
    fun `creates collection and associates videos`() {
        val videoId1 = createVideo(
            TestFactories.createCreateVideoRequest(
                providerVideoId = "hurray",
                title = "a-video",
                playbackId = "hiphip"
            )
        ).content.id
        val videoId2 = createVideo(TestFactories.createCreateVideoRequest(title = "another-video")).content.id
        val createRequest = TestFactories.createCollectionRequest(
            title = "title",
            videos = listOf("http://localhost/v1/videos/$videoId1", "http://localhost/v1/videos/$videoId2")
        )

        val collection = createCollection(createRequest)
        assertThat(collection.title).isEqualTo("title")
        assertThat(collection.owner.value).isEqualTo("this-user")
        assertThat(collection.videos).hasSize(2)
        assertThat(collection.videos.first().value).isEqualTo(videoId1)
        assertThat(collection.createdByBoclips).isFalse()

        val allCollections = collectionRepository.getByOwner(
            UserId("this-user"),
            PageRequest(0, 10)
        ).elements
        assertThat(allCollections).contains(collection)
    }

    @Test
    fun `makes searchable if public`() {
        val createRequest = TestFactories.createCollectionRequest(
            title = "title",
            public = true
        )

        val collection = createCollection(createRequest)

        assertThat(collectionService.search(CollectionSearchQuery("title", emptyList(), 1, 0)).elements).isNotEmpty
        assertThat(collectionService.search(CollectionSearchQuery("title", emptyList(), 1, 0)).elements.first().id).isEqualTo(collection.id)
    }

    @Test
    fun `collection is private by default`() {
        val createRequest = TestFactories.createCollectionRequest(
            title = "title"
        )

        createCollection(createRequest)

        assertThat(collectionService.search(CollectionSearchQuery("title", emptyList(), 1, 0)).elements).isEmpty()
    }

    @Test
    @WithMockUser("user@boclips.com")
    fun `flags collections by Boclips employees`() {
        val createRequest = TestFactories.createCollectionRequest(title = "title", videos = emptyList())
        val collection = createCollection(createRequest)

        assertThat(collection.createdByBoclips).isTrue()
    }

    @Test
    fun `throws when missing title`() {
        val createRequest = TestFactories.createCollectionRequest(
            title = null,
            videos = listOf("http://localhost/v1/videos/a-video")
        )

        assertThrows<NonNullableFieldCreateRequestException> { createCollection(createRequest) }
    }

    @BeforeEach
    fun setUp() {
        fakeKalturaClient.addMediaEntry(
            TestFactories.createMediaEntry(
                referenceId = "123"
            )
        )
        fakeKalturaClient.addMediaEntry(
            TestFactories.createMediaEntry(
                referenceId = "hiphip"
            )
        )
    }
}
