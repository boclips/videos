package com.boclips.videos.service.application

import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.video.VideoSearchService
import mu.KLogging
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class SearchHealthCheck(
    val videoSearchService: VideoSearchService,
    val collectionSearchService: CollectionSearchService
) : HealthIndicator {
    companion object : KLogging()

    override fun health(): Health {
        try {
            videoSearchService.makeSureIndexIsThere()
            collectionSearchService.makeSureIndexIsThere()
        } catch (ex: Exception) {
            logger.info(ex) { "Cannot connect to Search" }
            return Health.down().build()
        }

        return Health.up().build()
    }
}
