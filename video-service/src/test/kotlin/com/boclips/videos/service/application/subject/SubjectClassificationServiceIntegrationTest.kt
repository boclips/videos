package com.boclips.videos.service.application.subject

import com.boclips.eventbus.events.video.VideoSubjectClassificationRequested
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SubjectClassificationServiceIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var subjectClassificationService: SubjectClassificationService

    @Test
    fun `publishes events for instructional videos`() {
        val video = TestFactories.createVideo(title = "the video title", type = LegacyVideoType.INSTRUCTIONAL_CLIPS)

        subjectClassificationService.classifyVideo(video)

        val event = fakeEventBus.getEventOfType(VideoSubjectClassificationRequested::class.java)

        assertThat(event.title).isEqualTo("the video title")
    }

    @Test
    fun `ignores stock videos`() {
        val video = TestFactories.createVideo(type = LegacyVideoType.STOCK)

        subjectClassificationService.classifyVideo(video)

        assertThat(fakeEventBus.hasReceivedEventOfType(VideoSubjectClassificationRequested::class.java)).isFalse()
    }

    @Test
    fun `ignores news videos`() {
        val video = TestFactories.createVideo(type = LegacyVideoType.NEWS)

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
