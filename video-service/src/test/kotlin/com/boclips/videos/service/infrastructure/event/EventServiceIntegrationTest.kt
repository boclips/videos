package com.boclips.videos.service.infrastructure.event

import com.boclips.videos.service.infrastructure.search.SearchEvent
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class EventServiceIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var eventService: EventService

    @Autowired
    lateinit var eventLogRepository: EventLogRepository

    @Test
    fun `searches are stored as analytics events`() {
        eventService.saveEvent(SearchEvent("brownie", 9))

        assertThat(eventLogRepository.count()).isEqualTo(1)
    }
}