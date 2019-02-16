package com.boclips.videos.service.config

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.GenericSearchServiceAdmin
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.search.service.infrastructure.ElasticSearchConfig
import com.boclips.search.service.infrastructure.ElasticSearchService
import com.boclips.search.service.infrastructure.ElasticSearchServiceAdmin
import com.boclips.search.service.infrastructure.legacy.SolrSearchService
import com.boclips.videos.service.application.video.search.ReportNoResults
import com.boclips.videos.service.config.properties.ElasticSearchProperties
import com.boclips.videos.service.config.properties.SolrProperties
import com.boclips.videos.service.domain.service.video.SearchService
import com.boclips.videos.service.infrastructure.email.EmailClient
import com.boclips.videos.service.infrastructure.search.VideoAssetSearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class SearchContext {
    @Bean
    @Profile("!fake-search")
    fun videoMetadataSearchService(elasticSearchConfig: ElasticSearchConfig): GenericSearchService {
        return ElasticSearchService(elasticSearchConfig)
    }

    @Bean
    @Profile("!fake-search")
    fun legacySearchService(solrProperties: SolrProperties): LegacySearchService {
        return SolrSearchService(host = solrProperties.host, port = solrProperties.port)
    }

    @Bean
    @Profile("!fake-search")
    fun videoSearchServiceAdmin(elasticSearchConfig: ElasticSearchConfig): GenericSearchServiceAdmin<VideoMetadata> {
        return ElasticSearchServiceAdmin(elasticSearchConfig)
    }

    @Bean
    fun searchService(
        videoMetadataSearchService: GenericSearchService,
        videoSearchServiceAdmin: GenericSearchServiceAdmin<VideoMetadata>
    ): SearchService {
        return VideoAssetSearchService(videoMetadataSearchService, videoSearchServiceAdmin)
    }

    @Bean
    fun elasticSearchConfig(elasticSearchProperties: ElasticSearchProperties): ElasticSearchConfig {
        return ElasticSearchConfig(
            scheme = elasticSearchProperties.scheme,
            host = elasticSearchProperties.host,
            port = elasticSearchProperties.port,
            username = elasticSearchProperties.username,
            password = elasticSearchProperties.password
        )
    }

    @Bean
    fun reportNoResults(emailClient: EmailClient): ReportNoResults {
        return ReportNoResults(emailClient)
    }
}