package com.boclips.videos.service.application.collection

import com.boclips.eventbus.events.collection.CollectionBookmarkChanged
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
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

        bookmarkCollection(collectionId.value, UserFactory.sample(id = "me@me.com"))

        val collection = collectionRepository.find(collectionId)
        assertThat(collection).isNotNull
        assertThat(collection!!.bookmarks).containsExactly(
            UserId(
                "me@me.com"
            )
        )
    }

    @Test
    fun `collection bookmarks are updated for search`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = true)

        bookmarkCollection(collectionId.value, UserFactory.sample(id = "me@me.com"))

        val results = collectionSearchService.search(
            searchRequest = PaginatedSearchRequest(
                query = CollectionQuery(
                    visibilityForOwners = setOf(VisibilityForOwner(null, CollectionVisibilityQuery.publicOnly())),
                    bookmarkedBy = "me@me.com"
                )
            )
        )

        assertThat(results.elements).containsExactly(collectionId.value)
    }

    @Test
    fun `throws error when user owns the collection`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = true)

        assertThrows<CollectionIllegalOperationException> {
            bookmarkCollection(collectionId.value, UserFactory.sample(id = "owner@example.com"))
        }

        val collection = collectionRepository.find(collectionId)
        assertThat(collection!!.bookmarks).isEmpty()
    }

    @Test
    fun `throws error when collection is not public`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = false)

        assertThrows<CollectionNotFoundException> {
            bookmarkCollection(collectionId.value, UserFactory.sample(id = "me@me.com"))
        }

        val collection = collectionRepository.find(collectionId)
        assertThat(collection!!.bookmarks).isEmpty()
    }

    @Test
    fun `throws when collection doesn't exist`() {
        assertThrows<CollectionNotFoundException> {
            bookmarkCollection(
                collectionId = TestFactories.aValidId(),
                user = UserFactory.sample(id = "me@me.com")
            )
        }
    }

    @Test
    fun `logs an event`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = true)

        bookmarkCollection(collectionId.value, UserFactory.sample(id = "someone@else.com"))

        val event = fakeEventBus.getEventOfType(CollectionBookmarkChanged::class.java)
        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.isBookmarked).isTrue()
        assertThat(event.userId).isEqualTo("someone@else.com")
    }
}
