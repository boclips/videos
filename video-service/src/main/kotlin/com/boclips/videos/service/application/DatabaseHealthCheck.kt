package com.boclips.videos.service.application

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import mu.KLogging
import org.bson.types.ObjectId
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class DatabaseHealthCheck(val videoRepository: VideoRepository) : HealthIndicator {
    companion object : KLogging()

    override fun health(): Health {
        try {
            videoRepository.find(VideoId(ObjectId().toHexString()))
        } catch (ex: Exception) {
            logger.info(ex) { "Cannot retrieve videos" }
            return Health.down().build()
        }

        return Health.up().build()
    }
}
