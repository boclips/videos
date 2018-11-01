package com.boclips.videos.service.config

import com.boclips.kalturaclient.KalturaClient
import com.boclips.search.service.domain.SearchService
import com.boclips.search.service.infrastructure.ElasticSearchConfig
import com.boclips.search.service.infrastructure.ElasticSearchService
import com.boclips.videos.service.config.properties.ElasticSearchProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class SearchConfig {
    @Bean
    @Profile("!fake-search")
    fun searchService(elasticSearchProperties: ElasticSearchProperties, kalturaClient: KalturaClient): SearchService {
        return ElasticSearchService(ElasticSearchConfig(
                scheme = elasticSearchProperties.scheme,
                host = elasticSearchProperties.host,
                port = elasticSearchProperties.port,
                username = elasticSearchProperties.username,
                password = elasticSearchProperties.password
        ))
    }
}