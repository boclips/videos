package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.domain.model.collection.UserId
import com.boclips.videos.service.domain.service.collection.CollectionRepository
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

        assertThat(collectionRepository.getById(collectionId)!!.bookmarks).containsExactly(UserId("me@me.com"))

        setSecurityContext("me@me.com")
        unbookmarkCollection(collectionId.value)

        assertThat(collectionRepository.getById(collectionId)!!.bookmarks).isEmpty()
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

        val message = messageCollector.forChannel(topics.collectionBookmarkChanged()).poll()

        assertThat(message).isNotNull
        assertThat(message.payload.toString()).contains(collectionId.value)
        assertThat(message.payload.toString()).contains("me@me.com")
        assertThat(message.payload.toString()).contains("false")
    }
}