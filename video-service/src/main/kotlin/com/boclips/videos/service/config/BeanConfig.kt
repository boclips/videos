package com.boclips.videos.service.config

import com.boclips.videos.service.application.SearchVideos
import com.boclips.videos.service.domain.service.AnalyticsSearchServiceDecorator
import com.boclips.videos.service.domain.service.AnalyticsService
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.infrastructure.analytics.AnalyticsRepository
import com.boclips.videos.service.infrastructure.analytics.MongoAnalyticsService
import com.boclips.videos.service.infrastructure.search.ElasticSearchProperties
import com.boclips.videos.service.infrastructure.search.ElasticSearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig {

    @Bean
    fun searchService(elasticSearchProperties: ElasticSearchProperties, analyticsService: AnalyticsService): SearchService {
        val searchService = ElasticSearchService(elasticSearchProperties)
        return AnalyticsSearchServiceDecorator(searchService, analyticsService)
    }

    @Bean
    fun searchVideos(searchService: SearchService): SearchVideos {
        return SearchVideos(searchService)
    }

    @Bean
    fun analyticsService(analyticsRepository: AnalyticsRepository): AnalyticsService {
        return MongoAnalyticsService(analyticsRepository)
    }
}