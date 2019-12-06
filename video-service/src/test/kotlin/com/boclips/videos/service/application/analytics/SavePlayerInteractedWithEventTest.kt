package com.boclips.videos.service.application.analytics

import com.boclips.eventbus.events.video.VideoPlayerInteractedWith
import com.boclips.videos.service.presentation.event.CreatePlayerInteractedWithEvent
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SavePlayerInteractedWithEventTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var savePlayerInteractedWithEvent: SavePlayerInteractedWithEvent

    private val payload = CreatePlayerInteractedWithEvent(
        videoId = TestFactories.aValidId(),
        currentTime = 54,
        subtype = "captions-on",
        payload = mapOf<String, Any>(
            Pair("kind", "caption-kind"),
            Pair("language", "caption-language"),
            Pair("id", "caption-id"),
            Pair("label", "caption-label")
        )
    )

    @Test
    fun `saves the event`() {
        savePlayerInteractedWithEvent.execute(payload, UserFactory.sample())

        val firedEvent = fakeEventBus.getEventOfType(VideoPlayerInteractedWith::class.java)

        assertThat(firedEvent.subtype).isEqualTo("captions-on")
        assertThat(firedEvent.currentTime).isEqualTo(54L)
        assertThat(firedEvent.payload).containsEntry("kind", "caption-kind")
        assertThat(firedEvent.payload).containsEntry("language", "caption-language")
        assertThat(firedEvent.payload).containsEntry("id", "caption-id")
        assertThat(firedEvent.payload).containsEntry("label", "caption-label")
    }
}
