package com.boclips.videos.service.application.collection

import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.domain.service.collection.CollectionRetrievalService
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser

class CreateCollectionTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var collectionRetrievalService: CollectionRetrievalService

    @Test
    fun `makes searchable if curated`() {
        val createRequest = VideoServiceApiFactory.createCollectionRequest(
            title = "title",
            curated = true
        )

        val collection = createCollection(createRequest, UserFactory.sample(id = "some@teacher.com"))

        val collections = collectionRetrievalService.search(
            CollectionSearchQuery(
                text = "title",
                subjectIds = emptyList(),
                curated = null,
                pageSize = 1,
                pageIndex = 0,
                permittedCollections = null,
                hasLessonPlans = null
            ),
            user = UserFactory.sample()
        ).elements

        assertThat(collections).hasSize(1)
        assertThat(collections.first().id).isEqualTo(collection.id)
    }

    @Test
    fun `makes searchable if private`() {
        val createRequest = VideoServiceApiFactory.createCollectionRequest(
            title = "title",
            curated = false
        )

        val collection = createCollection(createRequest, UserFactory.sample(id = "some@teacher.com"))

        val collections = collectionRetrievalService.search(
            CollectionSearchQuery(
                text = "title",
                subjectIds = emptyList(),
                owner = null,
                curated = null,
                pageSize = 1,
                pageIndex = 0,
                hasLessonPlans = null,
                permittedCollections = null
            ),
            user = UserFactory.sample()
        ).elements

        assertThat(collections).hasSize(1)
        assertThat(collections.first().id).isEqualTo(collection.id)
    }

    @Test
    fun `collection is private by default`() {
        val createRequest = VideoServiceApiFactory.createCollectionRequest(
            title = "title"
        )

        createCollection(createRequest, UserFactory.sample(id = "some@teacher.com"))

        val collections = collectionRetrievalService.search(
            CollectionSearchQuery(
                text = "title",
                subjectIds = emptyList(),
                owner = null,
                curated = null,
                pageSize = 1,
                pageIndex = 0,
                hasLessonPlans = null,
                permittedCollections = null
            ),
            user = UserFactory.sample()
        ).elements

        assertThat(collections.first().isCurated).isFalse()
    }

    @Test
    @WithMockUser("user@boclips.com")
    fun `flags collections by Boclips employees`() {
        val createRequest = VideoServiceApiFactory.createCollectionRequest(title = "title", videos = emptyList())
        val collection =
            createCollection(createRequest, UserFactory.sample(id = "some@teacher.com", boclipsEmployee = true))

        assertThat(collection.createdByBoclips).isTrue()
    }

    @Test
    fun `allows to set a description`() {
        val description = "test description"
        val createRequest =
            VideoServiceApiFactory.createCollectionRequest(title = "test title", description = description)

        val collection = createCollection(createRequest, UserFactory.sample(id = "some@teacher.com"))

        assertThat(collection.description).isEqualTo(description)
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
