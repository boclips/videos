package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.QueryValidationException
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GetVideosByQueryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var searchVideo: SearchVideo

    @Test
    fun `throws exception when query is null`() {
        assertThatThrownBy {
            searchVideo.byQuery(
                    query = null,
                    includeTags = emptyList(),
                    excludeTags = emptyList(),
                    pageSize = 2,
                    pageNumber = 0
            )
        }.isInstanceOf(QueryValidationException::class.java)
    }

    @Test
    fun `throws when page size too big`() {
        assertThatThrownBy {
            searchVideo.byQuery(query = "query", includeTags = emptyList(), excludeTags = emptyList(), pageSize = 1000, pageNumber = 0)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `throws when page size is too small`() {
        assertThatThrownBy {
            searchVideo.byQuery(query = "query", includeTags = emptyList(), excludeTags = emptyList(), pageSize = 0, pageNumber = 0)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `throws page index is smaller than 0`() {
        assertThatThrownBy {
            searchVideo.byQuery(query = "query", includeTags = emptyList(), excludeTags = emptyList(), pageSize = 0, pageNumber = -1)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `returns paginated results`() {
        saveVideo(title = "a youtube asset", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-1"))
        saveVideo(title = "a youtube asset", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-2"))
        saveVideo(title = "a another asset", playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "you-3"))
        saveVideo(title = "a youtube asset", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-4"))

        val result = searchVideo.byQuery(query = "youtube", includeTags = emptyList(), excludeTags = emptyList(), pageSize = 2, pageNumber = 1)

        assertThat(result.videos).hasSize(1)
        assertThat(result.totalVideos).isEqualTo(3)
        assertThat(result.pageNumber).isEqualTo(1)
        assertThat(result.pageSize).isEqualTo(2)
    }

    @Test
    fun `includes educational content when category is classroom`() {
        val videoId = saveVideo(title = "banana", typeId = LegacyVideoType.INSTRUCTIONAL_CLIPS.id)
        saveVideo(title = "banana", typeId = LegacyVideoType.STOCK.id)

        val videos = searchVideo.byQuery(query = "banana", includeTags = listOf("classroom"), excludeTags = emptyList(), pageSize = 2, pageNumber = 0)

        assertThat(videos.videos).hasSize(1)
        assertThat(videos.videos.first().id).isEqualTo(videoId.value)
    }

    @Test
    fun `shows only news when category is news`() {
        val newsVideoId = saveVideo(title = "banana", typeId = LegacyVideoType.NEWS.id)
        saveVideo(title = "banana", typeId = LegacyVideoType.INSTRUCTIONAL_CLIPS.id)

        val videos = searchVideo.byQuery(query = "banana", includeTags = listOf("news"), excludeTags = emptyList(), pageSize = 2, pageNumber = 0)

        assertThat(videos.videos).hasSize(1)
        assertThat(videos.videos.first().id).isEqualTo(newsVideoId.value)
    }

    @Test
    fun `returns videos with multiple categories`() {
        saveVideo(title = "banana", typeId = LegacyVideoType.STOCK.id)
        val newsAndClassroomVideoId = saveVideo(title = "banana", typeId = LegacyVideoType.NEWS.id)
        saveVideo(title = "banana", typeId = LegacyVideoType.INSTRUCTIONAL_CLIPS.id)

        val videos = searchVideo.byQuery(query = "banana", includeTags = listOf("news", "classroom"), excludeTags = emptyList(), pageSize = 2, pageNumber = 0)

        assertThat(videos.videos.map { it.id }).containsExactly(newsAndClassroomVideoId.value)
    }
}