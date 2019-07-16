package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ClassifyVideoTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var classifyVideo: ClassifyVideo

    @Test
    fun `publishes events for instructional videos`() {
        val video = TestFactories.createVideo(title = "the video title", type = LegacyVideoType.INSTRUCTIONAL_CLIPS)

        classifyVideo(video)

        val message = messageCollector.forChannel(topics.videoSubjectClassificationRequested()).poll()

        assertThat(message).isNotNull
        assertThat(message.payload.toString()).contains("the video title")
    }

    @Test
    fun `ignores stock videos`() {
        val video = TestFactories.createVideo(type = LegacyVideoType.STOCK)

        classifyVideo(video)

        val message = messageCollector.forChannel(topics.videoSubjectClassificationRequested()).poll()

        assertThat(message).isNull()
    }

    @Test
    fun `ignores news videos`() {
        val video = TestFactories.createVideo(type = LegacyVideoType.NEWS)

        classifyVideo(video)

        val message = messageCollector.forChannel(topics.videoSubjectClassificationRequested()).poll()

        assertThat(message).isNull()
    }

    @Test
    fun `ignores unplayable video`() {
        val video = TestFactories.createVideo(
            playback = VideoPlayback.FaultyPlayback(
                id = PlaybackId(value = "123", type = PlaybackProviderType.KALTURA)
            )
        )

        classifyVideo(video)

        val message = messageCollector.forChannel(topics.videoSubjectClassificationRequested()).poll()

        assertThat(message).isNull()
    }
}