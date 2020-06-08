package com.boclips.videos.service.domain.service.collection

import com.boclips.eventbus.events.collection.VideoAddedToCollection
import com.boclips.eventbus.events.collection.VideoRemovedFromCollection
import com.boclips.videos.service.application.collection.exceptions.CollectionIllegalOperationException
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class CollectionUpdateServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var collectionUpdateService: CollectionUpdateService

    @Nested
    inner class AddVideo {
        @Test
        fun `adds a video to collection of legitimate user`() {
            val collectionId = saveCollection(owner = "me@me.com")
            val videoId = saveVideo()

            collectionUpdateService.addVideoToCollectionOfUser(
                collectionId,
                videoId,
                UserFactory.sample(id = "me@me.com")
            )

            val collection = collectionRepository.find(CollectionId(collectionId.value))!!
            assertThat(collection.videos.map { it.value }).containsExactly(videoId.value)
        }

        @Test
        fun `throws error when user doesn't own the collection`() {
            val collectionId = saveCollection(owner = "me@me.com")
            val videoId = saveVideo()

            assertThrows<CollectionIllegalOperationException> {
                collectionUpdateService.addVideoToCollectionOfUser(
                    collectionId,
                    videoId,
                    UserFactory.sample(id = "attacker@example.com")
                )
            }
        }

        @Test
        fun `logs an event`() {
            val collectionId = saveCollection(owner = "me@me.com")
            val videoId = saveVideo()

            collectionUpdateService.addVideoToCollectionOfUser(
                collectionId,
                videoId,
                UserFactory.sample(id = "me@me.com")
            )

            val event = fakeEventBus.getEventOfType(VideoAddedToCollection::class.java)

            assertThat(event.collectionId).isEqualTo(collectionId.value)
            assertThat(event.videoId).isEqualTo(videoId.value)
            assertThat(event.userId).isEqualTo("me@me.com")
        }
    }

    @Nested
    inner class RemoveVideo {
        @Test
        fun `removes the video using the collection service`() {
            val videoId = saveVideo()
            val collectionId = saveCollection(owner = "owner@collections.com", videos = listOf(videoId.value))

            assertThat(collectionRepository.find(collectionId)?.videos).isNotEmpty

            collectionUpdateService.removeVideoToCollectionOfUser(
                collectionId,
                videoId,
                UserFactory.sample(id = "owner@collections.com")
            )

            assertThat(collectionRepository.find(collectionId)?.videos).isEmpty()
        }

        @Test
        fun `logs an event`() {
            val videoId = saveVideo()
            val collectionId = saveCollection(owner = "owner@collection.com", videos = listOf(videoId.value))

            collectionUpdateService.removeVideoToCollectionOfUser(
                collectionId,
                videoId,
                UserFactory.sample(id = "owner@collection.com")
            )

            val event = fakeEventBus.getEventOfType(VideoRemovedFromCollection::class.java)

            assertThat(event.videoId).isEqualTo(videoId.value)
            assertThat(event.collectionId).isEqualTo(collectionId.value)
            assertThat(event.userId).isEqualTo("owner@collection.com")
        }

        @Test
        fun `throws an exception when user doesn't own the collection`() {
            val videoId = saveVideo()
            val collectionId = saveCollection(owner = "owner@collections.com", videos = listOf(videoId.value))

            assertThrows<CollectionIllegalOperationException> {
                collectionUpdateService.removeVideoToCollectionOfUser(
                    collectionId,
                    videoId,
                    UserFactory.sample(id = "attacker@example.com")
                )
            }
            assertThat(collectionRepository.find(collectionId)?.videos).isNotEmpty
        }
    }

    @Nested
    inner class ApplyUpdates() {
        @Test
        fun `saves update of legitimate collection owner`() {
            val user = UserFactory.sample()
            val collectionId = saveCollection(owner = user.id.value, discoverable = false)

            collectionUpdateService.updateCollectionAsOwner(
                listOf(
                    CollectionUpdateCommand.ChangeDiscoverability(
                        collectionId = collectionId,
                        discoverable = true,
                        user = user
                    )
                )
            )

            val collection = collectionRepository.find(collectionId)!!

            assertThat(collection.discoverable).isTrue()
        }

        @Test
        fun `doese not save updates of someone else's collection`() {
            val collectionId = saveCollection(owner = UserFactory.sample(id = "user").id.value, discoverable = false)

            assertThrows<CollectionIllegalOperationException> {
                collectionUpdateService.updateCollectionAsOwner(
                    listOf(
                        CollectionUpdateCommand.ChangeDiscoverability(
                            collectionId = collectionId,
                            discoverable = true,
                            user = UserFactory.sample(id = "another-user")
                        )
                    )
                )
            }
        }
    }
}
