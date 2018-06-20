package com.boclips.cleanser.configuration

import com.boclips.cleanser.domain.service.CleanserService
import com.boclips.cleanser.infrastructure.boclips.BoclipsVideosRepository
import com.boclips.cleanser.infrastructure.kaltura.TimePagedKalturaMediaService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfig {
    @Bean
    fun cleanserService(boclipsVideosRepository: BoclipsVideosRepository, timePagedKalturaMediaService: TimePagedKalturaMediaService): CleanserService {
        return CleanserService(boclipsVideosRepository, timePagedKalturaMediaService)
    }
}