package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.videos.service.application.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.Locale

internal class VideoAnalysisServiceIntegrationTest(@Autowired val videoAnalysisService: VideoAnalysisService) :
    AbstractSpringIntegrationTest() {

    @Test
    fun `sends an event`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "kaltura-id"),
            duration = Duration.ofSeconds(70)
        ).value

        videoAnalysisService.analysePlayableVideo(videoId, language = Locale.GERMAN)

        val event = fakeEventBus.getEventOfType(VideoAnalysisRequested::class.java)

        Assertions.assertThat(event.videoId).isEqualTo(videoId)
        Assertions.assertThat(event.videoUrl).isEqualTo("https://download/video-entry-kaltura-id.mp4")
        Assertions.assertThat(event.language).isEqualTo(Locale.GERMAN)
    }

    @Test
    fun `does not send events for videos not longer than 20s`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "kaltura-id"),
            duration = Duration.ofSeconds(20)
        ).value

        videoAnalysisService.analysePlayableVideo(videoId, language = null)

        Assertions.assertThat(fakeEventBus.hasReceivedEventOfType(VideoAnalysisRequested::class.java)).isFalse()
    }

    @Test
    fun `does not send events for non instructional videos`() {
        val videoId = saveVideo(
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "kaltura-id"),
            legacyType = LegacyVideoType.NEWS
        ).value

        videoAnalysisService.analysePlayableVideo(videoId, language = null)

        Assertions.assertThat(fakeEventBus.hasReceivedEventOfType(VideoAnalysisRequested::class.java)).isFalse()
    }

    @Test
    fun `throws on youtube videos`() {
        val videoId =
            saveVideo(playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "youtube-id")).value

        org.junit.jupiter.api.assertThrows<VideoNotAnalysableException> {
            videoAnalysisService.analysePlayableVideo(
                videoId,
                language = null
            )
        }
    }

    @Test
    fun `it should only send analyse messages for Ted`() {
        saveVideo(contentProvider = "Ted")
        saveVideo(contentProvider = "Ted")
        saveVideo(contentProvider = "Bob")

        videoAnalysisService.analyseVideosOfContentPartner("Ted", language = null)

        Assertions.assertThat(fakeEventBus.countEventsOfType(VideoAnalysisRequested::class.java)).isEqualTo(2)
    }
}