package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.RemoveVideoFromCollectionCommand
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

internal class RemoveVideoFromCollectionTest {

    @BeforeEach
    fun setUp() {
        setSecurityContext("me@me.com")
    }

    @Test
    fun `removes the video using the collection service`() {
        val collectionService = mock<CollectionService>()
        val eventService = FakeEventService()
        val removeVideoFromCollection = RemoveVideoFromCollection(collectionService, eventService)

        val videoId = TestFactories.aValidId()
        removeVideoFromCollection("col-id", videoId)

        argumentCaptor<RemoveVideoFromCollectionCommand>().apply {
            verify(collectionService).update(eq(CollectionId("col-id")), capture())
            assertThat(firstValue.videoId.value).isEqualTo(videoId)
        }
    }

    @Test
    fun `logs an event`() {
        val collectionService = mock<CollectionService>()
        val eventService = FakeEventService()
        val removeVideoFromCollection = RemoveVideoFromCollection(collectionService, eventService)

        val videoId = TestFactories.aValidId()
        removeVideoFromCollection("col-id", videoId)

        assertThat(eventService.removeFromCollectionEvent().data.videoId).isEqualTo(videoId)
        assertThat(eventService.removeFromCollectionEvent().data.collectionId).isEqualTo("col-id")
        assertThat(eventService.removeFromCollectionEvent().user.id).isEqualTo("me@me.com")
    }
}