package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DeleteVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var deleteVideo: DeleteVideo

    @Autowired
    lateinit var videoService: VideoService

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Test
    fun `requesting deletion of an existing video deletes the video`() {
        val videoId = saveVideo()

        deleteVideo(videoId.value, UserFactory.sample())

        assertThatThrownBy { videoService.getPlayableVideo(videoId, VideoAccessRule.Everything) }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `requesting deletion with blank video ID throws an exception`() {
        assertThatThrownBy { deleteVideo("   ", UserFactory.sample()) }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `requesting deletion with null video ID throws an exception`() {
        assertThatThrownBy { deleteVideo(null, UserFactory.sample()) }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `remove deletes a video from repository`() {
        val videoId = saveVideo(title = "Some title", description = "test description 3")

        deleteVideo(videoId.value, UserFactory.sample())

        assertThatThrownBy { videoService.getPlayableVideo(videoId, VideoAccessRule.Everything) }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `remove deletes a video from search service`() {
        val videoId = saveVideo(title = "Some title", description = "test description 3")

        deleteVideo(videoId.value, UserFactory.sample())

        assertThat(
            videoSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "Some title"
                    )
                )
            )
        ).isEmpty()
    }

    @Test
    fun `remove deletes a video from Kaltura`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "id-123"),
            title = "Some title",
            description = "test description 3"
        )

        deleteVideo(videoId.value, UserFactory.sample())

        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "id-123")
        assertThat(kalturaPlaybackProvider.retrievePlayback(listOf(playbackId))).isEmpty()
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

        deleteVideo(videoId.value, UserFactory.sample())

        assertThat(collectionRepository.find(collectionId)!!.videos).doesNotContain(videoId)
    }
}
