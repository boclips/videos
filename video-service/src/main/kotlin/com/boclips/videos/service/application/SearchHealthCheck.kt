package com.boclips.videos.service.application

import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.service.video.VideoSearchService
import mu.KLogging
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class SearchHealthCheck(val videoSearchService: VideoSearchService) : HealthIndicator {
    companion object : KLogging()

    override fun health(): Health {
        try {
            videoSearchService.count(VideoQuery())
        } catch (ex: Exception) {
            logger.info(ex) { "Cannot connect to Search" }
            return Health.down().build()
        }

        return Health.up().build()
    }
}
