package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.request.VideoRequest
import com.boclips.videos.service.domain.model.video.request.VideoRequestPagingState
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RetrievePlayableVideosTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var retrievePlayableVideos: RetrievePlayableVideos

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `retrieve videos by query returns Kaltura videos`() {
        saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1"),
            title = "a kaltura video"
        )

        val results = retrievePlayableVideos.searchPlayableVideos(
            VideoRequest(
                    text = "kaltura",
                    pageSize = 10,
                    pagingState = VideoRequestPagingState.PageNumber(0)
            ),
            VideoAccess.Everything
        )

        assertThat(results.videos).isNotEmpty
        assertThat(results.videos.first().title).isEqualTo("a kaltura video")
    }

    @Test
    fun `retrieve videos by query returns Youtube videos`() {
        saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"),
            title = "a youtube video"
        )

        val results = retrievePlayableVideos.searchPlayableVideos(
            VideoRequest(
                    text = "youtube",
                    pageSize = 10,
                    pagingState = VideoRequestPagingState.PageNumber(0)
            ),
            VideoAccess.Everything
        )

        assertThat(results.videos).isNotEmpty
        assertThat(results.videos.first().title).isEqualTo("a youtube video")
        assertThat((results.videos.first().playback as VideoPlayback.YoutubePlayback).thumbnailUrl).isNotBlank()
    }
}
