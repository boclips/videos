package com.boclips.videos.service.application.collection

import com.boclips.eventbus.events.collection.CollectionBookmarkChanged
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class UnbookmarkCollectionTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var unbookmarkCollection: UnbookmarkCollection

    @Test
    fun `the bookmark gets deleted`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = true, bookmarkedBy = "me@me.com")

        assertThat(collectionRepository.find(collectionId)!!.bookmarks).containsExactly(
            UserId(
                "me@me.com"
            )
        )

        unbookmarkCollection(collectionId.value, UserFactory.sample(id = "me@me.com"))

        assertThat(collectionRepository.find(collectionId)!!.bookmarks).isEmpty()
    }

    @Test
    fun `the bookmark gets deleted from search`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = true, bookmarkedBy = "me@me.com")

        assertThat(collectionRepository.find(collectionId)!!.bookmarks).containsExactly(
            UserId(
                "me@me.com"
            )
        )

        unbookmarkCollection(collectionId.value, UserFactory.sample(id = "me@me.com"))

        val results = collectionSearchService.search(
            searchRequest = PaginatedSearchRequest(
                query = CollectionQuery(
                    bookmarkedBy = "me@me.com"
                )
            )
        )
        assertThat(results.elements).isEmpty()
    }

    @Test
    fun `throws when collection doesn't exist`() {
        assertThrows<CollectionNotFoundException> {
            unbookmarkCollection(
                collectionId = TestFactories.aValidId(),
                user = UserFactory.sample(id = "me@me.com")
            )
        }
    }

    @Test
    fun `throws error when user owns the collection`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = true)

        assertThrows<CollectionIllegalOperationException> {
            unbookmarkCollection(collectionId.value, UserFactory.sample(id = "owner@example.com"))
        }

        val collection = collectionRepository.find(collectionId)
        assertThat(collection!!.bookmarks).isEmpty()
    }

    @Test
    fun `throws error when collection is not public`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = false)

        assertThrows<CollectionNotFoundException> {
            unbookmarkCollection(collectionId.value, UserFactory.sample(id = "another-owner@example.com"))
        }

        val collection = collectionRepository.find(collectionId)
        assertThat(collection!!.bookmarks).isEmpty()
    }

    @Test
    fun `logs an event`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = true, bookmarkedBy = "me@me.com")

        unbookmarkCollection(collectionId.value, UserFactory.sample(id = "me@me.com"))

        val event = fakeEventBus.getEventOfType(CollectionBookmarkChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.userId).isEqualTo("me@me.com")
        assertThat(event.isBookmarked).isFalse()
    }
}
