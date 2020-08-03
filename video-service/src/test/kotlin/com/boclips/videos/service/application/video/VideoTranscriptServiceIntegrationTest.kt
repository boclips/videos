package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoTranscriptCreated
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoTranscriptServiceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoRepository: VideoRepository

    @Test
    fun `updates transcripts of a video`() {
        val videoId = saveVideo()

        val transcriptCreated = VideoTranscriptCreated.builder()
            .videoId(videoId.value)
            .transcript("some transcripts")
            .build()

        fakeEventBus.publish(transcriptCreated)

        assertThat(videoRepository.find(videoId)!!.voice.transcript).isEqualTo("some transcripts")
    }
}
