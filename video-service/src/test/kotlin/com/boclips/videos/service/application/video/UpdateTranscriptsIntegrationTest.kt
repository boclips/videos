package com.boclips.videos.service.application.video

import com.boclips.events.config.subscriptions.VideoTranscriptCreatedSubscription
import com.boclips.events.types.video.VideoTranscriptCreated
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder

class UpdateTranscriptsIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoRepository: VideoRepository

    @Autowired
    lateinit var videoTranscriptCreatedSubscription: VideoTranscriptCreatedSubscription

    @Test
    fun `updates transcripts of a video`() {
        val videoId = saveVideo()

        val transcriptCreated = VideoTranscriptCreated.builder()
            .videoId(videoId.value)
            .transcript("some transcripts")
            .build()

        val message = MessageBuilder.withPayload(transcriptCreated).build()

        videoTranscriptCreatedSubscription.channel().send(message)

        assertThat(videoRepository.find(videoId)!!.transcript).isEqualTo("some transcripts")
    }
}
