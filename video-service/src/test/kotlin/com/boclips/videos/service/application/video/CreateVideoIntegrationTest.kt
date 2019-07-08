package com.boclips.videos.service.application.video

import com.boclips.events.types.video.VideoAnalysisRequested
import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.VideoSearchQuery
import com.boclips.videos.service.domain.model.common.UnboundedAgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
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

class CreateVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoService: VideoService

    @Autowired
    lateinit var contentPartnerRepository: ContentPartnerRepository

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
        fakeYoutubePlaybackProvider.addMetadata("8889", "channel name", "channel id")

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
            TestFactories.createCreateVideoRequest(title = "the latest Bloomberg video", playbackId = "1234")
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

        createVideo(TestFactories.createCreateVideoRequest(title = "the latest Bloomberg video", playbackId = "1234"))

        verify(legacyVideoSearchService).upsert(any(), anyOrNull())
    }

    @Test
    fun `does not populate legacy search when youtube video is created`() {
        fakeYoutubePlaybackProvider.addVideo("1234", thumbnailUrl = "some-thumb", duration = Duration.ZERO)
        fakeYoutubePlaybackProvider.addMetadata("1234", "channel name", "channel id")
        createVideo(
            TestFactories.createCreateVideoRequest(
                title = "the latest banana video",
                playbackId = "1234",
                playbackProvider = "YOUTUBE"
            )
        )

        verifyZeroInteractions(legacyVideoSearchService)
    }

    @Test
    fun `creates content partner if it does not exist`() {
        fakeKalturaClient.addMediaEntry(
            createMediaEntry(
                id = "entry-$123",
                referenceId = "1234",
                duration = Duration.ofMinutes(1)
            )
        )

        createVideo(
            TestFactories.createCreateVideoRequest(
                provider = "another-youtube-channel",
                playbackId = "1234"
            )
        )

        val contentPartner = contentPartnerRepository.findByName("another-youtube-channel")

        assertThat(contentPartner!!.name).isEqualTo("another-youtube-channel")
        assertThat(contentPartner.contentPartnerId).isNotNull
        assertThat(contentPartner.ageRange).isInstanceOf(UnboundedAgeRange::class.java)
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
                videoType = "INSTRUCTIONAL_CLIPS",
                playbackId = "1234",
                analyseVideo = true
            )
        )

        val message = messageCollector.forChannel(topics.videoAnalysisRequested()).poll()
        val event = objectMapper.readValue(message.payload.toString(), VideoAnalysisRequested::class.java)

        assertThat(event.videoId).isEqualTo(video.content.id)
        assertThat(event.videoUrl).isEqualTo("https://download/video-entry-$123.mp4")
    }

    @Test
    fun `it dispatches a video updated event`() {
        fakeKalturaClient.addMediaEntry(createMediaEntry(referenceId = "1"))

        createVideo(TestFactories.createCreateVideoRequest(playbackId = "1", title = "parabole"))

        val message = messageCollector.forChannel(topics.videoUpdated()).poll()

        assertThat(message.payload.toString()).contains("parabole")
    }

    @Test
    fun `it requests that the video subject is classified`() {
        fakeKalturaClient.addMediaEntry(createMediaEntry(referenceId = "1234"))

        createVideo(
            TestFactories.createCreateVideoRequest(
                title = "fractions",
                videoType = "INSTRUCTIONAL_CLIPS",
                playbackId = "1234"
            )
        )

        val message = messageCollector.forChannel(topics.videoSubjectClassificationRequested()).poll()

        assertThat(message.payload.toString()).contains("fractions")
    }

    @Test
    fun `it does not add to search indices if content partner is not searchable`() {
        fakeKalturaClient.addMediaEntry(
            createMediaEntry(
                id = "entry-$123",
                referenceId = "1234",
                duration = Duration.ofMinutes(1)
            )
        )

        val contentPartner = saveContentPartner(searchable = false)

        val createRequest =
            TestFactories.createCreateVideoRequest(
                provider = contentPartner.name,
                title = "the latest and greatest Bloomberg video",
                playbackId = "1234"
            )

        createVideo(createRequest)

        val results = videoService.search(
            VideoSearchQuery(
                text = "the latest and greatest Bloomberg video",
                includeTags = emptyList(),
                excludeTags = emptyList(),
                pageSize = 1,
                pageIndex = 0
            )
        )

        assertThat(results).hasSize(0)
        verifyZeroInteractions(legacyVideoSearchService)
    }
}
