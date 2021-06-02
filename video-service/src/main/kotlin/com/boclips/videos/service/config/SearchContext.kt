package com.boclips.videos.service.config

import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.search.service.infrastructure.ElasticSearchClient
import com.boclips.search.service.infrastructure.IndexParameters
import com.boclips.search.service.infrastructure.channels.ChannelsIndexReader
import com.boclips.search.service.infrastructure.channels.ChannelsIndexWriter
import com.boclips.search.service.infrastructure.collections.CollectionIndexReader
import com.boclips.search.service.infrastructure.collections.CollectionIndexWriter
import com.boclips.search.service.infrastructure.subjects.SubjectsIndexReader
import com.boclips.search.service.infrastructure.subjects.SubjectsIndexWriter
import com.boclips.search.service.infrastructure.videos.VideoIndexReader
import com.boclips.search.service.infrastructure.videos.VideoIndexWriter
import com.boclips.search.service.infrastructure.videos.aggregations.ElasticSearchAggregationProperties
import com.boclips.search.service.infrastructure.videos.legacy.SolrVideoSearchService
import com.boclips.videos.service.application.channels.VideoChannelService
import com.boclips.videos.service.config.properties.ElasticSearchProperties
import com.boclips.videos.service.config.properties.ReindexProperties
import com.boclips.videos.service.config.properties.SolrProperties
import com.boclips.videos.service.domain.service.collection.CollectionIndex
import com.boclips.videos.service.domain.service.suggestions.ChannelIndex
import com.boclips.videos.service.domain.service.suggestions.SubjectIndex
import com.boclips.videos.service.domain.service.video.VideoIndex
import com.boclips.videos.service.infrastructure.search.DefaultChannelSearch
import com.boclips.videos.service.infrastructure.search.DefaultCollectionSearch
import com.boclips.videos.service.infrastructure.search.DefaultSubjectSearch
import com.boclips.videos.service.infrastructure.search.DefaultVideoSearch
import io.opentracing.Tracer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@ComponentScan(basePackageClasses = [ElasticSearchAggregationProperties::class])
class SearchContext(
    private val tracer: Tracer
) {
    @Bean
    @Profile("!fakes-search")
    fun legacySearchService(solrProperties: SolrProperties): LegacyVideoSearchService {
        return SolrVideoSearchService(host = solrProperties.host, port = solrProperties.port)
    }

    @Bean
    @Profile("!fakes-search")
    fun videoSearchService(
        elasticSearchClient: ElasticSearchClient,
        videoChannelService: VideoChannelService,
        reindexProperties: ReindexProperties,
        elasticSearchAggregationProperties: ElasticSearchAggregationProperties
    ): VideoIndex {
        return DefaultVideoSearch(
            VideoIndexReader(elasticSearchClient.buildClient(), elasticSearchAggregationProperties),
            VideoIndexWriter.createInstance(
                elasticSearchClient.buildClient(),
                IndexParameters(numberOfShards = 5),
                reindexProperties.batchSize
            ),
            videoChannelService
        )
    }

    @Bean
    @Profile("!fakes-search")
    fun channelSearchService(
        elasticSearchClient: ElasticSearchClient,
        reindexProperties: ReindexProperties
    ): ChannelIndex {
        return DefaultChannelSearch(
            ChannelsIndexReader(elasticSearchClient.buildClient()),
            ChannelsIndexReader(elasticSearchClient.buildClient()),
            ChannelsIndexWriter.createInstance(
                elasticSearchClient.buildClient(),
                IndexParameters(numberOfShards = 5),
                reindexProperties.batchSize
            )
        )
    }

    @Bean
    @Profile("!fakes-search")
    fun subjectSearchService(
        elasticSearchClient: ElasticSearchClient,
        reindexProperties: ReindexProperties
    ): SubjectIndex {
        return DefaultSubjectSearch(
            SubjectsIndexReader(elasticSearchClient.buildClient()),
            SubjectsIndexWriter.createInstance(
                elasticSearchClient.buildClient(),
                IndexParameters(numberOfShards = 5),
                reindexProperties.batchSize
            )
        )
    }

    @Bean
    @Profile("!fakes-search")
    fun collectionSearchService(
        elasticSearchClient: ElasticSearchClient,
        reindexProperties: ReindexProperties
    ): CollectionIndex {
        return DefaultCollectionSearch(
            CollectionIndexReader(elasticSearchClient.buildClient()),
            CollectionIndexWriter.createInstance(
                elasticSearchClient.buildClient(),
                IndexParameters(numberOfShards = 5),
                reindexProperties.batchSize
            )
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
            password = elasticSearchProperties.password,
            tracer = tracer
        )
    }
}
