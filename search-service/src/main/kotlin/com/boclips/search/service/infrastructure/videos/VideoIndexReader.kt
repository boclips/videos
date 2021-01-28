package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.common.Do
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.ResultCounts
import com.boclips.search.service.domain.common.SearchResults
import com.boclips.search.service.domain.common.model.CursorBasedIndexSearchRequest
import com.boclips.search.service.domain.common.model.IndexSearchRequest
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.common.model.PagingCursor
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.allCriteria
import com.boclips.search.service.infrastructure.videos.aggregations.AgeRangeAggregation.Companion.aggregateAgeRanges
import com.boclips.search.service.infrastructure.videos.aggregations.AttachmentTypeAggregation.Companion.aggregateAttachmentTypes
import com.boclips.search.service.infrastructure.videos.aggregations.ChannelAggregation.Companion.aggregateChannels
import com.boclips.search.service.infrastructure.videos.aggregations.DurationAggregation.Companion.aggregateDuration
import com.boclips.search.service.infrastructure.videos.aggregations.PriceAggregation.Companion.aggregateVideoPrices
import com.boclips.search.service.infrastructure.videos.aggregations.SubjectAggregation.Companion.aggregateSubjects
import com.boclips.search.service.infrastructure.videos.aggregations.VideoTypeAggregation.Companion.aggregateVideoTypes
import com.boclips.search.service.infrastructure.videos.aggregations.extractFacetCounts
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchScrollRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.lucene.search.function.CombineFunction
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import java.util.concurrent.TimeUnit
import org.elasticsearch.search.sort.SortOrder as EsSortOrder

class VideoIndexReader(val client: RestHighLevelClient) : IndexReader<VideoMetadata, VideoQuery> {
    private val scrollTimeout = TimeValue(5, TimeUnit.MINUTES)

    companion object : KLogging()

    override fun search(searchRequest: IndexSearchRequest<VideoQuery>): SearchResults {
        val results = performSearch(searchRequest)

        val elements = results.hits
            .map(VideoDocumentConverter::fromSearchHit)
            .map { it.id }

        val counts =
            ResultCounts(
                totalHits = results.hits.totalHits?.value ?: 0L,
                facets = if (searchRequest.isCursorBased()) emptyList() else extractFacetCounts(results)
            )

        val cursor: PagingCursor? = results.scrollId?.let(::PagingCursor)

        return SearchResults(elements = elements, counts = counts, cursor = cursor)
    }

    private fun performSearch(searchRequest: IndexSearchRequest<VideoQuery>): SearchResponse {
        val isCursorBasedRequest = searchRequest.isCursorBased()
        (searchRequest as? CursorBasedIndexSearchRequest)?.cursor?.let { cursor ->
            return client.scroll(
                SearchScrollRequest(cursor.value).scroll(scrollTimeout),
                RequestOptions.DEFAULT
            )
        }

        val videoQuery = searchRequest.query
        val windowSize = searchRequest.windowSize
        val query = SearchSourceBuilder()
            .apply {
                query(EsVideoQuery().buildQuery(videoQuery))
                if (!isCursorBasedRequest) { // aggregations cause trouble with the scroll API
                    aggregation(aggregateSubjects(videoQuery))
                    aggregation(aggregateAgeRanges(videoQuery))
                    aggregation(aggregateDuration(videoQuery))
                    aggregation(aggregateAttachmentTypes(videoQuery))
                    aggregation(aggregateVideoTypes(videoQuery))
                    aggregation(aggregateVideoPrices(videoQuery.facetDefinition?.organisationId))
                    if (videoQuery.facetDefinition?.includeChannelFacets == true) {
                        aggregation(aggregateChannels(videoQuery))
                    }
                }
                postFilter(allCriteria(videoQuery.userQuery))
                if (videoQuery.sort.isNotEmpty()) {
                    videoQuery.sort.forEach {
                        applySort(it)
                    }
                }
            }

        val request = SearchRequest(
            arrayOf(VideosIndex.getIndexAlias()),
            query
                .size(windowSize)
                .explain(false)
                .apply {
                    (searchRequest as? PaginatedIndexSearchRequest)?.let {
                        from(searchRequest.startIndex)
                    }
                }
        ).apply {
            (searchRequest as? CursorBasedIndexSearchRequest)?.let {
                scroll(scrollTimeout)
            }
        }

        return client.search(request, RequestOptions.DEFAULT)
    }

    private fun SearchSourceBuilder.applySort(sort: Sort<VideoMetadata>) {
        Do exhaustive when (sort) {
            is Sort.ByField -> sort(sort.fieldName.name, EsSortOrder.fromString(sort.order.toString()))
            is Sort.ByRandom -> query(
                FunctionScoreQueryBuilder(
                    query(),
                    ScoreFunctionBuilders.randomFunction()
                ).boostMode(CombineFunction.REPLACE)
            )
        }
    }
}
