package com.boclips.videos.service.infrastructure.analytics

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class MongoAnalyticsServiceIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mongoAnalyticsService: MongoAnalyticsService

    @Autowired
    lateinit var analyticsRepository: AnalyticsRepository

    @Test
    fun `searches are stored as analytics events`() {
        mongoAnalyticsService.saveSearch(query = "brownie", resultsReturned = 9)

        assertThat(analyticsRepository.count()).isEqualTo(1)
    }
}