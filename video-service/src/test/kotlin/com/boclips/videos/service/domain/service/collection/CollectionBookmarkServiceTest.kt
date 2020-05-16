package com.boclips.videos.service.domain.service.collection

import com.boclips.eventbus.events.collection.CollectionBookmarkChanged
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class CollectionBookmarkServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var collectionBookmarkService: CollectionBookmarkService

    @Nested
    inner class Bookmark {
        @Test
        fun `collection bookmarks are saved`() {
            val collectionId = saveCollection(owner = "owner@example.com", curated = true)

            collectionBookmarkService.bookmark(collectionId, UserFactory.sample(id = "me@me.com"))

            val collection = collectionRepository.find(collectionId)
            Assertions.assertThat(collection).isNotNull
            Assertions.assertThat(collection!!.bookmarks).containsExactly(
                UserId(
                    "me@me.com"
                )
            )
        }

        @Test
        fun `collection bookmarks are updated for search`() {
            val collectionId = saveCollection(owner = "owner@example.com", curated = true)

            collectionBookmarkService.bookmark(collectionId, UserFactory.sample(id = "me@me.com"))

            val results = collectionIndex.search(
                searchRequest = PaginatedSearchRequest(
                    query = CollectionQuery(
                        owner = null,
                        searchable = null,
                        bookmarkedBy = "me@me.com"
                    )
                )
            )

            Assertions.assertThat(results.elements).containsExactly(collectionId.value)
        }

        @Test
        fun `throws error when user owns the collection`() {
            val collectionId = saveCollection(owner = "owner@example.com", curated = true)

            assertThrows<CollectionIllegalOperationException> {
                collectionBookmarkService.bookmark(collectionId, UserFactory.sample(id = "owner@example.com"))
            }

            val collection = collectionRepository.find(collectionId)
            Assertions.assertThat(collection!!.bookmarks).isEmpty()
        }

        @Test
        fun `throws when collection doesn't exist`() {
            assertThrows<CollectionNotFoundException> {
                collectionBookmarkService.bookmark(
                    collectionId = CollectionId(TestFactories.aValidId()),
                    user = UserFactory.sample(id = "me@me.com")
                )
            }
        }

        @Test
        fun `logs an event`() {
            val collectionId = saveCollection(owner = "owner@example.com", curated = true)

            collectionBookmarkService.bookmark(collectionId, UserFactory.sample(id = "someone@else.com"))

            val event = fakeEventBus.getEventOfType(CollectionBookmarkChanged::class.java)
            Assertions.assertThat(event.collectionId).isEqualTo(collectionId.value)
            Assertions.assertThat(event.isBookmarked).isTrue()
            Assertions.assertThat(event.userId).isEqualTo("someone@else.com")
        }
    }

    @Nested
    inner class Unbookmark {
        @Test
        fun `the bookmark gets deleted`() {
            val collectionId = saveCollection(owner = "owner@example.com", bookmarkedBy = "me@me.com")

            Assertions.assertThat(collectionRepository.find(collectionId)!!.bookmarks).containsExactly(
                UserId(
                    "me@me.com"
                )
            )

            collectionBookmarkService.unbookmark(collectionId, UserFactory.sample(id = "me@me.com"))

            Assertions.assertThat(collectionRepository.find(collectionId)!!.bookmarks).isEmpty()
        }

        @Test
        fun `the bookmark gets deleted from search`() {
            val collectionId = saveCollection(owner = "owner@example.com", bookmarkedBy = "me@me.com")

            Assertions.assertThat(collectionRepository.find(collectionId)!!.bookmarks).containsExactly(
                UserId("me@me.com")
            )

            collectionBookmarkService.unbookmark(collectionId, UserFactory.sample(id = "me@me.com"))

            val results = collectionIndex.search(
                searchRequest = PaginatedSearchRequest(
                    query = CollectionQuery(bookmarkedBy = "me@me.com")
                )
            )
            Assertions.assertThat(results.elements).isEmpty()
        }

        @Test
        fun `throws when collection doesn't exist`() {
            assertThrows<CollectionNotFoundException> {
                collectionBookmarkService.unbookmark(
                    collectionId = CollectionId(TestFactories.aValidId()),
                    user = UserFactory.sample(id = "me@me.com")
                )
            }
        }

        @Test
        fun `throws error when user owns the collection`() {
            val collectionId = saveCollection(owner = "owner@example.com", curated = true)

            assertThrows<CollectionIllegalOperationException> {
                collectionBookmarkService.unbookmark(collectionId, UserFactory.sample(id = "owner@example.com"))
            }

            val collection = collectionRepository.find(collectionId)
            Assertions.assertThat(collection!!.bookmarks).isEmpty()
        }

        @Test
        fun `logs an event`() {
            val collectionId = saveCollection(owner = "owner@example.com", curated = true, bookmarkedBy = "me@me.com")

            collectionBookmarkService.unbookmark(collectionId, UserFactory.sample(id = "me@me.com"))

            val event = fakeEventBus.getEventOfType(CollectionBookmarkChanged::class.java)

            Assertions.assertThat(event.collectionId).isEqualTo(collectionId.value)
            Assertions.assertThat(event.userId).isEqualTo("me@me.com")
            Assertions.assertThat(event.isBookmarked).isFalse()
        }
    }
}
