package com.boclips.videos.service.application.video.search

import com.boclips.eventbus.events.video.VideosSearched
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class GetVideosByVideoQueryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var searchVideo: SearchVideo

    @Test
    fun `can search for empty query`() {
        saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-1"),
            title = "a youtube video"
        )

        val result = searchVideo.byQuery(
            query = null,
            includeTags = emptyList(),
            excludeTags = emptyList(),
            pageSize = 2,
            pageNumber = 1
        )

        assertThat(result.pageInfo.totalElements).isEqualTo(1)
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
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-1"),
            title = "a youtube video"
        )
        saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-2"),
            title = "a youtube video"
        )
        saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "you-3"),
            title = "a another video"
        )
        saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-4"),
            title = "a youtube video"
        )

        val result = searchVideo.byQuery(
            query = "youtube",
            includeTags = emptyList(),
            excludeTags = emptyList(),
            pageSize = 2,
            pageNumber = 1
        )

        assertThat(result.elements).hasSize(1)
        assertThat(result.pageInfo.totalElements).isEqualTo(3)
    }

    @Test
    fun `includes educational content when category is classroom`() {
        val videoId = saveVideo(title = "banana", legacyType = LegacyVideoType.INSTRUCTIONAL_CLIPS)
        saveVideo(title = "banana", legacyType = LegacyVideoType.STOCK)

        val videos = searchVideo.byQuery(
            query = "banana",
            includeTags = listOf("classroom"),
            excludeTags = emptyList(),
            pageSize = 2,
            pageNumber = 0
        )

        assertThat(videos.elements).hasSize(1)
        assertThat(videos.elements.first().videoId.value).isEqualTo(videoId.value)
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

        assertThat(videos.elements).hasSize(1)
        assertThat(videos.elements.first().videoId.value).isEqualTo(newsVideoId.value)
    }

    @Test
    fun `returns videos with multiple categories`() {
        saveVideo(title = "banana", legacyType = LegacyVideoType.STOCK)
        val newsAndClassroomVideoId = saveVideo(title = "banana", legacyType = LegacyVideoType.NEWS)
        saveVideo(title = "banana", legacyType = LegacyVideoType.INSTRUCTIONAL_CLIPS)

        val videos = searchVideo.byQuery(
            query = "banana",
            includeTags = listOf("news", "classroom"),
            excludeTags = emptyList(),
            pageSize = 2,
            pageNumber = 0
        )

        assertThat(videos.elements.map { it.videoId.value }).containsExactly(newsAndClassroomVideoId.value)
    }

    @Test
    fun `publishes a VIDEOS_SEARCHED event`() {
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

        val event = fakeEventBus.getEventOfType(VideosSearched::class.java)
        assertThat(event.query).isEqualTo("why are camels so tall")
    }

    @Test
    fun `can filter by duration between 0 and 10 seconds`() {
        saveVideo(title = "why are camels so tall 1", duration = Duration.ofSeconds(5))
        saveVideo(title = "why are camels so tall 2", duration = Duration.ofSeconds(10))
        saveVideo(title = "why are camels so tall 3", duration = Duration.ofSeconds(15))

        val videos = searchVideo.byQuery(
            query = "why are camels so tall",
            includeTags = emptyList(),
            excludeTags = emptyList(),
            minDuration = "PT0S",
            maxDuration = "PT10S",
            pageSize = 20,
            pageNumber = 0
        )

        assertThat(videos.pageInfo.totalElements).isEqualTo(2)
        assertThat(videos.elements.toList().size).isEqualTo(2)
        assertThat(videos.elements.first().title).isEqualTo("why are camels so tall 1")
        assertThat(videos.elements.elementAt(1).title).isEqualTo("why are camels so tall 2")
    }

    @Test
    fun `can filter by source`() {
        saveVideo(
            playbackId = PlaybackId(value = "1233", type = PlaybackProviderType.KALTURA),
            title = "why are camels so tall 1"
        )
        saveVideo(
            playbackId = PlaybackId(value = "1234", type = PlaybackProviderType.YOUTUBE),
            title = "why are camels so tall 2"
        )

        val results = searchVideo.byQuery(
            query = "why are camels so tall",
            includeTags = emptyList(),
            excludeTags = emptyList(),
            pageSize = 20,
            pageNumber = 0,
            source = "youtube"
        )

        assertThat(results.pageInfo.totalElements).isEqualTo(1)
        assertThat(results.elements.toList().size).isEqualTo(1)
        assertThat(results.elements.first().title).isEqualTo("why are camels so tall 2")
    }
}
