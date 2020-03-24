package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.common.Do
import com.boclips.search.service.domain.common.ResultCounts
import com.boclips.search.service.domain.common.FacetCount
import com.boclips.search.service.domain.common.FacetType
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.SearchResults
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.AGE_RANGES
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.SUBJECTS
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.allCriteria
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.removeCriteria
import com.boclips.search.service.infrastructure.videos.aggregations.AgeRangeAggregation.Companion.aggregateAgeRanges
import com.boclips.search.service.infrastructure.videos.aggregations.AgeRangeAggregation.Companion.extractAgeRangeCounts
import com.boclips.search.service.infrastructure.videos.aggregations.SubjectAggregation.Companion.aggregateSubjects
import com.boclips.search.service.infrastructure.videos.aggregations.SubjectAggregation.Companion.extractSubjectCounts
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.lucene.search.function.CombineFunction
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortOrder as EsSortOrder

class VideoIndexReader(val client: RestHighLevelClient) : IndexReader<VideoMetadata, VideoQuery> {
    companion object : KLogging();

    override fun search(searchRequest: PaginatedSearchRequest<VideoQuery>): SearchResults {
        val results = search(searchRequest.query, searchRequest.startIndex, searchRequest.windowSize)

        val elements = results.hits
            .map(VideoDocumentConverter::fromSearchHit)
            .map { it.id }

        val counts = ResultCounts(
            totalHits = results.hits.totalHits?.value ?: 0L,
            facets = extractFacets(results)
        )

        return SearchResults(elements = elements, counts = counts)
    }

    private fun search(videoQuery: VideoQuery, startIndex: Int, windowSize: Int): SearchResponse {
        val query = SearchSourceBuilder()
            .apply {
                query(EsVideoQuery().buildQuery(videoQuery))
                aggregation(aggregateSubjects(removeCriteria(allCriteria(videoQuery), SUBJECTS)))
                aggregation(aggregateAgeRanges(removeCriteria(allCriteria(videoQuery), AGE_RANGES)))
                postFilter(allCriteria(videoQuery))
                videoQuery.sort?.let { applySort(it) }
            }

        val request = SearchRequest(
            arrayOf(VideosIndex.getIndexAlias()),
            query.from(startIndex).size(windowSize).explain(false)
        )

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

    private fun extractFacets(results: SearchResponse): List<FacetCount> {
        return listOf(
            FacetCount(type = FacetType.Subjects, counts = extractSubjectCounts(results)),
            FacetCount(type = FacetType.AgeRanges, counts = extractAgeRangeCounts(results))
        )
    }
}
