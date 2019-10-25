package com.boclips.videos.service.application.collection

import com.boclips.eventbus.events.collection.CollectionBookmarkChanged
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
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

        assertThat(collectionRepository.find(collectionId)!!.bookmarks).containsExactly(UserId("me@me.com"))

        setSecurityContext("me@me.com")
        unbookmarkCollection(collectionId.value)

        assertThat(collectionRepository.find(collectionId)!!.bookmarks).isEmpty()
    }

    @Test
    fun `the bookmark gets deleted from search`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = true, bookmarkedBy = "me@me.com")

        assertThat(collectionRepository.find(collectionId)!!.bookmarks).containsExactly(UserId("me@me.com"))

        setSecurityContext("me@me.com")
        unbookmarkCollection(collectionId.value)

        assertThat(
            collectionSearchService.search(
                searchRequest = PaginatedSearchRequest(
                    query = CollectionQuery(
                        bookmarkedBy = "me@me.com"
                    )
                )
            )
        ).isEmpty()
    }

    @Test
    fun `throws when collection doesn't exist`() {
        setSecurityContext("me@me.com")
        assertThrows<CollectionNotFoundException> {
            unbookmarkCollection(
                collectionId = TestFactories.aValidId()
            )
        }
    }

    @Test
    fun `logs an event`() {
        val collectionId = saveCollection(owner = "owner@example.com", public = true, bookmarkedBy = "me@me.com")

        setSecurityContext("me@me.com")
        unbookmarkCollection(collectionId.value)

        val event = fakeEventBus.getEventOfType(CollectionBookmarkChanged::class.java)

        assertThat(event.collectionId).isEqualTo(collectionId.value)
        assertThat(event.userId).isEqualTo("me@me.com")
        assertThat(event.isBookmarked).isFalse()
    }
}
