package com.boclips.videos.service.application.analytics

import com.boclips.eventbus.events.page.PageRendered
import com.boclips.videos.service.presentation.event.PageRenderedWithEvent
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SavePageRenderedWithEventTest : AbstractSpringIntegrationTest(){

    @Autowired
    lateinit var savePageRenderedWithEvent: SavePageRenderedWithEvent

    @Test
    fun execute() {
        val payload = PageRenderedWithEvent(url = "https://teachers.boclips.com/collections")
        savePageRenderedWithEvent.execute(payload)

        val event = fakeEventBus.getEventOfType(PageRendered::class.java)
        assertThat(event.url).isEqualTo("https://teachers.boclips.com/collections")
    }
}
