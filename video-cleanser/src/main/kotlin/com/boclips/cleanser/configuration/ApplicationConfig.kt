package com.boclips.cleanser.configuration

import com.boclips.cleanser.domain.service.VideoAnalysisService
import com.boclips.cleanser.infrastructure.boclips.BoclipsVideosRepository
import com.boclips.cleanser.infrastructure.kaltura.PaginationOrchestrator
import com.boclips.cleanser.infrastructure.kaltura.PagedKalturaMediaService
import com.boclips.cleanser.infrastructure.kaltura.client.KalturaMediaClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfig {
    @Bean
    fun analysisService(boclipsVideosRepository: BoclipsVideosRepository,
                        pagedKalturaMediaService: PagedKalturaMediaService): VideoAnalysisService {
        return VideoAnalysisService(boclipsVideosRepository, pagedKalturaMediaService)
    }

    @Bean
    fun paginationOrchestrator(kalturaMediaClient: KalturaMediaClient): PaginationOrchestrator {
        return PaginationOrchestrator(kalturaMediaClient, 10000, 500)
    }
}