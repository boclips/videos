package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.presentation.event.CreatePlayerInteractedWithEvent
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SavePlayerInteractedWithEventTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var savePlayerInteractedWithEvent: SavePlayerInteractedWithEvent

    private val payload = CreatePlayerInteractedWithEvent(
        playerId = "player-id",
        videoId = TestFactories.aValidId(),
        videoDurationSeconds = 60,
        currentTime = 54,
        type = "captions-on",
        payload = mapOf<String, Any>(
            Pair("kind", "caption-kind"),
            Pair("language", "caption-language"),
            Pair("id", "caption-id"),
            Pair("label", "caption-label")
        )
    )

    @Test
    fun `saves the event`() {
        savePlayerInteractedWithEvent.execute(payload)

        val event = messageCollector.forChannel(topics.videoPlayerInteractedWith()).poll()

        assertThat(event).isNotNull
        assertThat(event.payload.toString()).contains("player-id")
        assertThat(event.payload.toString()).contains("captions-on")
        assertThat(event.payload.toString()).contains("60")
        assertThat(event.payload.toString()).contains("54")
        assertThat(event.payload.toString()).contains("caption-kind")
        assertThat(event.payload.toString()).contains("caption-language")
        assertThat(event.payload.toString()).contains("caption-id")
        assertThat(event.payload.toString()).contains("caption-label")
    }
}
