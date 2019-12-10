package com.boclips.videos.service.application.analytics

import com.boclips.eventbus.events.video.VideoSegmentPlayed
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SavePlaybackEventTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var savePlaybackEvent: SavePlaybackEvent
    private val videoId = TestFactories.aValidId()

    private val exampleEventOne = CreatePlaybackEventCommand(
        videoId = videoId,
        videoIndex = 1,
        segmentStartSeconds = 10,
        segmentEndSeconds = 20
    )

    private val exampleEventTwo = CreatePlaybackEventCommand(
        videoId = videoId,
        videoIndex = 1,
        segmentStartSeconds = 10,
        segmentEndSeconds = 20
    )

    @Test
    fun `saves one event`() {
        val user = UserFactory.sample()
        savePlaybackEvent.execute(listOf(exampleEventOne), playbackDevice = null, user = user)

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
        savePlaybackEvent.execute(listOf(exampleEventOne, exampleEventTwo), playbackDevice = null, user = user)

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
}
