package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
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

        deleteVideo(videoId.value)

        assertThatThrownBy { videoService.getPlayableVideo(videoId) }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `requesting deletion with blank video ID throws an exception`() {
        assertThatThrownBy { deleteVideo("   ") }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `requesting deletion with null video ID throws an exception`() {
        assertThatThrownBy { deleteVideo(null) }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `remove deletes a video from repository`() {
        val videoId = saveVideo(title = "Some title", description = "test description 3")

        deleteVideo(videoId.value)

        assertThatThrownBy { videoService.getPlayableVideo(videoId) }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `remove deletes a video from search service`() {
        val videoId = saveVideo(title = "Some title", description = "test description 3")

        deleteVideo(videoId.value)

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
            title = "Some title",
            description = "test description 3",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-123")
        )

        deleteVideo(videoId.value)

        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-123")
        assertThat(kalturaPlaybackProvider.retrievePlayback(listOf(playbackId))).isEmpty()
    }

    @Test
    fun `remove deletes a video from collections`() {
        val videoId = saveVideo(
            title = "Some title",
            description = "test description 3",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-123")
        )

        val collectionId = saveCollection()

        collectionRepository.update(collectionId, CollectionUpdateCommand.AddVideoToCollection(videoId))

        deleteVideo(videoId.value)

        assertThat(collectionRepository.find(collectionId)!!.videos).doesNotContain(videoId)
    }
}
