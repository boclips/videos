package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.CollectionService
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.fakes.FakeEventService
import com.boclips.videos.service.testsupport.setSecurityContext
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
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

        val eventService = FakeEventService()

        val removeVideoFromDefaultCollection = RemoveVideoFromDefaultCollection(collectionService, eventService)

        val videoId = TestFactories.aValidId()
        removeVideoFromDefaultCollection(videoId)

        assertThat(eventService.removeFromCollectionEvent().data.videoId).isEqualTo(videoId)
        assertThat(eventService.removeFromCollectionEvent().data.collectionId).isEqualTo("col id")
        assertThat(eventService.removeFromCollectionEvent().user.id).isEqualTo("me@me.com")
    }
}