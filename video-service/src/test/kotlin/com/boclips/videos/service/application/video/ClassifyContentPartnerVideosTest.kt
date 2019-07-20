package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoSubjectClassificationRequested
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ClassifyContentPartnerVideosTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var classifyContentPartnerVideos: ClassifyContentPartnerVideos

    @Test
    fun `classifying a video sends a message to the relevant channel`() {
        saveVideo(title = "matrix multiplication")

        classifyContentPartnerVideos(null).get()

        val event = fakeEventBus.getEventOfType(VideoSubjectClassificationRequested::class.java)

        assertThat(event.title).isEqualTo("matrix multiplication")
    }
}
