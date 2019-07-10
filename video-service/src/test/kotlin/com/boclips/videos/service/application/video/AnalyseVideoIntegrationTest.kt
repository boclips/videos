package com.boclips.videos.service.application.video

import com.boclips.events.types.video.VideoAnalysisRequested
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.Locale

class AnalyseVideoIntegrationTest(
    @Autowired val analyseVideo: AnalyseVideo
) : AbstractSpringIntegrationTest() {

    @Test
    fun `sends an event`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "kaltura-id"),
            duration = Duration.ofSeconds(70)
        ).value

        analyseVideo(videoId, language = Locale.GERMAN)

        val message = messageCollector.forChannel(topics.videoAnalysisRequested()).poll()
        val event = objectMapper.readValue(message.payload.toString(), VideoAnalysisRequested::class.java)
        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.videoUrl).isEqualTo("https://download/video-entry-kaltura-id.mp4")
        assertThat(event.language).isEqualTo(Locale.GERMAN)
    }


    @Test
    fun `does not send events for videos not longer than 20s`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "kaltura-id"),
            duration = Duration.ofSeconds(20)
        ).value

        analyseVideo(videoId, language = null)

        val message = messageCollector.forChannel(topics.videoAnalysisRequested()).poll()

        assertThat(message).isNull()
    }

    @Test
    fun `does not send events for non instructional videos`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "kaltura-id"),
            legacyType = LegacyVideoType.NEWS
        ).value

        analyseVideo(videoId, language = null)

        val message = messageCollector.forChannel(topics.videoAnalysisRequested()).poll()

        assertThat(message).isNull()
    }

    @Test
    fun `throws on youtube videos`() {
        val videoId =
            saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "youtube-id")).value

        assertThrows<VideoNotAnalysableException> { analyseVideo(videoId, language = null) }
    }
}
