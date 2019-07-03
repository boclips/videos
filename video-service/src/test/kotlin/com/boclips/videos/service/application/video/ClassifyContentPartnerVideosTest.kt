package com.boclips.videos.service.application.video

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ClassifyContentPartnerVideosTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var classifyContentPartnerVideos: ClassifyContentPartnerVideos

    @Test
    fun `classifying a video sends a message to the relevant channel`() {
        saveVideo(title = "matrix multiplication")

        classifyContentPartnerVideos(null).get()

        val message = messageCollector.forChannel(topics.videoSubjectClassificationRequested()).poll()

        Assertions.assertThat(message).isNotNull
        Assertions.assertThat(message.payload.toString()).contains("matrix multiplication")
    }
}