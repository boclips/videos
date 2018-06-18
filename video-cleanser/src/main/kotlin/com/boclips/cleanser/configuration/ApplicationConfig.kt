package com.boclips.cleanser.configuration

import com.boclips.cleanser.domain.CleanserService
import com.boclips.cleanser.infrastructure.CleanserServiceImpl
import com.boclips.cleanser.infrastructure.boclips.BoclipsVideosRepository
import com.boclips.cleanser.infrastructure.kaltura.KalturaVideosRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfig {
    @Bean
    fun cleanserService(boclipsVideosRepository: BoclipsVideosRepository, kalturaVideosRepository: KalturaVideosRepository): CleanserService {
        return CleanserServiceImpl(boclipsVideosRepository, kalturaVideosRepository)
    }
}