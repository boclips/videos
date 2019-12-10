package com.boclips.videos.service.application.analytics

import com.boclips.eventbus.events.video.VideoSegmentPlayed
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.CreatePlaybackEventCommandFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZoneOffset
import java.time.ZonedDateTime

class SavePlaybackEventTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var savePlaybackEvent: SavePlaybackEvent
    private val videoId = TestFactories.aValidId()

    @Test
    fun `saves one event`() {
        val user = UserFactory.sample()
        savePlaybackEvent.execute(
            listOf(
                CreatePlaybackEventCommandFactory.sample(
                    videoId = videoId,
                    videoIndex = 1,
                    segmentStartSeconds = 10,
                    segmentEndSeconds = 20,
                    captureTime = ZonedDateTime.now()
                )
            ), playbackDevice = null, user = user
        )

        val event = fakeEventBus.getEventOfType(VideoSegmentPlayed::class.java)

        assertThat(event.videoId).isEqualTo(videoId)
        assertThat(event.userId).isEqualTo(user.id)
        assertThat(event.videoIndex).isEqualTo(1)
        assertThat(event.segmentEndSeconds).isEqualTo(20L)
        assertThat(event.segmentStartSeconds).isEqualTo(10L)
    }

    @Test
    fun `saves multiple events`() {
        val user = UserFactory.sample()
        savePlaybackEvent.execute(
            listOf(
                CreatePlaybackEventCommandFactory.sample(
                    videoId = videoId,
                    videoIndex = 1,
                    segmentStartSeconds = 10,
                    segmentEndSeconds = 20,
                    captureTime = ZonedDateTime.now()
                ), CreatePlaybackEventCommandFactory.sample(
                    videoId = videoId,
                    videoIndex = 1,
                    segmentStartSeconds = 10,
                    segmentEndSeconds = 20,
                    captureTime = ZonedDateTime.now()
                )
            ), playbackDevice = null, user = user
        )

        val events = fakeEventBus.getEventsOfType(VideoSegmentPlayed::class.java)

        assertThat(events[0].videoId).isEqualTo(videoId)
        assertThat(events[0].userId).isEqualTo(user.id)
        assertThat(events[0].videoIndex).isEqualTo(1)
        assertThat(events[0].segmentStartSeconds).isEqualTo(10L)
        assertThat(events[0].segmentEndSeconds).isEqualTo(20L)
        assertThat(events[0].playbackDevice).isNull()
        assertThat(events[0].timestamp).isNotNull()
        /* TODO make referrer explicit parameter and assert value here:
          assertThat(events.first().url).isEqualTo("https://teachers.boclips.com/videos?q=abc")*/

        assertThat(events[1].videoId).isEqualTo(videoId)
        assertThat(events[1].userId).isEqualTo(user.id)
        assertThat(events[1].videoIndex).isEqualTo(1)
        assertThat(events[1].segmentStartSeconds).isEqualTo(10L)
        assertThat(events[1].segmentEndSeconds).isEqualTo(20L)
        assertThat(events[1].playbackDevice).isNull()
        assertThat(events[1].timestamp).isNotNull()
    }

    @Test
    fun `for single event we do not validate timestamp`() {
        savePlaybackEvent.execute(
            listOf(CreatePlaybackEventCommandFactory.sample(captureTime = null)),
            playbackDevice = null,
            user = UserFactory.sample()
        )

        val event = fakeEventBus.getEventOfType(VideoSegmentPlayed::class.java)

        assertThat(event.timestamp).isNotNull()
    }

    @Test
    fun `for multiple event we do validate timestamp`() {
        val user = UserFactory.sample()

        val invalidEvent = CreatePlaybackEventCommandFactory.sample(captureTime = null)
        val validEvent = CreatePlaybackEventCommandFactory.sample(captureTime = ZonedDateTime.now())

        assertThrows<InvalidEventException> {
            savePlaybackEvent.execute(listOf(validEvent, invalidEvent), playbackDevice = null, user = user)
        }
    }

    @Test
    fun `for multiple events we store provided timestamps`() {
        val user = UserFactory.sample()
        val captureTime = ZonedDateTime.of(2018, 12, 10, 0, 0, 0, 0, ZoneOffset.UTC)
        val validEvent = CreatePlaybackEventCommandFactory.sample(captureTime = captureTime)

        savePlaybackEvent.execute(listOf(validEvent, validEvent), playbackDevice = null, user = user)

        val events = fakeEventBus.getEventsOfType(VideoSegmentPlayed::class.java)

        assertThat(events[0].timestamp).isEqualTo(captureTime)
        assertThat(events[1].timestamp).isEqualTo(captureTime)
    }
}
