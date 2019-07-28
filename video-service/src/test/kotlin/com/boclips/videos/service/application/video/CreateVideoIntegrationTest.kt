package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.eventbus.events.video.VideoCreated
import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.common.UnboundedAgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoSearchQuery
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createMediaEntry
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

        val event = fakeEventBus.getEventOfType(VideoAnalysisRequested::class.java)

        assertThat(event.videoId).isEqualTo(video.content.id)
        assertThat(event.videoUrl).isEqualTo("https://download/video-entry-$123.mp4")
    }

    @Test
    fun `it dispatches a video created event`() {
        fakeKalturaClient.addMediaEntry(createMediaEntry(referenceId = "1"))

        createVideo(TestFactories.createCreateVideoRequest(playbackId = "1", title = "parabole"))

        val event = fakeEventBus.getEventOfType(VideoCreated::class.java)

        assertThat(event.video.title).isEqualTo("parabole")
    }

    @Test
    fun `it gets the distribution method from its content partner`() {
        fakeKalturaClient.addMediaEntry(
            createMediaEntry(
                id = "entry-$123",
                referenceId = "1234",
                duration = Duration.ofMinutes(1)
            )
        )

        val contentPartner = saveContentPartner(
            distributionMethods = setOf(
                DistributionMethodResource.DOWNLOAD,
                DistributionMethodResource.STREAM
            )
        )

        val createRequest =
            TestFactories.createCreateVideoRequest(
                provider = contentPartner.name,
                title = "the latest and greatest Bloomberg video",
                playbackId = "1234"
            )

        val videoResource = createVideo(createRequest)
        assertThat(videoResource.content.distributionMethods).containsExactlyInAnyOrder(
            DistributionMethodResource.DOWNLOAD,
            DistributionMethodResource.STREAM
        )
    }

    @Test
    fun `bumps video counter when video created`() {
        val videoCounterBefore = videoCounter.count()

        fakeKalturaClient.addMediaEntry(
            createMediaEntry(
                id = "entry-$123",
                referenceId = "1234",
                duration = Duration.ofMinutes(1)
            )
        )
        
        val createRequest =
            TestFactories.createCreateVideoRequest(
                title = "the latest and greatest Bloomberg video",
                playbackId = "1234"
            )

        createVideo(createRequest)

        val videoCounterAfter = videoCounter.count()

        assertThat(videoCounterAfter).isEqualTo(videoCounterBefore + 1)
    }
}
