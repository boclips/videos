package com.boclips.videos.service.application.collection

import com.boclips.eventbus.events.collection.CollectionBookmarkChanged
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class BookmarkCollectionTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Test
    fun `collection bookmarks are saved`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = true)

        setSecurityContext("me@me.com")
        bookmarkCollection(collectionId.value)

        val collection = collectionRepository.find(collectionId)
        assertThat(collection).isNotNull
        assertThat(collection!!.bookmarks).containsExactly(UserId("me@me.com"))
    }

    @Test
    fun `throws error when user owns the collection`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = true)

        assertThrows<CollectionIllegalOperationException> {
            bookmarkCollection(collectionId.value)
        }

        val collection = collectionRepository.find(collectionId)
        assertThat(collection!!.bookmarks).isEmpty()
    }

    @Test
    fun `throws error when collection is not public`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = false)

        setSecurityContext("me@me.com")
        assertThrows<CollectionAccessNotAuthorizedException> {
            bookmarkCollection(collectionId.value)
        }

        val collection = collectionRepository.find(collectionId)
        assertThat(collection!!.bookmarks).isEmpty()
    }

    @Test
    fun `throws when collection doesn't exist`() {
        setSecurityContext("me@me.com")
        assertThrows<CollectionNotFoundException> {
            bookmarkCollection(
                collectionId = TestFactories.aValidId()
            )
        }
    }

    @Test
    fun `logs an event`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = true)

        setSecurityContext("someone@else.com")
        bookmarkCollection(collectionId.value)

        val event = fakeEventBus.getEventOfType(CollectionBookmarkChanged::class.java)
        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.user.id).isEqualTo("someone@else.com")
        assertThat(event.isBookmarked).isTrue()
    }
}
