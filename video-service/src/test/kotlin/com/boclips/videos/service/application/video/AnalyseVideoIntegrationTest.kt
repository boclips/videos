package com.boclips.videos.service.application.video

import com.boclips.events.types.VideoToAnalyse
import com.boclips.videos.service.domain.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class AnalyseVideoIntegrationTest(
    @Autowired val analyseVideo: AnalyseVideo
) : AbstractSpringIntegrationTest() {

    @Test
    fun `sends an event`() {
        val videoId = saveVideo(
                playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "kaltura-id"),
                searchable = true
        ).value

        analyseVideo(videoId)

        val message = messageCollector.forChannel(topics.videosToAnalyse()).poll()
        val event = objectMapper.readValue(message.payload.toString(), VideoToAnalyse::class.java)
        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.videoUrl).isEqualTo("https://download/video-entry-kaltura-id.mp4")
    }

    @Test
    fun `does not send events for non searchable videos`() {
        val videoId = saveVideo(
                playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "kaltura-id"),
                searchable = false
        ).value

        analyseVideo(videoId)

        val message = messageCollector.forChannel(topics.videosToAnalyse()).poll()

        assertThat(message).isNull()
    }

    @Test
    fun `throws on youtube videos`() {
        val videoId = saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "youtube-id")).value

        assertThrows<VideoNotAnalysableException> { analyseVideo(videoId) }
    }
}
