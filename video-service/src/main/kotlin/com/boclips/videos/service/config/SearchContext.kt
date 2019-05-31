package com.boclips.videos.service.config

import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.ESConfig
import com.boclips.search.service.infrastructure.collections.ESCollectionReadSearchService
import com.boclips.search.service.infrastructure.collections.ESCollectionWriteSearchService
import com.boclips.search.service.infrastructure.legacy.SolrSearchService
import com.boclips.search.service.infrastructure.videos.ESVideoReadSearchService
import com.boclips.search.service.infrastructure.videos.ESVideoWriteSearchService
import com.boclips.videos.service.application.video.search.ReportNoResults
import com.boclips.videos.service.config.properties.ElasticSearchProperties
import com.boclips.videos.service.config.properties.SolrProperties
import com.boclips.videos.service.infrastructure.email.EmailClient
import com.boclips.videos.service.infrastructure.search.DefaultVideoSearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class SearchContext {
    @Bean
    @Profile("!fake-search")
    fun videoMetadataSearchService(ESConfig: ESConfig) = ESVideoReadSearchService(ESConfig.buildClient())

    @Bean
    @Profile("!fake-search")
    fun videoSearchServiceAdmin(ESConfig: ESConfig): ESVideoWriteSearchService {
        return ESVideoWriteSearchService(ESConfig.buildClient())
    }

    @Bean
    @Profile("!fake-search")
    fun collectionMetadataSearchService(ESConfig: ESConfig) = ESCollectionReadSearchService(ESConfig.buildClient())

    @Bean
    @Profile("!fake-search")
    fun collectionSearchServiceAdmin(ESConfig: ESConfig) = ESCollectionWriteSearchService(ESConfig.buildClient())

    @Bean
    @Profile("!fake-search")
    fun legacySearchService(solrProperties: SolrProperties): LegacySearchService {
        return SolrSearchService(host = solrProperties.host, port = solrProperties.port)
    }

    @Bean
    fun searchService(
        videoMetadataSearchService: ReadSearchService<VideoMetadata, VideoQuery>,
        writeSearchService: WriteSearchService<VideoMetadata>
    ): com.boclips.videos.service.domain.service.video.VideoSearchService {
        return DefaultVideoSearchService(videoMetadataSearchService, writeSearchService)
    }

    @Bean
    fun elasticSearchClient(elasticSearchProperties: ElasticSearchProperties): ESConfig {
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