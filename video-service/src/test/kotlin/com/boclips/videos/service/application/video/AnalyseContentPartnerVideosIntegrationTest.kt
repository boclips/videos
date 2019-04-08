package com.boclips.videos.service.application.video

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.Message

class AnalyseContentPartnerVideosIntegrationTest(
        @Autowired val analyseContentPartnerVideos: AnalyseContentPartnerVideos
) : AbstractSpringIntegrationTest() {

    @Test
    fun `sends events`() {
        saveVideo(contentProvider = "Ted")
        saveVideo(contentProvider = "Ted")
        saveVideo(contentProvider = "Bob")

        analyseContentPartnerVideos("Ted")

        val messages = mutableListOf<Message<*>>().apply {
            messageCollector.forChannel(topics.videosToAnalyse()).drainTo(this)
        }

        assertThat(messages).hasSize(2)
    }
}
