package com.boclips.videos.service.config

import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.search.service.infrastructure.ElasticSearchClient
import com.boclips.search.service.infrastructure.collections.CollectionIndexReader
import com.boclips.search.service.infrastructure.collections.CollectionIndexWriter
import com.boclips.search.service.infrastructure.videos.VideoIndexReader
import com.boclips.search.service.infrastructure.videos.VideoIndexWriter
import com.boclips.search.service.infrastructure.videos.legacy.SolrVideoSearchService
import com.boclips.videos.service.application.video.search.ReportNoResults
import com.boclips.videos.service.config.properties.ElasticSearchProperties
import com.boclips.videos.service.config.properties.SolrProperties
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.infrastructure.email.EmailClient
import com.boclips.videos.service.infrastructure.search.DefaultCollectionSearch
import com.boclips.videos.service.infrastructure.search.DefaultVideoSearch
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class SearchContext {
    @Bean
    @Profile("!fakes-search")
    fun legacySearchService(solrProperties: SolrProperties): LegacyVideoSearchService {
        return SolrVideoSearchService(host = solrProperties.host, port = solrProperties.port)
    }

    @Bean
    @Profile("!fakes-search")
    fun videoSearchService(elasticSearchClient: ElasticSearchClient): VideoSearchService {
        return DefaultVideoSearch(
            VideoIndexReader(elasticSearchClient.buildClient()),
            VideoIndexWriter(elasticSearchClient.buildClient())
        )
    }

    @Bean
    @Profile("!fakes-search")
    fun collectionSearchService(elasticSearchClient: ElasticSearchClient): CollectionSearchService {
        return DefaultCollectionSearch(
            CollectionIndexReader(elasticSearchClient.buildClient()),
            CollectionIndexWriter(elasticSearchClient.buildClient())
        )
    }

    @Bean
    @Profile("!fakes-search")
    fun elasticSearchClient(elasticSearchProperties: ElasticSearchProperties): ElasticSearchClient {
        return ElasticSearchClient(
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