package com.boclips.videos.service.application.video

import com.boclips.events.types.video.VideoAnalysisRequested
import com.boclips.events.types.video.VideoSubjectClassificationRequested
import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import io.micrometer.core.instrument.Counter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.Resource
import java.time.Duration

class CreateVideoTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoService: VideoService

    @Autowired
    lateinit var videoCounter: Counter

    @Test
    fun `requesting creation of an existing kaltura video creates the video`() {
        fakeKalturaClient.addMediaEntry(
            createMediaEntry(
                id = "entry-$123",
                referenceId = "1234",
                duration = Duration.ofMinutes(1)
            )
        )

        val resource = createVideo(TestFactories.createCreateVideoRequest(playbackId = "1234"))

        assertThat(videoService.getPlayableVideo(VideoId(resource.content.id!!))).isNotNull
    }

    @Test
    fun `requesting creation of an existing youtube video creates the video`() {
        fakeYoutubePlaybackProvider.addVideo("8889", "thumbnailUrl-url", duration = Duration.ZERO)

        val resource =
            createVideo(TestFactories.createCreateVideoRequest(playbackId = "8889", playbackProvider = "YOUTUBE"))

        assertThat(videoService.getPlayableVideo(VideoId(resource.content.id!!))).isNotNull
    }

    @Test
    fun `requesting creation of video without playback ignores video and throws`() {
        assertThrows<VideoPlaybackNotFound> {
            createVideo(TestFactories.createCreateVideoRequest(playbackId = "1234"))
        }

        assertThat(
            videoService.count(
                VideoSearchQuery(
                    text = "the latest Bloomberg video",
                    includeTags = emptyList(),
                    excludeTags = emptyList(),
                    pageSize = 0,
                    pageIndex = 0
                )
            )
        ).isEqualTo(0)
    }

    @Test
    fun `created video becomes available in search`() {
        fakeKalturaClient.addMediaEntry(
            createMediaEntry(
                id = "entry-$123",
                referenceId = "1234",
                duration = Duration.ofMinutes(1)
            )
        )

        val createRequest =
            TestFactories.createCreateVideoRequest(playbackId = "1234", title = "the latest Bloomberg video")
        createVideo(createRequest)

        val results = videoService.search(
            VideoSearchQuery(
                text = "the latest bloomberg",
                includeTags = emptyList(),
                excludeTags = emptyList(),
                pageSize = 1,
                pageIndex = 0
            )
        )

        val firstVideoInResults = results.first()
        assertThat(firstVideoInResults.title).isEqualTo("the latest Bloomberg video")
    }

    @Test
    fun `created video is made available in legacy search`() {
        fakeKalturaClient.addMediaEntry(
            createMediaEntry(
                id = "entry-$123",
                referenceId = "1234",
                duration = Duration.ofMinutes(1)
            )
        )

        createVideo(TestFactories.createCreateVideoRequest(playbackId = "1234", title = "the latest Bloomberg video"))

        verify(legacySearchService).upsert(any(), anyOrNull())
    }

    @Test
    fun `created video video uses the duration specified by the playback provider`() {
        val playbackProviderDuration = Duration.ofMinutes(2)

        fakeKalturaClient.addMediaEntry(
            createMediaEntry(
                id = "entry-$123",
                referenceId = "1234",
                duration = playbackProviderDuration
            )
        )

        val resource = createVideo(TestFactories.createCreateVideoRequest(playbackId = "1234"))

        val video = videoService.getPlayableVideo(VideoId(resource.content.id!!))

        assertThat(video.playback.duration).isEqualTo(playbackProviderDuration)
        assertThat(video.playback.duration).isEqualTo(playbackProviderDuration)
    }

    @Test
    fun `throws when create request is incomplete`() {
        val createRequest = TestFactories.createCreateVideoRequest(description = null, playbackProvider = "KALTURA")

        fakeKalturaClient.addMediaEntry(
            createMediaEntry(
                id = "entry-$123",
                referenceId = createRequest.playbackId!!,
                duration = Duration.ZERO
            )
        )

        assertThrows<NonNullableFieldCreateRequestException> { createVideo(createRequest) }
    }

    @Test
    fun `throws when playback provider ID or type are missing`() {
        val createRequest = TestFactories.createCreateVideoRequest(playbackId = null, playbackProvider = null)

        assertThrows<VideoPlaybackNotFound> { createVideo(createRequest) }
    }

    @Test
    fun `bumps video counter`() {
        val videoCounterBefore = videoCounter.count()

        fakeKalturaClient.addMediaEntry(
            createMediaEntry(
                id = "entry-$123",
                referenceId = "1234",
                duration = Duration.ofMinutes(1)
            )
        )

        createVideo(TestFactories.createCreateVideoRequest(playbackId = "1234"))

        val videoCounterAfter = videoCounter.count()

        assertThat(videoCounterAfter).isEqualTo(videoCounterBefore + 1)
    }

    @Test
    fun `does not populate legacy search when youtube video is created`() {
        fakeYoutubePlaybackProvider.addVideo("1234", thumbnailUrl = "some-thumb", duration = Duration.ZERO)
        createVideo(
            TestFactories.createCreateVideoRequest(
                playbackId = "1234",
                title = "the latest banana video",
                playbackProvider = "YOUTUBE"
            )
        )

        verifyZeroInteractions(legacySearchService)
    }

    @Test
    fun `it requests that the video is analysed`() {
        fakeKalturaClient.addMediaEntry(
            createMediaEntry(
                id = "entry-$123",
                referenceId = "1234",
                duration = Duration.ofMinutes(1)
            )
        )

        val video: Resource<VideoResource> = createVideo(
            TestFactories.createCreateVideoRequest(
                playbackId = "1234",
                videoType = "INSTRUCTIONAL_CLIPS",
                analyseVideo = true
            )
        )

        val message = messageCollector.forChannel(topics.videoAnalysisRequested()).poll()
        val event = objectMapper.readValue(message.payload.toString(), VideoAnalysisRequested::class.java)

        assertThat(event.videoId).isEqualTo(video.content.id)
        assertThat(event.videoUrl).isEqualTo("https://download/video-entry-$123.mp4")
    }

    @Test
    fun `it requests that the video subject is classified`() {
        fakeKalturaClient.addMediaEntry(createMediaEntry(referenceId = "1234"))

        createVideo(
            TestFactories.createCreateVideoRequest(
                playbackId = "1234",
                videoType = "INSTRUCTIONAL_CLIPS",
                title = "fractions"
            )
        )

        val message = messageCollector.forChannel(topics.videoSubjectClassificationRequested()).poll()

        assertThat(message.payload.toString()).contains("fractions")
    }
}
