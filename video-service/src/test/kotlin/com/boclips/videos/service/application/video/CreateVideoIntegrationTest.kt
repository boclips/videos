package com.boclips.videos.service.application.video

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.eventbus.events.video.VideoCreated
import com.boclips.eventbus.events.video.VideoSubjectClassificationRequested
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.VideoSearchQuery
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import io.micrometer.core.instrument.Counter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class CreateVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoService: VideoService

    @Autowired
    lateinit var videoCounter: Counter

    @Test
    fun `requesting creation of an existing kaltura video creates the video`() {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val contentPartner = saveContentPartner()

        val video = createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = contentPartner.contentPartnerId.value,
                playbackId = "entry-\$123"
            ),
            UserFactory.sample()
        )

        assertThat(videoService.getPlayableVideo(video.videoId, VideoAccessRule.Everything)).isNotNull
    }

    @Test
    fun `requesting creation of an existing youtube video creates the video`() {
        fakeYoutubePlaybackProvider.addVideo("8889", "thumbnailUrl-url", duration = Duration.ZERO)
        fakeYoutubePlaybackProvider.addMetadata("8889", "channel name", "channel id")
        val contentPartner = saveContentPartner()

        val video =
            createVideo(
                VideoServiceApiFactory.createCreateVideoRequest(
                    providerId = contentPartner.contentPartnerId.value,
                    playbackId = "8889",
                    playbackProvider = "YOUTUBE"
                ),
                UserFactory.sample()
            )

        assertThat(videoService.getPlayableVideo(video.videoId, VideoAccessRule.Everything)).isNotNull
    }

    @Test
    fun `requesting creation of video without playback ignores video and throws`() {
        val contentPartner = saveContentPartner()


        assertThrows<VideoPlaybackNotFound> {
            createVideo(
                VideoServiceApiFactory.createCreateVideoRequest(
                    providerId = contentPartner.contentPartnerId.value,
                    playbackId = "1234"
                ),
                UserFactory.sample()
            )
        }

        assertThat(
            videoService.count(
                VideoSearchQuery(
                    text = "the latest Bloomberg video",
                    pageSize = 0,
                    pageIndex = 0
                ),
                VideoAccessRule.Everything
            )
        ).isEqualTo(0)
    }

    @Test
    fun `throws if content partner if it does not exist`() {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        assertThrows<ContentPartnerNotFoundException> {
            createVideo(
                VideoServiceApiFactory.createCreateVideoRequest(
                    providerId = "4321",
                    playbackId = "entry-$123"
                ),
                UserFactory.sample()
            )
        }
    }

    @Test
    fun `created video video uses the duration specified by the playback provider`() {
        val playbackProviderDuration = Duration.ofMinutes(2)

        createMediaEntry(
            id = "entry-$123",
            duration = playbackProviderDuration
        )

        val contentPartner = saveContentPartner()

        val createdVideo = createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = contentPartner.contentPartnerId.value,
                playbackId = "entry-\$123"
            ),
            UserFactory.sample()
        )

        val video = videoService.getPlayableVideo(createdVideo.videoId, VideoAccessRule.Everything)

        assertThat(video.playback.duration).isEqualTo(playbackProviderDuration)
    }

    @Test
    fun `throws when playback provider ID or type are missing`() {
        val contentPartner = saveContentPartner()
        val createRequest = VideoServiceApiFactory.createCreateVideoRequest(
            providerId = contentPartner.contentPartnerId.value,
            playbackId = null,
            playbackProvider = null
        )

        assertThrows<VideoPlaybackNotFound> { createVideo(createRequest, UserFactory.sample()) }
    }

    @Test
    fun `it requests that the video is analysed`() {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val contentPartner = saveContentPartner()

        val video: Video = createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = contentPartner.contentPartnerId.value,
                videoType = "INSTRUCTIONAL_CLIPS",
                playbackId = "entry-\$123",
                analyseVideo = true
            ),
            UserFactory.sample()
        )

        val event = fakeEventBus.getEventOfType(VideoAnalysisRequested::class.java)

        assertThat(event.videoId).isEqualTo(video.videoId.value)
        assertThat(event.videoUrl).isEqualTo("https://download.com/entryId/entry-\$123/format/download")
    }

    @Test
    fun `it dispatches a video created event`() {
        createMediaEntry(id = "1")

        val contentPartner = saveContentPartner()

        createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = contentPartner.contentPartnerId.value,
                title = "parabole",
                playbackId = "1"
            ),
            UserFactory.sample()
        )

        val event = fakeEventBus.getEventOfType(VideoCreated::class.java)

        assertThat(event.video.title).isEqualTo("parabole")
    }

    @Test
    fun `bumps video counter when video created`() {
        val videoCounterBefore = videoCounter.count()

        createAVideo("the latest and greatest Bloomberg video")

        val videoCounterAfter = videoCounter.count()

        assertThat(videoCounterAfter).isEqualTo(videoCounterBefore + 1)
    }

    @Test
    fun `when video is created it requests that the video subject is classified`() {
        createAVideo("the latest and greatest Bloomberg video")

        val event = fakeEventBus.getEventOfType(VideoSubjectClassificationRequested::class.java)
        assertThat(event.title).isEqualTo("the latest and greatest Bloomberg video")
    }

    private fun createAVideo(title: String) {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val contentPartner = saveContentPartner()

        val createRequest =
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = contentPartner.contentPartnerId.value,
                title = title,
                videoType = ContentType.INSTRUCTIONAL_CLIPS.toString(),
                playbackId = "entry-\$123"
            )

        createVideo(createRequest, UserFactory.sample())
    }
}
