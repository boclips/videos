package com.boclips.videos.service.application.collection

import com.boclips.eventbus.events.collection.CollectionDescriptionChanged
import com.boclips.eventbus.events.collection.CollectionRenamed
import com.boclips.eventbus.events.collection.CollectionVideosBulkChanged
import com.boclips.eventbus.events.collection.CollectionVisibilityChanged
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class UpdateCollectionIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var collectionService: CollectionService

    @Test
    fun `rename collection`() {
        val collectionId = saveCollection(owner = "me@me.com", title = "original title")

        updateCollection(collectionId.value, UpdateCollectionRequest(title = "new title"))

        assertThat(collectionRepository.find(collectionId)!!.title).isEqualTo("new title")
    }

    @Test
    fun `logs an event when renaming`() {
        val collectionId = saveCollection(owner = "me@me.com", title = "original title")

        updateCollection(collectionId.value, UpdateCollectionRequest(title = "new title"))

        val event = fakeEventBus.getEventOfType(CollectionRenamed::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.userId).isEqualTo("me@me.com")
        assertThat(event.collectionTitle).isEqualTo("new title")
    }

    @Test
    fun `logs an event when changing visibility`() {
        val collectionId = saveCollection(owner = "me@me.com", public = false)

        updateCollection(collectionId.value, UpdateCollectionRequest(isPublic = true))

        val event = fakeEventBus.getEventOfType(CollectionVisibilityChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.userId).isEqualTo("me@me.com")
        assertThat(event.isPublic).isTrue()
    }

    @Test
    fun `logs an event when changing description`() {
        val collectionId = saveCollection(owner = "me@me.com")

        updateCollection(collectionId.value, UpdateCollectionRequest(description = "New Description"))

        val event = fakeEventBus.getEventOfType(CollectionDescriptionChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.userId).isEqualTo("me@me.com")
        assertThat(event.description).isEqualTo("New Description")
    }

    @Test
    fun `logs an event when bulk updating videos`() {
        val firstVideoId = saveVideo(title = "first")
        val secondVideoId = saveVideo(title = "second")

        val collectionId = saveCollection(owner = "me@me.com")

        updateCollection(
            collectionId.value,
            UpdateCollectionRequest(videos = listOf(firstVideoId.value, secondVideoId.value))
        )

        val event = fakeEventBus.getEventOfType(CollectionVideosBulkChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.userId).isEqualTo("me@me.com")
        assertThat(event.videoIds).containsExactlyInAnyOrder(firstVideoId.value, secondVideoId.value)
    }

    @Test
    fun `throws error when user doesn't own the collection`() {
        val collectionId = saveCollection(owner = "me@me.com", title = "original title")

        setSecurityContext("attacker@example.com")

        assertThrows<CollectionAccessNotAuthorizedException> {
            updateCollection(collectionId.value, UpdateCollectionRequest(title = "you have been pwned"))
        }
        assertThat(collectionRepository.find(collectionId)!!.title).isEqualTo("original title")
    }

    @Test
    fun `throws when collection doesn't exist`() {
        val collectionId = aValidId()

        assertThrows<CollectionNotFoundException> {
            updateCollection(
                collectionId = collectionId,
                updateCollectionRequest = UpdateCollectionRequest(title = "new title")
            )
        }
    }
}
