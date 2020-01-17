package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.domain.service.collection.CollectionReadService
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
    lateinit var collectionReadService: CollectionReadService

    @Test
    fun `makes searchable if public`() {
        val createRequest = VideoServiceApiFactory.createCollectionRequest(
            title = "title",
            public = true
        )

        val collection = createCollection(createRequest, UserFactory.sample(id = "some@teacher.com"))

        val collections = collectionReadService.search(
            CollectionSearchQuery(
                text = "title",
                subjectIds = emptyList(),
                visibilityForOwners = setOf(
                    VisibilityForOwner(
                        owner = null,
                        visibility = CollectionVisibilityQuery.publicOnly()
                    )
                ),
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
            public = false
        )

        val collection = createCollection(createRequest, UserFactory.sample(id = "some@teacher.com"))

        val collections = collectionReadService.search(
            CollectionSearchQuery(
                text = "title",
                subjectIds = emptyList(),
                visibilityForOwners = setOf(
                    VisibilityForOwner(
                        owner = null,
                        visibility = CollectionVisibilityQuery.privateOnly()
                    )
                ),
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

        val collections = collectionReadService.search(
            CollectionSearchQuery(
                text = "title",
                subjectIds = emptyList(),
                visibilityForOwners = setOf(
                    VisibilityForOwner(
                        owner = null,
                        visibility = CollectionVisibilityQuery.privateOnly()
                    )
                ),
                pageSize = 1,
                pageIndex = 0,
                hasLessonPlans = null,
                permittedCollections = null
            ),
            user = UserFactory.sample()
        ).elements

        assertThat(collections.first().isPublic).isFalse()
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
