package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
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
        val contentPartner = saveContentPartner()

        val videoId1 = createVideo(
            TestFactories.createCreateVideoRequest(
                providerVideoId = "hurray",
                title = "a-video",
                playbackId = "hiphip",
                providerId = contentPartner.contentPartnerId.value
            )
        ).content.id

        val videoId2 = createVideo(
            TestFactories.createCreateVideoRequest(
                title = "another-video",
                providerId = contentPartner.contentPartnerId.value
            )
        ).content.id

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

        val collections = collectionService.search(CollectionSearchQuery(
            text = "title",
            subjectIds = emptyList(),
            visibility = listOf(CollectionVisibility.PUBLIC),
            pageSize = 1,
            pageIndex = 0
        )).elements

        assertThat(collections).hasSize(1)
        assertThat(collections.first().id).isEqualTo(collection.id)
    }

    @Test
    fun `makes searchable if private`() {
        val createRequest = TestFactories.createCollectionRequest(
            title = "title",
            public = false
        )

        val collection = createCollection(createRequest)

        val collections = collectionService.search(CollectionSearchQuery(
            text = "title",
            subjectIds = emptyList(),
            visibility = listOf(CollectionVisibility.PRIVATE),
            pageSize = 1,
            pageIndex = 0
        )).elements

        assertThat(collections).hasSize(1)
        assertThat(collections.first().id).isEqualTo(collection.id)
    }

    @Test
    fun `collection is private by default`() {
        val createRequest = TestFactories.createCollectionRequest(
            title = "title"
        )

        createCollection(createRequest)

        val collections = collectionService.search(CollectionSearchQuery(
            text = "title",
            subjectIds = emptyList(),
            visibility = listOf(CollectionVisibility.PRIVATE),
            pageSize = 1,
            pageIndex = 0
        )).elements

        assertThat(collections.first().isPublic).isFalse()
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
        createMediaEntry(
            id = "123"
        )
        createMediaEntry(
            id = "hiphip"
        )
    }
}
