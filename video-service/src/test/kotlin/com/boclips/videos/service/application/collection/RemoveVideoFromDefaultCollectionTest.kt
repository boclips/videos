package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.CollectionService
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.types.AddToCollectionEventData
import com.boclips.videos.service.infrastructure.event.types.Event
import com.boclips.videos.service.infrastructure.event.types.RemoveFromCollectionEventData
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.setSecurityContext
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RemoveVideoFromDefaultCollectionTest {

    lateinit var collectionService: CollectionService

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
    }

    @Test
    fun `logs an event`() {
        val collection = TestFactories.createCollection(id = CollectionId("col id"), owner = "me@me.com")
        collectionService = mock {
            on { getByOwner(UserId(value = "me@me.com")) } doReturn listOf(collection)
        }

        val eventService = mock<EventService>()

        val addVideoToCollection = RemoveVideoFromDefaultCollection(collectionService, eventService)

        addVideoToCollection.execute("123")

        verify(eventService).saveEvent(com.nhaarman.mockito_kotlin.check<Event<RemoveFromCollectionEventData>> {
            Assertions.assertThat(it.data.videoId).isEqualTo("123")
            Assertions.assertThat(it.data.collectionId).isEqualTo("col id")
            Assertions.assertThat(it.user.id).isEqualTo("me@me.com")
        })
    }


}