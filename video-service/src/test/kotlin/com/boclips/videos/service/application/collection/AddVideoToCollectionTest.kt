package com.boclips.videos.service.application.collection

import com.boclips.eventbus.events.collection.VideoAddedToCollection
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class AddVideoToCollectionTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var addVideoToCollection: AddVideoToCollection

    @Test
    fun `adds a video to collection`() {
        val collectionId = saveCollection(owner = "me@me.com")
        val videoId = saveVideo()

        addVideoToCollection(collectionId.value, videoId.value)

        val collection = collectionRepository.find(CollectionId(collectionId.value))!!
        assertThat(collection.videos.map { it.value }).containsExactly(videoId.value)
    }

    @Test
    fun `throws error when user doesn't own the collection`() {
        val collectionId = saveCollection(owner = "me@me.com")
        val videoId = saveVideo()

        setSecurityContext("attacker@example.com")

        assertThrows<CollectionAccessNotAuthorizedException> {
            addVideoToCollection(collectionId.value, videoId.value)
        }
    }

    @Test
    fun `logs an event`() {
        val collectionId = saveCollection(owner = "me@me.com")
        val videoId = saveVideo()

        addVideoToCollection(collectionId.value, videoId.value)

        val event = fakeEventBus.getEventOfType(VideoAddedToCollection::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.videoId).isEqualTo(videoId.value)
        assertThat(event.user.id).isEqualTo("me@me.com")
    }
}
