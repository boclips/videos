package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.eventbus.events.video.VideoCreated
import com.boclips.eventbus.events.video.VideoSubjectClassificationRequested
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.service.application.video.exceptions.ChannelNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.application.video.search.RetrievePlayableVideos
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoType
import com.boclips.videos.service.domain.model.video.request.VideoRequest
import com.boclips.videos.service.domain.model.video.request.VideoRequestPagingState
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.CategoryFactory
import com.boclips.videos.service.testsupport.UserFactory
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
    lateinit var retrievePlayableVideos: RetrievePlayableVideos

    @Autowired
    lateinit var videoCounter: Counter

    @Test
    fun `requesting creation of an existing kaltura video creates the video`() {
        taxonomyRepository.create(CategoryFactory.sample(code = "A", description = "A description"))
        taxonomyRepository.create(CategoryFactory.sample(code = "B", description = "B description"))
        taxonomyRepository.create(CategoryFactory.sample(code = "C", description = "C description"))
        taxonomyRepository.create(CategoryFactory.sample(code = "D", description = "D description"))

        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val channel = saveChannel(categories = listOf("C", "D"))

        val video = createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = channel.id.value,
                playbackId = "entry-\$123",
                categories = listOf("A", "B")
            ),
            UserFactory.sample()
        )

        val createdVideo = videoRetrievalService.getPlayableVideo(video.videoId, VideoAccess.Everything(emptySet()))
        assertThat(createdVideo).isNotNull
        assertThat(createdVideo.channelCategories).containsExactlyInAnyOrder(
            CategoryWithAncestors(codeValue = CategoryCode("C"), description = "C description", ancestors = emptySet()),
            CategoryWithAncestors(codeValue = CategoryCode("D"), description = "D description", ancestors = emptySet()),
        )
        assertThat(createdVideo.manualCategories).containsExactlyInAnyOrder(
            CategoryWithAncestors(codeValue = CategoryCode("A"), description = "A description", ancestors = emptySet()),
            CategoryWithAncestors(codeValue = CategoryCode("B"), description = "B description", ancestors = emptySet()),
        )
    }

    @Test
    fun `creating a video which already exists as a YT scrape deactivates the old video`() {
        fakeYoutubePlaybackProvider.addVideo("8889", "thumbnailUrl-url", duration = Duration.ZERO)
        fakeYoutubePlaybackProvider.addMetadata("8889", "channel name", "channel id")

        createMediaEntry(id = "entry-$123", duration = Duration.ofMinutes(1))

        val channelYt = saveChannel(name = "TED")
        val channelNew = saveChannel(name = "TED")

        val scrapedVideo =
            createVideo(
                VideoServiceApiFactory.createCreateVideoRequest(
                    providerId = channelYt.id.value,
                    providerVideoId = "8889",
                    playbackId = "8889",
                    title = "The same video",
                    playbackProvider = "YOUTUBE"
                ),
                UserFactory.sample()
            )

        val activeVideo = createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = channelNew.id.value,
                providerVideoId = "1234",
                title = "The same video",
                playbackId = "entry-\$123"
            ),
            UserFactory.sample()
        )

        val updatedScrapedVideo = videoRetrievalService.getPlayableVideo(
            scrapedVideo.videoId,
            VideoAccess.Everything(
                emptySet()
            )
        )
        assertThat(updatedScrapedVideo.deactivated).isTrue()
        assertThat(updatedScrapedVideo.activeVideoId).isEqualTo(activeVideo.videoId)

        val newVideo = videoRetrievalService.getPlayableVideo(activeVideo.videoId, VideoAccess.Everything(emptySet()))
        assertThat(newVideo).isNotNull
    }

    @Test
    fun `requesting creation of an existing youtube video creates the video`() {
        fakeYoutubePlaybackProvider.addVideo("8889", "thumbnailUrl-url", duration = Duration.ZERO)
        fakeYoutubePlaybackProvider.addMetadata("8889", "channel name", "channel id")
        val channel = saveChannel()

        val video =
            createVideo(
                VideoServiceApiFactory.createCreateVideoRequest(
                    providerId = channel.id.value,
                    playbackId = "8889",
                    playbackProvider = "YOUTUBE"
                ),
                UserFactory.sample()
            )

        assertThat(videoRetrievalService.getPlayableVideo(video.videoId, VideoAccess.Everything(emptySet()))).isNotNull
    }

    @Test
    fun `requesting creation of video without playback ignores video and throws`() {
        val channel = saveChannel()

        assertThrows<VideoPlaybackNotFound> {
            createVideo(
                VideoServiceApiFactory.createCreateVideoRequest(
                    providerId = channel.id.value,
                    playbackId = "1234"
                ),
                UserFactory.sample()
            )
        }

        assertThat(
            retrievePlayableVideos.searchPlayableVideos(
                VideoRequest(
                    text = "the latest Bloomberg video",
                    pageSize = 0,
                    pagingState = VideoRequestPagingState.PageNumber(0)
                ),
                VideoAccess.Everything(emptySet())
            ).counts.total
        ).isEqualTo(0)
    }

    @Test
    fun `throws if channel if it does not exist`() {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        assertThrows<ChannelNotFoundException> {
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

        val channel = saveChannel()

        val createdVideo = createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = channel.id.value,
                playbackId = "entry-\$123"
            ),
            UserFactory.sample()
        )

        val video = videoRetrievalService.getPlayableVideo(createdVideo.videoId, VideoAccess.Everything(emptySet()))

        assertThat(video.playback.duration).isEqualTo(playbackProviderDuration)
    }

    @Test
    fun `throws when playback provider ID or type are missing`() {
        val channel = saveChannel()
        val createRequest = VideoServiceApiFactory.createCreateVideoRequest(
            providerId = channel.id.value,
            playbackId = null,
            playbackProvider = null
        )

        assertThrows<VideoPlaybackNotFound> {
            createVideo(
                createRequest,
                UserFactory.sample()
            )
        }
    }

    @Test
    fun `it requests that the video is analysed`() {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val channel = saveChannel()

        val video: Video = createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = channel.id.value,
                videoTypes = listOf("INSTRUCTIONAL_CLIPS"),
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

        val channel = saveChannel()

        createVideo(
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = channel.id.value,
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

    @Test
    fun `can create a video with language from an ISO 639-2 code`() {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val channel = saveChannel()

        val createRequest = VideoServiceApiFactory.createCreateVideoRequest(
            language = "wel",
            providerId = channel.id.value,
            playbackId = "entry-\$123"
        )
        val createdVideo = createVideo(createRequest, UserFactory.sample())

        assertThat(createdVideo.voice.language?.displayLanguage).isEqualTo("Welsh")
    }

    @Test
    fun `when no language specified the video takes the channel's language`() {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val channel = saveChannel(language = "wel")

        val createRequest = VideoServiceApiFactory.createCreateVideoRequest(
            language = null,
            providerId = channel.id.value,
            playbackId = "entry-\$123"
        )
        val createdVideo = createVideo(createRequest, UserFactory.sample())

        assertThat(createdVideo.voice.language?.displayLanguage).isEqualTo("Welsh")
    }

    private fun createAVideo(title: String) {
        createMediaEntry(
            id = "entry-$123",
            duration = Duration.ofMinutes(1)
        )

        val channel = saveChannel()

        val createRequest =
            VideoServiceApiFactory.createCreateVideoRequest(
                providerId = channel.id.value,
                title = title,
                videoTypes = listOf(VideoType.INSTRUCTIONAL_CLIPS.toString()),
                playbackId = "entry-\$123"
            )

        createVideo(createRequest, UserFactory.sample())
    }
}
