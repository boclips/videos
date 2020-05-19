package com.boclips.videos.service.application.collection

import com.boclips.eventbus.events.collection.CollectionDescriptionChanged
import com.boclips.eventbus.events.collection.CollectionRenamed
import com.boclips.eventbus.events.collection.CollectionVideosBulkChanged
import com.boclips.eventbus.events.collection.CollectionVisibilityChanged
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.api.request.attachments.AttachmentRequest
import com.boclips.videos.api.request.collection.UpdateCollectionRequest
import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class UpdateCollectionIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Test
    fun `can promote a collection`() {
        setSecurityContext("boclipper@boclips.com", UserRoles.BACKOFFICE)

        val collectionId = saveCollection(owner = "me@me.com", title = "original title")

        updateCollection(
            collectionId.value,
            UpdateCollectionRequest(promoted = true),
            UserFactory.sample(id = "me@me.com")
        )

        assertThat(collectionRepository.find(collectionId)!!.promoted).isTrue()
    }

    @Test
    fun `rename collection`() {
        val collectionId = saveCollection(owner = "me@me.com", title = "original title")

        updateCollection(
            collectionId.value,
            UpdateCollectionRequest(title = "new title"),
            UserFactory.sample(id = "me@me.com")
        )

        assertThat(collectionRepository.find(collectionId)!!.title).isEqualTo("new title")
    }

    @Test
    fun `adds a lesson plan description and URL to a collection`() {
        val collectionId = saveCollection(owner = "me@me.com", title = "original title")

        updateCollection(
            collectionId = collectionId.value,
            updateCollectionRequest = UpdateCollectionRequest(
                attachment = AttachmentRequest(
                    linkToResource = "www.lesson-plan.com",
                    description = "my lesson plan description",
                    type = "LESSON_PLAN"
                )
            ),
            requester = UserFactory.sample(id = "me@me.com")
        )

        assertThat(collectionRepository.find(collectionId)!!.attachments.size).isEqualTo(1)
        assertThat(collectionRepository.find(collectionId)!!.attachments.first().linkToResource).isEqualTo("www.lesson-plan.com")
        assertThat(collectionRepository.find(collectionId)!!.attachments.first().description).isEqualTo("my lesson plan description")
        assertThat(collectionRepository.find(collectionId)!!.attachments.first().type).isEqualTo(AttachmentType.LESSON_PLAN)
    }

    @Test
    fun `logs an event when renaming`() {
        val collectionId = saveCollection(owner = "me@me.com", title = "original title")

        updateCollection(
            collectionId = collectionId.value,
            updateCollectionRequest = UpdateCollectionRequest(title = "new title"),
            requester = UserFactory.sample(id = "me@me.com")
        )

        val event = fakeEventBus.getEventOfType(CollectionRenamed::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.userId).isEqualTo("me@me.com")
        assertThat(event.collectionTitle).isEqualTo("new title")
    }

    @Test
    fun `logs an event when changing visibility`() {
        val collectionId = saveCollection(owner = "me@me.com", discoverable = false)

        updateCollection(
            collectionId = collectionId.value,
            updateCollectionRequest = UpdateCollectionRequest(discoverable = true),
            requester = UserFactory.sample(id = "me@me.com")
        )

        val event = fakeEventBus.getEventOfType(CollectionVisibilityChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.userId).isEqualTo("me@me.com")
        assertThat(event.isPublic).isTrue()
        assertThat(event.isDiscoverable).isTrue()
    }

    @Test
    fun `logs an event when changing description`() {
        val collectionId = saveCollection(owner = "me@me.com")

        updateCollection(
            collectionId = collectionId.value,
            updateCollectionRequest = UpdateCollectionRequest(description = "New Description"),
            requester = UserFactory.sample(id = "me@me.com")
        )

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
            collectionId = collectionId.value,
            updateCollectionRequest = UpdateCollectionRequest(videos = listOf(firstVideoId.value, secondVideoId.value)),
            requester = UserFactory.sample(id = "me@me.com")
        )

        val event = fakeEventBus.getEventOfType(CollectionVideosBulkChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.userId).isEqualTo("me@me.com")
        assertThat(event.videoIds).containsExactlyInAnyOrder(firstVideoId.value, secondVideoId.value)
    }

    @Test
    fun `throws when collection doesn't exist`() {
        val collectionId = aValidId()

        assertThrows<CollectionIllegalOperationException> {
            updateCollection(
                collectionId = collectionId,
                updateCollectionRequest = UpdateCollectionRequest(title = "new title"),
                requester = UserFactory.sample(id = "me@me.com")
            )
        }
    }
}
