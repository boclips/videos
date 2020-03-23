package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.common.Do
import com.boclips.search.service.domain.common.Bucket
import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.common.Counts
import com.boclips.search.service.domain.common.FilterCounts
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.SubjectAggregation.Companion.aggregateSubjects
import com.boclips.search.service.infrastructure.videos.SubjectAggregation.Companion.extractSubjectsAggregation
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.SUBJECTS
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.allCriteria
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria.Companion.removeCriteria
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

    override fun search(searchRequest: PaginatedSearchRequest<VideoQuery>): List<String> {
        return search(searchRequest.query, searchRequest.startIndex, searchRequest.windowSize).hits
            .map(VideoDocumentConverter::fromSearchHit)
            .map { it.id }
    }

    override fun count(query: VideoQuery): Counts {
        val response = search(videoQuery = query, startIndex = 0, windowSize = 1)

        val subjectCounts = extractSubjectsAggregation(response).map { bucket ->
            Count(id = bucket.key.toString(), hits = bucket.docCount)
        }

        return Counts(
            hits = response.hits.totalHits?.value ?: 0L,
            buckets = listOf(FilterCounts(key = Bucket.SubjectsBucket, counts = subjectCounts))
        )
    }

    private fun search(videoQuery: VideoQuery, startIndex: Int, windowSize: Int): SearchResponse {
        val query = SearchSourceBuilder()
            .apply {
                query(EsVideoQuery().buildQuery(videoQuery))
                aggregation(aggregateSubjects(removeCriteria(allCriteria(videoQuery), SUBJECTS)))
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
}
