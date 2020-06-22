package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.eventbus.events.video.VideoCreated
import com.boclips.eventbus.events.video.VideoSubjectClassificationRequested
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.service.application.video.exceptions.ChannelNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.request.VideoRequest
import com.boclips.videos.service.domain.service.video.VideoRetrievalService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import io.micrometer.core.instrument.Counter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class CreateVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRetrievalService: VideoRetrievalService

    @Autowired
    lateinit var videoCounter: Counter

    @Test
    fun `requesting creation of an existing kaltura video creates the video`() {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val contentPartner = saveChannel()

        val video = createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = contentPartner.id.value,
                playbackId = "entry-\$123"
            )
        )

        assertThat(videoRetrievalService.getPlayableVideo(video.videoId, VideoAccess.Everything)).isNotNull
    }

    @Test
    fun `creating a video which already exists as a YT scrape deactivates the old video`() {
        fakeYoutubePlaybackProvider.addVideo("8889", "thumbnailUrl-url", duration = Duration.ZERO)
        fakeYoutubePlaybackProvider.addMetadata("8889", "channel name", "channel id")

        createMediaEntry(id = "entry-$123", duration = Duration.ofMinutes(1))

        val contentPartnerYt = saveChannel(name = "TED")
        val contentPartnerNew = saveChannel(name = "TED")

        val scrapedVideo =
            createVideo(
                VideoServiceApiFactory.createCreateVideoRequest(
                    providerId = contentPartnerYt.id.value,
                    providerVideoId = "8889",
                    playbackId = "8889",
                    title = "The same video",
                    playbackProvider = "YOUTUBE"
                )
            )


        val activeVideo = createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = contentPartnerNew.id.value,
                providerVideoId = "1234",
                title = "The same video",
                playbackId = "entry-\$123"
            )
        )

        val updatedScrapedVideo = videoRetrievalService.getPlayableVideo(scrapedVideo.videoId, VideoAccess.Everything)
        assertThat(updatedScrapedVideo.deactivated).isTrue()
        assertThat(updatedScrapedVideo.activeVideoId).isEqualTo(activeVideo.videoId)

        val newVideo = videoRetrievalService.getPlayableVideo(activeVideo.videoId, VideoAccess.Everything)
        assertThat(newVideo).isNotNull
    }

    @Test
    fun `requesting creation of an existing youtube video creates the video`() {
        fakeYoutubePlaybackProvider.addVideo("8889", "thumbnailUrl-url", duration = Duration.ZERO)
        fakeYoutubePlaybackProvider.addMetadata("8889", "channel name", "channel id")
        val contentPartner = saveChannel()

        val video =
            createVideo(
                VideoServiceApiFactory.createCreateVideoRequest(
                    providerId = contentPartner.id.value,
                    playbackId = "8889",
                    playbackProvider = "YOUTUBE"
                )
            )

        assertThat(videoRetrievalService.getPlayableVideo(video.videoId, VideoAccess.Everything)).isNotNull
    }

    @Test
    fun `requesting creation of video without playback ignores video and throws`() {
        val contentPartner = saveChannel()

        assertThrows<VideoPlaybackNotFound> {
            createVideo(
                VideoServiceApiFactory.createCreateVideoRequest(
                    providerId = contentPartner.id.value,
                    playbackId = "1234"
                )
            )
        }

        assertThat(
            videoRetrievalService.searchPlayableVideos(
                VideoRequest(
                    text = "the latest Bloomberg video",
                    pageSize = 0,
                    pageIndex = 0
                ),
                VideoAccess.Everything
            ).counts.total
        ).isEqualTo(0)
    }

    @Test
    fun `throws if content partner if it does not exist`() {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        assertThrows<ChannelNotFoundException> {
            createVideo(
                VideoServiceApiFactory.createCreateVideoRequest(
                    providerId = "4321",
                    playbackId = "entry-$123"
                )
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

        val contentPartner = saveChannel()

        val createdVideo = createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = contentPartner.id.value,
                playbackId = "entry-\$123"
            )
        )

        val video = videoRetrievalService.getPlayableVideo(createdVideo.videoId, VideoAccess.Everything)

        assertThat(video.playback.duration).isEqualTo(playbackProviderDuration)
    }

    @Test
    fun `throws when playback provider ID or type are missing`() {
        val contentPartner = saveChannel()
        val createRequest = VideoServiceApiFactory.createCreateVideoRequest(
            providerId = contentPartner.id.value,
            playbackId = null,
            playbackProvider = null
        )

        assertThrows<VideoPlaybackNotFound> { createVideo(createRequest) }
    }

    @Test
    fun `it requests that the video is analysed`() {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val contentPartner = saveChannel()

        val video: Video = createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = contentPartner.id.value,
                videoTypes = listOf("INSTRUCTIONAL_CLIPS"),
                playbackId = "entry-\$123",
                analyseVideo = true
            )
        )

        val event = fakeEventBus.getEventOfType(VideoAnalysisRequested::class.java)

        assertThat(event.videoId).isEqualTo(video.videoId.value)
        assertThat(event.videoUrl).isEqualTo("https://download.com/entryId/entry-\$123/format/download")
    }

    @Test
    fun `it dispatches a video created event`() {
        createMediaEntry(id = "1")

        val contentPartner = saveChannel()

        createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = contentPartner.id.value,
                title = "parabole",
                playbackId = "1"
            )
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

    @Test
    fun `can create a video with language from an ISO 639-2 code`() {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val contentPartner = saveChannel()

        val createRequest = VideoServiceApiFactory.createCreateVideoRequest(
            language = "wel",
            providerId = contentPartner.id.value,
            playbackId = "entry-\$123"
        )
        val createdVideo = createVideo(createRequest)

        assertThat(createdVideo.language?.displayLanguage).isEqualTo("Welsh")
    }

    private fun createAVideo(title: String) {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val contentPartner = saveChannel()

        val createRequest =
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = contentPartner.id.value,
                title = title,
                videoTypes = listOf(ContentType.INSTRUCTIONAL_CLIPS.toString()),
                playbackId = "entry-\$123"
            )

        createVideo(createRequest)
    }
}
