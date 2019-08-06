package com.boclips.videos.service.application.collection

import com.boclips.eventbus.events.collection.CollectionDescriptionChanged
import com.boclips.eventbus.events.collection.CollectionRenamed
import com.boclips.eventbus.events.collection.CollectionVisibilityChanged
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
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
        assertThat(event.user.id).isEqualTo("me@me.com")
        assertThat(event.collectionTitle).isEqualTo("new title")
    }

    @Test
    fun `logs an event when changing visibility`() {
        val collectionId = saveCollection(owner = "me@me.com", public = false)

        updateCollection(collectionId.value, UpdateCollectionRequest(isPublic = true))

        val event = fakeEventBus.getEventOfType(CollectionVisibilityChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.user.id).isEqualTo("me@me.com")
        assertThat(event.isPublic).isTrue()
    }

    @Test
    fun `logs an event when changing description`() {
        val collectionId = saveCollection(owner = "me@me.com")

        updateCollection(collectionId.value, UpdateCollectionRequest(description = "New Description"))

        val event = fakeEventBus.getEventOfType(CollectionDescriptionChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.user.id).isEqualTo("me@me.com")
        assertThat(event.description).isEqualTo("New Description")
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

    @Test
    fun `makes searchable if public`() {
        val collectionId = saveCollection(owner = "me@me.com", title = "title")

        updateCollection(collectionId.value, UpdateCollectionRequest(isPublic = false))
        updateCollection(collectionId.value, UpdateCollectionRequest(isPublic = true))

        assertThat(
            collectionService.search(
                CollectionSearchQuery(
                    "title",
                    emptyList(),
                    1,
                    0
                )
            ).elements.first().id
        ).isEqualTo(collectionId)
    }

    @Test
    fun `removes from search if not public`() {
        val collectionId = saveCollection(owner = "me@me.com", title = "title")

        updateCollection(collectionId.value, UpdateCollectionRequest(isPublic = true))
        updateCollection(collectionId.value, UpdateCollectionRequest(isPublic = false))

        assertThat(collectionService.search(
            CollectionSearchQuery(
                "title",
                emptyList(),
                1,
                0
            )
        ).elements).isEmpty()
    }
}
