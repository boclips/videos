package com.boclips.videos.service.config

import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.infrastructure.ESConfig
import com.boclips.search.service.infrastructure.legacy.SolrSearchService
import com.boclips.search.service.infrastructure.videos.ESVideoReadSearchService
import com.boclips.search.service.infrastructure.videos.ESVideoWriteSearchService
import com.boclips.videos.service.application.video.search.ReportNoResults
import com.boclips.videos.service.config.properties.ElasticSearchProperties
import com.boclips.videos.service.config.properties.SolrProperties
import com.boclips.videos.service.domain.service.video.SearchService
import com.boclips.videos.service.infrastructure.email.EmailClient
import com.boclips.videos.service.infrastructure.search.VideoVideoSearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class SearchContext {
    @Bean
    @Profile("!fake-search")
    fun videoMetadataSearchService(ESConfig: ESConfig): com.boclips.search.service.domain.videos.VideoSearchService {
        return ESVideoReadSearchService(ESConfig)
    }

    @Bean
    @Profile("!fake-search")
    fun legacySearchService(solrProperties: SolrProperties): LegacySearchService {
        return SolrSearchService(host = solrProperties.host, port = solrProperties.port)
    }

    @Bean
    @Profile("!fake-search")
    fun videoSearchServiceAdmin(ESConfig: ESConfig): WriteSearchService<VideoMetadata> {
        return ESVideoWriteSearchService(ESConfig)
    }

    @Bean
    fun searchService(
        videoMetadataSearchService: com.boclips.search.service.domain.videos.VideoSearchService,
        writeSearchService: WriteSearchService<VideoMetadata>
    ): SearchService {
        return VideoVideoSearchService(videoMetadataSearchService, writeSearchService)
    }

    @Bean
    fun elasticSearchConfig(elasticSearchProperties: ElasticSearchProperties): ESConfig {
        return ESConfig(
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