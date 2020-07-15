package com.boclips.videos.service.domain.service.video

import com.boclips.eventbus.events.video.CleanUpDeactivatedVideoRequested
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.collection.CreateCollectionCommand
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class VideoDuplicationServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoDuplicationService: VideoDuplicationService

    @Autowired
    lateinit var mongoVideoRepository: VideoRepository

    @Autowired
    lateinit var mongoCollectionRepository: CollectionRepository

    @Test
    fun `marks duplicate video as deactivated, save reference to new video`() {
        val oldVideo = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "The same video",
                deactivated = false,
                activeVideoId = null
            )
        )
        val newVideo = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "The same video",
                deactivated = false,
                activeVideoId = null
            )
        )

        videoDuplicationService.markDuplicate(oldVideo.videoId, newVideo.videoId, UserFactory.sample())

        val oldVideoAfter = mongoVideoRepository.find(oldVideo.videoId)
        val newVideoAfter = mongoVideoRepository.find(newVideo.videoId)

        assertThat(oldVideoAfter!!.deactivated).isTrue()
        assertThat(oldVideoAfter.activeVideoId).isEqualTo(newVideo.videoId)

        assertThat(newVideoAfter).isEqualTo(newVideo)
    }

    @Test
    fun `replaces deactivated video with its active substitute in collections`() {
        val oldVideo = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "The same video",
                deactivated = false,
                activeVideoId = null
            )
        )
        val newVideo = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "The same video",
                deactivated = false,
                activeVideoId = null
            )
        )
        val oldVideoCollection = mongoCollectionRepository.create(
            CreateCollectionCommand(
                title = "some collection",
                owner = UserId("some-user"),
                discoverable = false,
                description = "Some description",
                createdByBoclips = true
            )
        )
        mongoCollectionRepository.update(
            CollectionUpdateCommand.AddVideoToCollection(
                collectionId = oldVideoCollection.id,
                videoId = oldVideo.videoId,
                user = UserFactory.sample(id = "some-user")
            )
        )

        videoDuplicationService.markDuplicate(oldVideo.videoId, newVideo.videoId, UserFactory.sample())

        val updatedCollection = mongoCollectionRepository.find(oldVideoCollection.id)

        assertThat(updatedCollection!!.videos).containsOnly(newVideo.videoId)
    }

    @Test
    fun `cleans deactivated video when receiving CleanUpDeactivatedVideoRequested event`() {
        val newVideo = mongoVideoRepository.create(TestFactories.createVideo(title = "The same video"))
        val oldVideo = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "The same video",
                deactivated = true,
                activeVideoId = newVideo.videoId
            )
        )

        val collection = mongoCollectionRepository.create(
            CreateCollectionCommand(
                title = "some collection",
                owner = UserId("some-user"),
                discoverable = false,
                createdByBoclips = true
            )
        )

        val collectionWithDeactivatedVideo = mongoCollectionRepository.update(
            CollectionUpdateCommand.AddVideoToCollection(
                collectionId = collection.id,
                videoId = oldVideo.videoId,
                user = UserFactory.sample(id = "some-user")
            )
        )

        fakeEventBus.publish(CleanUpDeactivatedVideoRequested.builder().videoId(oldVideo.videoId.value).build())

        val updatedCollection = mongoCollectionRepository.find(collectionWithDeactivatedVideo.id)
        assertThat(collectionWithDeactivatedVideo!!.videos).containsOnly(oldVideo.videoId)
        assertThat(updatedCollection!!.videos).containsOnly(newVideo.videoId)
    }

    @Test
    fun `ignore deactivated videos without activeVideoId when cleanup`() {
        val deactivatedVideo = mongoVideoRepository.create(
            TestFactories.createVideo(
                title = "The same video",
                deactivated = true,
                activeVideoId = null
            )
        )

        val collection = mongoCollectionRepository.create(
            CreateCollectionCommand(
                title = "some collection",
                owner = UserId("some-user"),
                discoverable = false,
                createdByBoclips = true
            )
        )

        val collectionWithDeactivatedVideo = mongoCollectionRepository.update(
            CollectionUpdateCommand.AddVideoToCollection(
                collectionId = collection.id,
                videoId = deactivatedVideo.videoId,
                user = UserFactory.sample(id = "some-user")
            )
        )

        fakeEventBus.publish(CleanUpDeactivatedVideoRequested.builder().videoId(deactivatedVideo.videoId.value).build())

        val updatedCollection = mongoCollectionRepository.find(collectionWithDeactivatedVideo.id)
        assertThat(updatedCollection!!.videos).containsOnly(deactivatedVideo.videoId)
    }
}
