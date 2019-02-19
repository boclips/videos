package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.CollectionResourceConverter
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.setSecurityContext
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetDefaultCollectionTest {

    lateinit var collectionService: CollectionService
    lateinit var collectionResourceConverter: CollectionResourceConverter

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
        collectionResourceConverter = CollectionResourceConverter(VideoToResourceConverter())
    }

    @Test
    fun `initial collection is created for user who doesn't have one yet`() {
        val onCreateCollection = TestFactories.createCollection(
            owner = "me@me.com",
            title = "Freshly created",
            videos = emptyList()
        )

        collectionService = mock {
            on { getByOwner(UserId(value = "me@me.com")) } doReturn emptyList<Collection>()
            on { create(eq(UserId(value = "me@me.com")), any()) } doReturn(onCreateCollection)
        }

        val collection = GetDefaultCollection(collectionService, collectionResourceConverter).invoke()

        assertThat(collection.id).isEqualTo(onCreateCollection.id.value)
        assertThat(collection.owner).isEqualTo(onCreateCollection.owner.value)
        assertThat(collection.title).isEqualTo(onCreateCollection.title)
        assertThat(collection.videos).isEmpty()
    }

    @Test
    fun `initial collection has a default title`() {
        collectionService = mock {
            on { getByOwner(UserId(value = "me@me.com")) } doReturn emptyList<Collection>()
            on { create(eq(UserId(value = "me@me.com")), any()) } doReturn(TestFactories.createCollection())
        }

        GetDefaultCollection(collectionService, collectionResourceConverter).invoke()

        verify(collectionService).create(any(), eq("My Videos"))
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
        val collection = GetDefaultCollection(collectionService, collectionResourceConverter).invoke()

        assertThat(collection.id).isEqualTo("collection-id")
        assertThat(collection.owner).isEqualTo("me@me.com")
        assertThat(collection.title).isEqualTo("collection title")
        assertThat(collection.videos).hasSize(1)
    }
}