package com.boclips.videos.service.application.subject

import com.boclips.eventbus.domain.SubjectId
import com.boclips.eventbus.events.video.VideoSubjectClassificationRequested
import com.boclips.eventbus.events.video.VideoSubjectClassified
import com.boclips.videos.api.request.video.UpdateVideoRequest
import com.boclips.videos.service.application.video.UpdateVideo
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SubjectClassificationServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var updateVideo: UpdateVideo

    @Nested
    inner class PublishingEvents {
        @Test
        fun `publishes events for instructional videos`() {
            val video = TestFactories.createVideo(title = "the video title", type = ContentType.INSTRUCTIONAL_CLIPS)

            subjectClassificationService.classifyVideo(video)

            val event = fakeEventBus.getEventOfType(VideoSubjectClassificationRequested::class.java)

            assertThat(event.title).isEqualTo("the video title")
        }

        @Test
        fun `ignores stock videos`() {
            val video = TestFactories.createVideo(type = ContentType.STOCK)

            subjectClassificationService.classifyVideo(video)

            assertThat(fakeEventBus.hasReceivedEventOfType(VideoSubjectClassificationRequested::class.java)).isFalse()
        }

        @Test
        fun `ignores news videos`() {
            val video = TestFactories.createVideo(type = ContentType.NEWS)

            subjectClassificationService.classifyVideo(video)

            assertThat(fakeEventBus.hasReceivedEventOfType(VideoSubjectClassificationRequested::class.java)).isFalse()
        }

        @Test
        fun `ignores unplayable video`() {
            val video = TestFactories.createVideo(
                playback = VideoPlayback.FaultyPlayback(
                    id = PlaybackId(value = "123", type = PlaybackProviderType.KALTURA)
                )
            )

            subjectClassificationService.classifyVideo(video)

            assertThat(fakeEventBus.hasReceivedEventOfType(VideoSubjectClassificationRequested::class.java)).isFalse()
        }

        @Test
        fun `classifying a video sends a message to the relevant channel`() {
            saveVideo(title = "matrix multiplication")

            subjectClassificationService.classifyVideosByContentPartner(null).get()

            val event = fakeEventBus.getEventOfType(VideoSubjectClassificationRequested::class.java)

            assertThat(event.title).isEqualTo("matrix multiplication")
        }
    }

    @Nested
    inner class ReadingEvents {
        @Test
        fun `replaces subjects on VideoSubjectClassified Event`() {
            val newSubject = saveSubject("scottish")
            val videoId = saveVideo()

            val videoClassified = VideoSubjectClassified.builder()
                .videoId(videoId.value)
                .subjects(setOf(SubjectId(newSubject.id.value)))
                .build()

            fakeEventBus.publish(videoClassified)

            assertThat(videoRepository.find(videoId)!!.subjects.items).containsExactly(newSubject)
            assertThat(videoRepository.find(videoId)!!.subjects.setManually).isFalse()
        }

        @Test
        fun `does not replace subjects on VideoSubjectClassified Event when subjects are set manually`() {
            val manuallySetSubject = saveSubject("english")
            val newSubject = saveSubject("scottish")
            val videoId = saveVideo()
            updateVideo.invoke(
                id = videoId.value,
                updateRequest = UpdateVideoRequest(subjectIds = listOf(manuallySetSubject.id.value)),
                user = UserFactory.sample()
            )

            val videoClassified = VideoSubjectClassified.builder()
                .videoId(videoId.value)
                .subjects(setOf(SubjectId(newSubject.id.value)))
                .build()

            fakeEventBus.publish(videoClassified)

            assertThat(videoRepository.find(videoId)!!.subjects.items).containsExactly(manuallySetSubject)
            assertThat(videoRepository.find(videoId)!!.subjects.setManually).isTrue()
        }
    }
}
