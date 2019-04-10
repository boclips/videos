package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.SearchRequestValidationException
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
        }.isInstanceOf(SearchRequestValidationException::class.java)
    }

    @Test
    fun `throws when page size too big`() {
        assertThatThrownBy {
            searchVideo.byQuery(
                query = "query",
                includeTags = emptyList(),
                excludeTags = emptyList(),
                pageSize = 1000,
                pageNumber = 0
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `throws when page size is too small`() {
        assertThatThrownBy {
            searchVideo.byQuery(
                query = "query",
                includeTags = emptyList(),
                excludeTags = emptyList(),
                pageSize = 0,
                pageNumber = 0
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `throws page index is smaller than 0`() {
        assertThatThrownBy {
            searchVideo.byQuery(
                query = "query",
                includeTags = emptyList(),
                excludeTags = emptyList(),
                pageSize = 0,
                pageNumber = -1
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `returns paginated results`() {
        saveVideo(
            title = "a youtube asset",
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-1")
        )
        saveVideo(
            title = "a youtube asset",
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-2")
        )
        saveVideo(
            title = "a another asset",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "you-3")
        )
        saveVideo(
            title = "a youtube asset",
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-4")
        )

        val result = searchVideo.byQuery(
            query = "youtube",
            includeTags = emptyList(),
            excludeTags = emptyList(),
            pageSize = 2,
            pageNumber = 1
        )

        assertThat(result.videos).hasSize(1)
        assertThat(result.totalVideos).isEqualTo(3)
        assertThat(result.pageNumber).isEqualTo(1)
        assertThat(result.pageSize).isEqualTo(2)
    }

    @Test
    fun `includes educational content when category is classroom`() {
        val videoId = saveVideo(title = "banana", legacyType = LegacyVideoType.INSTRUCTIONAL_CLIPS)
        saveVideo(title = "banana", legacyType = LegacyVideoType.STOCK)
        saveVideo(title = "banana", contentProvider = "AP")

        val videos = searchVideo.byQuery(
            query = "banana",
            includeTags = listOf("classroom"),
            excludeTags = emptyList(),
            pageSize = 2,
            pageNumber = 0
        )

        assertThat(videos.videos).hasSize(1)
        assertThat(videos.videos.first().content.id).isEqualTo(videoId.value)
    }

    @Test
    fun `shows only news when category is news`() {
        val newsVideoId = saveVideo(title = "banana", legacyType = LegacyVideoType.NEWS)
        saveVideo(title = "banana", legacyType = LegacyVideoType.INSTRUCTIONAL_CLIPS)

        val videos = searchVideo.byQuery(
            query = "banana",
            includeTags = listOf("news"),
            excludeTags = emptyList(),
            pageSize = 2,
            pageNumber = 0
        )

        assertThat(videos.videos).hasSize(1)
        assertThat(videos.videos.first().content.id).isEqualTo(newsVideoId.value)
    }

    @Test
    fun `returns videos with multiple categories`() {
        saveVideo(title = "banana", legacyType = LegacyVideoType.STOCK)
        val newsAndClassroomVideoId = saveVideo(title = "banana", legacyType = LegacyVideoType.NEWS)
        saveVideo(title = "banana", legacyType = LegacyVideoType.INSTRUCTIONAL_CLIPS)
        saveVideo(title = "banana", legacyType = LegacyVideoType.NEWS, contentProvider = "AP")

        val videos = searchVideo.byQuery(
            query = "banana",
            includeTags = listOf("news", "classroom"),
            excludeTags = emptyList(),
            pageSize = 2,
            pageNumber = 0
        )

        assertThat(videos.videos.map { it.content.id }).containsExactly(newsAndClassroomVideoId.value)
    }

    @Test
    fun `saves a search event`() {
        saveVideo(title = "why are camels so tall 1")
        saveVideo(title = "why are camels so tall 2")
        saveVideo(title = "why are camels so tall 3")

        searchVideo.byQuery(
            query = "why are camels so tall",
            includeTags = emptyList(),
            excludeTags = emptyList(),
            pageSize = 2,
            pageNumber = 1
        )

        assertThat(analyticsEventService.searchEvent().data.query).isEqualTo("why are camels so tall")
        assertThat(analyticsEventService.searchEvent().data.pageIndex).isEqualTo(1)
        assertThat(analyticsEventService.searchEvent().data.pageSize).isEqualTo(2)
        assertThat(analyticsEventService.searchEvent().data.totalResults).isEqualTo(3L)
    }
}
