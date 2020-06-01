package com.boclips.videos.service.domain.service.video

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.infrastructure.collection.CollectionRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoDeletionServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoDeletionService: VideoDeletionService

    @Autowired
    lateinit var videoRetrievalService: VideoRetrievalService

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Test
    fun `requesting deletion of an existing video deletes the video`() {
        val videoId = saveVideo()

        videoDeletionService.delete(videoId, UserFactory.sample())

        Assertions.assertThatThrownBy { videoRetrievalService.getPlayableVideo(videoId, VideoAccess.Everything) }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `remove deletes a video from repository`() {
        val videoId = saveVideo(title = "Some title", description = "test description 3")

        videoDeletionService.delete(videoId, UserFactory.sample())

        Assertions.assertThatThrownBy { videoRetrievalService.getPlayableVideo(videoId, VideoAccess.Everything) }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `remove deletes a video from search service`() {
        val videoId = saveVideo(title = "Some title", description = "test description 3")

        videoDeletionService.delete(videoId, UserFactory.sample())

        Assertions.assertThat(
            videoIndexFake.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "Some title"
                    )
                )
            ).elements
        ).isEmpty()
    }

    @Test
    fun `remove deletes a video from Kaltura`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "id-123"),
            title = "Some title",
            description = "test description 3"
        )

        videoDeletionService.delete(videoId, UserFactory.sample())

        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "id-123")
        Assertions.assertThat(kalturaPlaybackProvider.retrievePlayback(listOf(playbackId))).isEmpty()
    }

    @Test
    fun `remove deletes a video from collections`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-123"),
            title = "Some title",
            description = "test description 3"
        )

        val collectionId = saveCollection()

        collectionRepository.update(
            CollectionUpdateCommand.AddVideoToCollection(
                collectionId = collectionId,
                videoId = videoId,
                user = UserFactory.sample()
            )
        )

        videoDeletionService.delete(videoId, UserFactory.sample())

        Assertions.assertThat(collectionRepository.find(collectionId)!!.videos).doesNotContain(videoId)
    }
}
