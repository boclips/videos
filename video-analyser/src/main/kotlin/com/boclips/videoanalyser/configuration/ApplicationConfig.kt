package com.boclips.videoanalyser.configuration

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.KalturaClientConfig
import com.boclips.videoanalyser.domain.service.VideoAnalysisService
import com.boclips.videoanalyser.domain.service.search.SearchBenchmarkService
import com.boclips.videoanalyser.domain.service.search.SearchClient
import com.boclips.videoanalyser.infrastructure.BoclipsVideoRepository
import com.boclips.videoanalyser.infrastructure.kaltura.PagedKalturaMediaService
import com.boclips.videoanalyser.infrastructure.kaltura.PaginationOrchestrator
import com.boclips.videoanalyser.infrastructure.kaltura.client.KalturaMediaClient
import com.boclips.videoanalyser.infrastructure.search.LegacyBoclipsSearchClient
import com.boclips.videoanalyser.infrastructure.search.VideoServiceSearchClient
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

    @Bean
    fun searchBenchmarkService(legacyBoclipsSearchClient: LegacyBoclipsSearchClient, videoServiceSearchClient: VideoServiceSearchClient): SearchBenchmarkService {
        return SearchBenchmarkService(legacyBoclipsSearchClient, videoServiceSearchClient)
    }

    @Bean
    fun kalturaClient(propertiesKaltura: PropertiesKaltura): KalturaClient {
        return KalturaClient.create(KalturaClientConfig.builder()
                .partnerId(propertiesKaltura.partnerId)
                .userId(propertiesKaltura.userId)
                .secret(propertiesKaltura.secret)
                .sessionTtl(86400)
                .build())
    }
}