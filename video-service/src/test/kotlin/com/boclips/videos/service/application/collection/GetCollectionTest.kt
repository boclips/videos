package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.presentation.projections.Projection
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class GetCollectionTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getCollection: GetCollection

    @Test
    fun `finding collection by ID with the list projection`() {
        val savedVideoId = saveVideo()
        val savedCollectionId = saveCollection(
            owner = "me@me.com",
            title = "Freshly found",
            videos = listOf(savedVideoId.value)
        )

        val retrievedCollection =
            getCollection.invoke(savedCollectionId.value, Projection.list, UserFactory.sample(id = "me@me.com"))

        assertThat(retrievedCollection.id).isEqualTo(savedCollectionId.value)
        assertThat(retrievedCollection.videos).isNotEmpty
        assertThat(retrievedCollection.videos.component1().content.id).isNotBlank()
        assertThat(retrievedCollection.videos.component1().content.title).isNull()
        assertThat(retrievedCollection.videos.component1().content.description).isNull()
        assertThat(retrievedCollection.videos.component1().content.playback).isNull()
    }

    @Test
    fun `finding collection by ID with the details projection`() {
        val savedVideoId = saveVideo()
        val savedCollectionId = saveCollection(
            owner = "me@me.com",
            title = "Freshly found",
            videos = listOf(savedVideoId.value)
        )

        val retrievedCollection =
            getCollection.invoke(savedCollectionId.value, Projection.details, UserFactory.sample(id = "me@me.com"))

        assertThat(retrievedCollection.id).isEqualTo(savedCollectionId.value)
        assertThat(retrievedCollection.videos).isNotEmpty
        assertThat(retrievedCollection.videos.component1().content.id).isNotBlank()
        assertThat(retrievedCollection.videos.component1().content.title).isNotBlank()
        assertThat(retrievedCollection.videos.component1().content.description).isNotBlank()
        assertThat(retrievedCollection.videos.component1().content.playback).isNotNull
    }

    @Test
    fun `propagates CollectionNotFound error thrown from downstream`() {
        assertThrows<CollectionNotFoundException> {
            getCollection(
                collectionId = "123",
                user = UserFactory.sample()
            )
        }
    }

    @Test
    fun `propagates the CollectionNotFoundException when access of collection is not permitted`() {
        val savedCollectionId = saveCollection(
            owner = "me@me.com",
            public = false
        )

        assertThrows<CollectionNotFoundException> {
            getCollection(
                savedCollectionId.value,
                user = UserFactory.sample(id = "attacker@example.com")
            )
        }
    }
}
