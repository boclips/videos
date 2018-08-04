package com.boclips.videoanalyser.configuration

import com.boclips.videoanalyser.domain.service.VideoAnalysisService
import com.boclips.videoanalyser.infrastructure.BoclipsVideoRepository
import com.boclips.videoanalyser.infrastructure.kaltura.PagedKalturaMediaService
import com.boclips.videoanalyser.infrastructure.kaltura.PaginationOrchestrator
import com.boclips.videoanalyser.infrastructure.kaltura.client.KalturaMediaClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry

@Configuration
@EnableRetry
class ApplicationConfig {
    @Bean
    fun analysisService(boclipsVideoRepository: BoclipsVideoRepository,
                        pagedKalturaMediaService: PagedKalturaMediaService): VideoAnalysisService {
        return VideoAnalysisService(boclipsVideoRepository, pagedKalturaMediaService)
    }

    @Bean
    fun paginationOrchestrator(kalturaMediaClient: KalturaMediaClient): PaginationOrchestrator {
        return PaginationOrchestrator(kalturaMediaClient, 10000, 500)
    }
}