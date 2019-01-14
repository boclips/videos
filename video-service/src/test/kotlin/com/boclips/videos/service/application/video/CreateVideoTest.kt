package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.application.video.exceptions.InvalidCreateVideoRequestException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import io.micrometer.core.instrument.Counter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyCollectionOf
import org.mockito.ArgumentMatchers.anyListOf
import org.mockito.verification.VerificationMode
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class CreateVideoTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var createVideo: CreateVideo

    @Autowired
    lateinit var videoService: VideoService

    @Autowired
    lateinit var videoCounter: Counter

    @Autowired
    lateinit var legacySearchService: LegacySearchService

    @Test
    fun `requesting creation of an existing kaltura video creates the video`() {
        fakeKalturaClient.addMediaEntry(createMediaEntry(id = "entry-$123", referenceId = "1234", duration = Duration.ofMinutes(1)))

        val resource = createVideo.execute(TestFactories.createCreateVideoRequest(playbackId = "1234"))

        assertThat(videoService.get(AssetId(resource.id!!))).isNotNull
    }

    @Test
    fun `requesting creation of an existing youtube video creates the video`() {
        fakeYoutubePlaybackProvider.addVideo("8889", "thumbnail-url", duration = Duration.ZERO)

        val resource = createVideo.execute(TestFactories.createCreateVideoRequest(playbackId = "8889", playbackProvider = "YOUTUBE"))

        assertThat(videoService.get(AssetId(resource.id!!))).isNotNull
    }

    @Test
    fun `requesting creation of video without playback ignores video and throws`() {
        assertThrows<VideoPlaybackNotFound> {
            createVideo.execute(TestFactories.createCreateVideoRequest(playbackId = "1234"))
        }

        assertThat(videoService.count(VideoSearchQuery(
                text = "the latest Bloomberg video",
                includeTags = emptyList(),
                excludeTags = emptyList(),
                pageSize = 0,
                pageIndex = 0
        ))).isEqualTo(0)
    }

    @Test
    fun `created video becomes available in search`() {
        fakeKalturaClient.addMediaEntry(createMediaEntry(id = "entry-$123", referenceId = "1234", duration = Duration.ofMinutes(1)))

        createVideo.execute(TestFactories.createCreateVideoRequest(playbackId = "1234", title = "the latest Bloomberg video"))

        assertThat(videoService.search(VideoSearchQuery(
                text = "the latest bloomberg",
                includeTags = emptyList(),
                excludeTags = emptyList(),
                pageSize = 1,
                pageIndex = 0
        )).first().asset.title).isEqualTo("the latest Bloomberg video")
    }

    @Test
    fun `created video is made available in legacy search`() {
        fakeKalturaClient.addMediaEntry(createMediaEntry(id = "entry-$123", referenceId = "1234", duration = Duration.ofMinutes(1)))

        createVideo.execute(TestFactories.createCreateVideoRequest(playbackId = "1234", title = "the latest Bloomberg video"))

        verify(legacySearchService).upsert(any())
    }

    @Test
    fun `throws when create request is incomplete`() {
        assertThrows<InvalidCreateVideoRequestException> {
            createVideo.execute(TestFactories.createCreateVideoRequest(playbackId = null))
        }
    }

    @Test
    fun `bumps video counter`() {
        val videoCounterBefore = videoCounter.count()

        fakeKalturaClient.addMediaEntry(createMediaEntry(id = "entry-$123", referenceId = "1234", duration = Duration.ofMinutes(1)))

        createVideo.execute(TestFactories.createCreateVideoRequest(playbackId = "1234"))

        val videoCounterAfter = videoCounter.count()

        assertThat(videoCounterAfter).isEqualTo(videoCounterBefore + 1)
    }
}
