package com.boclips.videos.service.application.collection

import com.boclips.eventbus.events.collection.VideoRemovedFromCollection
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class RemoveVideoFromCollectionTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var removeVideoFromCollection: RemoveVideoFromCollection

    @Test
    fun `removes the video using the collection service`() {
        val videoId = saveVideo()
        val collectionId = saveCollection(owner = "owner@collections.com", videos = listOf(videoId.value))

        assertThat(collectionRepository.find(collectionId)?.videos).isNotEmpty

        removeVideoFromCollection(collectionId.value, videoId.value, UserFactory.sample(id = "owner@collections.com"))

        assertThat(collectionRepository.find(collectionId)?.videos).isEmpty()
    }

    @Test
    fun `logs an event`() {
        val videoId = saveVideo()
        val collectionId = saveCollection(owner = "owner@collection.com", videos = listOf(videoId.value))

        removeVideoFromCollection(collectionId.value, videoId.value, UserFactory.sample(id = "owner@collection.com"))

        val event = fakeEventBus.getEventOfType(VideoRemovedFromCollection::class.java)

        assertThat(event.videoId).isEqualTo(videoId.value)
        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.userId).isEqualTo("owner@collection.com")
    }

    @Test
    fun `throws an exception when user doesn't own the collection`() {
        val videoId = saveVideo()
        val collectionId = saveCollection(owner = "owner@collections.com", videos = listOf(videoId.value))

        assertThrows<CollectionNotFoundException> {
            removeVideoFromCollection(
                collectionId.value,
                videoId.value,
                UserFactory.sample(id = "attacker@example.com")
            )
        }
        assertThat(collectionRepository.find(collectionId)?.videos).isNotEmpty
    }
}
