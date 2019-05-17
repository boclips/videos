package com.boclips.videos.service.application.collection

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.presentation.collections.UpdateCollectionRequest
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class RemoveVideoFromCollectionTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var removeVideoFromCollection: RemoveVideoFromCollection

    @Test
    fun `removes the video using the collection service`() {
        val videoId = saveVideo()
        val collectionId = saveCollection(owner = "owner@collections.com", videos = listOf(videoId.value))

        assertThat(collectionRepository.getById(collectionId)?.videos).isNotEmpty

        removeVideoFromCollection(collectionId.value, videoId.value)

        assertThat(collectionRepository.getById(collectionId)?.videos).isEmpty()
    }

    @Test
    fun `logs an event`() {
        val videoId = saveVideo()
        val collectionId = saveCollection(owner = "owner@collection.com", videos = listOf(videoId.value))

        removeVideoFromCollection(collectionId.value, videoId.value)

        val message = messageCollector.forChannel(topics.videoRemovedFromCollection()).poll()

        assertThat(message).isNotNull
        assertThat(message.payload.toString()).contains(videoId.value)
        assertThat(message.payload.toString()).contains(collectionId.value)
        assertThat(message.payload.toString()).contains("owner@collection.com")
    }

    @Test
    fun `throws an exception when user doesn't own the collection`() {
        val videoId = saveVideo()
        val collectionId = saveCollection(owner = "owner@collections.com", videos = listOf(videoId.value))

        setSecurityContext("attacker@example.com")

        assertThrows<CollectionAccessNotAuthorizedException> {
            removeVideoFromCollection(collectionId.value, videoId.value)
        }
        assertThat(collectionRepository.getById(collectionId)?.videos).isNotEmpty
    }
}