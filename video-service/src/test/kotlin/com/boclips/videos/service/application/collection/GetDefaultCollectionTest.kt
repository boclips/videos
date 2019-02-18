package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.setSecurityContext
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetDefaultCollectionTest {

    lateinit var collectionService: CollectionService

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
    }

    @Test
    fun `collection is created for user who doesn't have one yet`() {
        val onCreateCollection = TestFactories.createCollection(
            owner = "me@me.com",
            title = "Freshly created",
            videos = emptyList()
        )

        collectionService = mock {
            on { getByOwner(UserId(value = "me@me.com")) } doReturn emptyList<Collection>()
            on { create(UserId(value = "me@me.com")) } doReturn(onCreateCollection)
        }

        val collection = GetDefaultCollection(collectionService, VideoToResourceConverter()).invoke()

        assertThat(collection.id).isEqualTo(onCreateCollection.id.value)
        assertThat(collection.owner).isEqualTo(onCreateCollection.owner.value)
        assertThat(collection.title).isEqualTo(onCreateCollection.title)
        assertThat(collection.videos).isEmpty()
    }

    @Test
    fun `user has a collection`() {
        collectionService = mock {
            on { getByOwner(UserId(value = "me@me.com")) } doReturn listOf(
                TestFactories.createCollection(
                    id = CollectionId("collection-id"),
                    owner = "me@me.com",
                    title = "collection title",
                    videos = listOf(createVideo())
                )
            )
        }
        val collection = GetDefaultCollection(collectionService, VideoToResourceConverter()).invoke()

        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.owner).isEqualTo("me@me.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
    }
}