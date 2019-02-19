package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.AddVideoToCollectionCommand
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.fakes.FakeEventService
import com.boclips.videos.service.testsupport.setSecurityContext
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddVideoToCollectionTest {

    lateinit var collectionService: CollectionService

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
    }

    @Test
    fun `creates a collection if it doesn't exist`() {
        collectionService = mock()

        val addVideoToCollection = AddVideoToCollection(collectionService, mock())
        val collectionId = TestFactories.aValidId()
        val videoId = TestFactories.aValidId()

        addVideoToCollection(collectionId, videoId)

        argumentCaptor<AddVideoToCollectionCommand>().apply {
            verify(collectionService).update(eq(CollectionId(collectionId)), capture())
            assertThat(firstValue.videoId.value).isEqualTo(videoId)
        }
    }

    @Test
    fun `logs an event`() {
        collectionService = mock()
        val eventService = FakeEventService()
        val addVideoToCollection = AddVideoToCollection(collectionService, eventService)
        val collectionId = TestFactories.aValidId()
        val videoId = TestFactories.aValidId()

        addVideoToCollection(collectionId, videoId)

        assertThat(eventService.addToCollectionEvent().data.collectionId).isEqualTo(collectionId)
        assertThat(eventService.addToCollectionEvent().data.videoId).isEqualTo(videoId)
        assertThat(eventService.addToCollectionEvent().user.id).isEqualTo("me@me.com")
    }
}