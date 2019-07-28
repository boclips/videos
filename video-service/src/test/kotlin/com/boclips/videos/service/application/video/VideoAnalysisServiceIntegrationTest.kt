package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.createKalturaCaptionAsset
import com.boclips.videos.service.testsupport.TestFactories.createVideoAnalysed
import com.boclips.videos.service.testsupport.TestFactories.createVideoAnalysedKeyword
import com.boclips.videos.service.testsupport.TestFactories.createVideoAnalysedTopic
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.Locale

class VideoAnalysisServiceIntegrationTest(@Autowired val videoAnalysisService: VideoAnalysisService) :
    AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoRepository: VideoRepository

    @Nested
    inner class SendsEvent {
        @Test
        fun `sends an event`() {
            val videoId = saveVideo(
                playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "kaltura-id"),
                duration = Duration.ofSeconds(70)
            ).value

            videoAnalysisService.analysePlayableVideo(videoId, language = Locale.GERMAN)

            val event = fakeEventBus.getEventOfType(VideoAnalysisRequested::class.java)

            assertThat(event.videoId).isEqualTo(videoId)
            assertThat(event.videoUrl).isEqualTo("https://download/video-entry-kaltura-id.mp4")
            assertThat(event.language).isEqualTo(Locale.GERMAN)
        }

        @Test
        fun `does not send events for videos not longer than 20s`() {
            val videoId = saveVideo(
                playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "kaltura-id"),
                duration = Duration.ofSeconds(20)
            ).value

            videoAnalysisService.analysePlayableVideo(videoId, language = null)

            assertThat(fakeEventBus.hasReceivedEventOfType(VideoAnalysisRequested::class.java)).isFalse()
        }

        @Test
        fun `does not send events for non instructional videos`() {
            val videoId = saveVideo(
                playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "kaltura-id"),
                legacyType = LegacyVideoType.NEWS
            ).value

            videoAnalysisService.analysePlayableVideo(videoId, language = null)

            assertThat(fakeEventBus.hasReceivedEventOfType(VideoAnalysisRequested::class.java)).isFalse()
        }

        @Test
        fun `throws on youtube videos`() {
            val videoId =
                saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "youtube-id")).value

            org.junit.jupiter.api.assertThrows<VideoNotAnalysableException> {
                videoAnalysisService.analysePlayableVideo(
                    videoId,
                    language = null
                )
            }
        }

        @Test
        fun `it should only send analyse messages for Ted`() {
            saveVideo(contentProvider = "Ted")
            saveVideo(contentProvider = "Ted")
            saveVideo(contentProvider = "Bob")

            videoAnalysisService.analyseVideosOfContentPartner("Ted", language = null)

            assertThat(fakeEventBus.countEventsOfType(VideoAnalysisRequested::class.java)).isEqualTo(2)
        }
    }

    @Nested
    inner class HandlesEvent {
        @Test
        fun `uploads captions to Kaltura`() {
            val videoId =
                saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "reference-id"))
            val videoAnalysed = createVideoAnalysed(videoId = videoId.value)

            fakeEventBus.publish(videoAnalysed)

            assertThat(fakeKalturaClient.getCaptionFilesByReferenceId("reference-id")).isNotEmpty
        }

        @Test
        fun `does NOT upload captions to Kaltura when transcript has no words`() {
            val videoId =
                saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "reference-id"))
            val videoAnalysed = createVideoAnalysed(videoId = videoId.value, transcript = "\n")

            fakeEventBus.publish(videoAnalysed)

            assertThat(fakeKalturaClient.getCaptionFilesByReferenceId("reference-id")).isEmpty()
        }

        @Test
        fun `deletes existing auto-generated captions when transcript has no words`() {
            val existingCaptions = createKalturaCaptionAsset(
                language = KalturaLanguage.ENGLISH,
                label = "English (auto-generated)"
            )
            fakeKalturaClient.createCaptionsFile("reference-id", existingCaptions, "bla bla bla")

            val videoId =
                saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "reference-id"))
            val videoAnalysed = createVideoAnalysed(videoId = videoId.value, transcript = "\n")

            fakeEventBus.publish(videoAnalysed)

            assertThat(fakeKalturaClient.getCaptionFilesByReferenceId("reference-id")).isEmpty()
        }

        @Test
        fun `stores language`() {
            val videoId =
                saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "reference-id"))
            val videoAnalysed = createVideoAnalysed(
                videoId = videoId.value,
                language = Locale.ITALY
            )

            fakeEventBus.publish(videoAnalysed)

            val video = videoRepository.find(videoId)!!

            assertThat(video.language).isEqualTo(Locale.ITALY)
        }

        @Test
        fun `stores transcript`() {
            val videoId =
                saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "reference-id"))
            val videoAnalysed = createVideoAnalysed(
                videoId = videoId.value,
                transcript = "bla bla bla"
            )

            fakeEventBus.publish(videoAnalysed)

            val video = videoRepository.find(videoId)!!

            assertThat(video.transcript).isEqualTo("bla bla bla")
        }

        @Test
        fun `stores eventBus`() {
            val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "reference-id"))
            val videoAnalysed = createVideoAnalysed(
                videoId = videoId.value,
                topics = listOf(createVideoAnalysedTopic(name = "topic name"))
            )

            fakeEventBus.publish(videoAnalysed)

            val video = videoRepository.find(videoId)!!

            assertThat(video.eventBus).hasSize(1)
            assertThat(video.eventBus.first().name).isEqualTo("topic name")
        }

        @Test
        fun `stores merged keywords`() {
            val videoId = saveVideo(
                playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "reference-id"),
                keywords = listOf("old keyword 1", "old keyword 2")
            )
            val videoAnalysed = createVideoAnalysed(
                videoId = videoId.value,
                keywords = listOf(
                    createVideoAnalysedKeyword(name = "old keyword 2"),
                    createVideoAnalysedKeyword(name = "new keyword")
                )
            )

            fakeEventBus.publish(videoAnalysed)

            val video = videoRepository.find(videoId)!!

            assertThat(video.keywords).containsExactlyInAnyOrder("old keyword 1", "old keyword 2", "new keyword")
        }

        @Test
        fun `updates the video in the search index`() {
            val videoId = saveVideo()

            val videoAnalysed = createVideoAnalysed(videoId = videoId.value, transcript = "the transcript")

            fakeEventBus.publish(videoAnalysed)

            assertThat(
                videoSearchService.search(
                    PaginatedSearchRequest(
                        query = VideoQuery(
                            "transcript"
                        )
                    )
                )
            ).containsExactly(videoId.value)
        }
    }
}