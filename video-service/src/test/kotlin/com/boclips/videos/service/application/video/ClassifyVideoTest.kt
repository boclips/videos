package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ClassifyVideoTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var classifyVideo: ClassifyVideo

    @Test
    fun `publishes events for instructional videos`() {
        val video = saveVideo(title = "the video title", legacyType = LegacyVideoType.INSTRUCTIONAL_CLIPS)

        classifyVideo(video.value)

        val message = messageCollector.forChannel(topics.videoSubjectClassificationRequested()).poll()

        assertThat(message).isNotNull
        assertThat(message.payload.toString()).contains("the video title")
    }

    @Test
    fun `ignores non-instructional videos`() {
        val video = saveVideo(legacyType = LegacyVideoType.STOCK)

        classifyVideo(video.value)

        val message = messageCollector.forChannel(topics.videoSubjectClassificationRequested()).poll()

        assertThat(message).isNull()
    }
}