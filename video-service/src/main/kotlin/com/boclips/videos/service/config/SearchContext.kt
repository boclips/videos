package com.boclips.videos.service.config

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.ElasticSearchClient
import com.boclips.search.service.infrastructure.collections.CollectionIndexReader
import com.boclips.search.service.infrastructure.collections.CollectionIndexWriter
import com.boclips.search.service.infrastructure.videos.legacy.SolrVideoSearchService
import com.boclips.search.service.infrastructure.videos.VideoIndexReader
import com.boclips.search.service.infrastructure.videos.VideoIndexWriter
import com.boclips.videos.service.application.video.search.ReportNoResults
import com.boclips.videos.service.config.properties.ElasticSearchProperties
import com.boclips.videos.service.config.properties.SolrProperties
import com.boclips.videos.service.infrastructure.email.EmailClient
import com.boclips.videos.service.infrastructure.search.DefaultVideoSearch
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class SearchContext {

    @Bean
    @Profile("!fake-search")
    fun videoMetadataSearchService(ElasticSearchClient: ElasticSearchClient) = VideoIndexReader(ElasticSearchClient.buildClient())

    @Bean
    @Profile("!fake-search")
    fun videoSearchServiceAdmin(ElasticSearchClient: ElasticSearchClient): VideoIndexWriter {
        return VideoIndexWriter(ElasticSearchClient.buildClient())
    }

    @Bean
    @Profile("!fake-search")
    fun collectionMetadataSearchService(ElasticSearchClient: ElasticSearchClient) = CollectionIndexReader(ElasticSearchClient.buildClient())

    @Bean
    @Profile("!fake-search")
    fun collectionSearchServiceAdmin(ElasticSearchClient: ElasticSearchClient) = CollectionIndexWriter(ElasticSearchClient.buildClient())

    @Bean
    @Profile("!fake-search")
    fun legacySearchService(solrProperties: SolrProperties): LegacyVideoSearchService {
        return SolrVideoSearchService(host = solrProperties.host, port = solrProperties.port)
    }

    @Bean
    fun searchService(
        videoMetadataSearchService: IndexReader<VideoMetadata, VideoQuery>,
        indexWriter: IndexWriter<VideoMetadata>
    ): com.boclips.videos.service.domain.service.video.VideoSearchService {
        return DefaultVideoSearch(videoMetadataSearchService, indexWriter)
    }

    @Bean
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