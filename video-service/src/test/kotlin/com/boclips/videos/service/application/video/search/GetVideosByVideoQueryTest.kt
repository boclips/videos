package com.boclips.videos.service.application.video.search

import com.boclips.eventbus.events.video.VideosSearched
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
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
            pageSize = 2,
            pageNumber = 1,
            user = UserFactory.sample()
        )

        assertThat(result.pageInfo.totalElements).isEqualTo(1)
    }

    @Test
    fun `throws when page size too big`() {
        assertThatThrownBy {
            searchVideo.byQuery(
                query = "query",
                pageSize = 1000,
                pageNumber = 0,
                user = UserFactory.sample()
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `throws when page size is too small`() {
        assertThatThrownBy {
            searchVideo.byQuery(
                query = "query",
                pageSize = 0,
                pageNumber = 0,
                user = UserFactory.sample()
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `throws page index is smaller than 0`() {
        assertThatThrownBy {
            searchVideo.byQuery(
                query = "query",
                pageSize = 0,
                pageNumber = -1,
                user = UserFactory.sample()
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
            pageSize = 2,
            pageNumber = 1,
            user = UserFactory.sample()
        )

        assertThat(result.elements).hasSize(1)
        assertThat(result.pageInfo.totalElements).isEqualTo(3)
    }

    @Test
    fun `shows only news when category is news`() {
        val newsVideoId = saveVideo(title = "banana", type = ContentType.NEWS)
        saveVideo(title = "banana", type = ContentType.INSTRUCTIONAL_CLIPS)

        val videos = searchVideo.byQuery(
            query = "banana",
            type = setOf("NEWS"),
            pageSize = 2,
            pageNumber = 0,
            user = UserFactory.sample()
        )

        assertThat(videos.elements).hasSize(1)
        assertThat(videos.elements.first().videoId.value).isEqualTo(newsVideoId.value)
    }

    @Test
    fun `publishes a VIDEOS_SEARCHED event`() {
        saveVideo(title = "why are camels so tall 1")
        saveVideo(title = "why are camels so tall 2")
        saveVideo(title = "why are camels so tall 3")

        searchVideo.byQuery(
            query = "why are camels so tall",
            pageSize = 2,
            pageNumber = 1,
            user = UserFactory.sample()
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
            minDuration = "PT0S",
            maxDuration = "PT10S",
            pageSize = 20,
            pageNumber = 0,
            user = UserFactory.sample()
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
            pageSize = 20,
            pageNumber = 0,
            source = "youtube",
            user = UserFactory.sample()
        )

        assertThat(results.pageInfo.totalElements).isEqualTo(1)
        assertThat(results.elements.toList().size).isEqualTo(1)
        assertThat(results.elements.first().title).isEqualTo("why are camels so tall 2")
    }
}
