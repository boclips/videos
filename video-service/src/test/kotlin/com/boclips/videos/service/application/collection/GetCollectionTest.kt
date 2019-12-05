package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.application.collection.exceptions.CollectionAccessNotAuthorizedException
import com.boclips.videos.service.domain.model.collection.CollectionNotFoundException
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class GetCollectionTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getCollection: GetCollection

    @Test
    fun `finding collection by ID with the list projection`() {
        setSecurityContext("me@me.com")

        val savedVideoId = saveVideo()
        val savedCollectionId = saveCollection(
            owner = "me@me.com",
            title = "Freshly found",
            videos = listOf(savedVideoId.value)
        )

        val retrievedCollection = getCollection.invoke(savedCollectionId.value, Projection.list)

        assertThat(retrievedCollection.id).isEqualTo(savedCollectionId.value)
        assertThat(retrievedCollection.videos).isNotEmpty
        assertThat(retrievedCollection.videos.component1().content.id).isNotBlank()
        assertThat(retrievedCollection.videos.component1().content.title).isNull()
        assertThat(retrievedCollection.videos.component1().content.description).isNull()
        assertThat(retrievedCollection.videos.component1().content.playback).isNull()
    }

    @Test
    fun `finding collection by ID with the details projection`() {
        setSecurityContext("me@me.com")

        val savedVideoId = saveVideo()
        val savedCollectionId = saveCollection(
            owner = "me@me.com",
            title = "Freshly found",
            videos = listOf(savedVideoId.value)
        )

        val retrievedCollection = getCollection.invoke(savedCollectionId.value, Projection.details)

        assertThat(retrievedCollection.id).isEqualTo(savedCollectionId.value)
        assertThat(retrievedCollection.videos).isNotEmpty
        assertThat(retrievedCollection.videos.component1().content.id).isNotBlank()
        assertThat(retrievedCollection.videos.component1().content.title).isNotBlank()
        assertThat(retrievedCollection.videos.component1().content.description).isNotBlank()
        assertThat(retrievedCollection.videos.component1().content.playback).isNotNull
    }

    @Test
    fun `propagates CollectionNotFound error thrown from downstream`() {
        assertThrows<CollectionNotFoundException> { getCollection(collectionId = "123") }
    }

    @Test
    fun `propagates the CollectionAccessNotAuthorizedException error thrown from downstream`() {
        setSecurityContext("attacker@example.com")

        val savedCollectionId = saveCollection(
            owner = "me@me.com",
            public = false
        )

        assertThrows<CollectionAccessNotAuthorizedException> { getCollection(savedCollectionId.value) }
    }
}
