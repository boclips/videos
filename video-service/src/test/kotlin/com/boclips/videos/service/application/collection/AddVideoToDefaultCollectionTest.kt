package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.CollectionService
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.types.AddToCollectionEvent
import com.boclips.videos.service.infrastructure.event.types.AddToCollectionEventData
import com.boclips.videos.service.infrastructure.event.types.Event
import com.boclips.videos.service.testsupport.TestFactories.createCollection
import com.boclips.videos.service.testsupport.setSecurityContext
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.asn1.x500.style.RFC4519Style.owner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.internal.verification.Times

class AddVideoToDefaultCollectionTest {

    lateinit var collectionService: CollectionService

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
    }

    @Test
    fun `creates a collection if it doesn't exist`() {
        collectionService = mock {
            on { getByOwner(UserId(value = "me@me.com")) } doReturn emptyList<Collection>()
        }

        val addVideoToCollection = AddVideoToDefaultCollection(collectionService, mock())

        try {
            addVideoToCollection.execute("123")
        } catch (e: Exception) {
            // fails after creating a collection because of the mock
        }

        verify(collectionService).create(UserId(value = "me@me.com"))
    }

    @Test
    fun `does not create a collection if it already exist`() {
        collectionService = mock {
            on { getByOwner(UserId(value = "me@me.com")) } doReturn listOf(createCollection(owner = "me@me.com"))
        }

        val addVideoToCollection = AddVideoToDefaultCollection(collectionService, mock())

        addVideoToCollection.execute("123")

        verify(collectionService, Times(0)).create(UserId(value = "me@me.com"))
    }

    @Test
    fun `logs an event`() {
        val collection = createCollection(id = CollectionId("col id"), owner = "me@me.com")
        collectionService = mock {
            on { getByOwner(UserId(value = "me@me.com")) } doReturn listOf(collection)
        }

        val eventService = mock<EventService>()

        val addVideoToCollection = AddVideoToDefaultCollection(collectionService, eventService)

        addVideoToCollection.execute("123")

        verify(eventService).saveEvent(check<Event<AddToCollectionEventData>> {
            assertThat(it.data.videoId).isEqualTo("123")
            assertThat(it.data.collectionId).isEqualTo("col id")
            assertThat(it.user.id).isEqualTo("me@me.com")
        })
    }
}