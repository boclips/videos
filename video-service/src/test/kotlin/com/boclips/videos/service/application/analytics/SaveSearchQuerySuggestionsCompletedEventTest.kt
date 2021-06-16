package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class SaveSearchQuerySuggestionsCompletedEventTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var saveSearchQuerySuggestionsCompletedEvent: SaveSearchQuerySuggestionsCompletedEvent

    @Test
    fun `throws when event is null`() {
        assertThrows<InvalidEventException> {
            saveSearchQuerySuggestionsCompletedEvent.execute(
                null,
                UserFactory.sample()
            )
        }
    }
}
