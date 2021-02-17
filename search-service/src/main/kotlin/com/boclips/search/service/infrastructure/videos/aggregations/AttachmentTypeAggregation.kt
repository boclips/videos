package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoDocument
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms

class AttachmentTypeAggregation {
    companion object {
        private const val ATTACHMENT_TYPE_AGGREGATION_FILTER = "attachment types"
        private const val ATTACHMENT_TYPE_SUB_AGGREGATION = "attachment type names"

        fun aggregateAttachmentTypes(videoQuery: VideoQuery): FilterAggregationBuilder {
            return aggregateAttachmentTypes(
                queryBuilder = VideoFilterCriteria.removeCriteria(
                    VideoFilterCriteria.allCriteria(videoQuery.userQuery),
                    VideoFilterCriteria.ATTACHMENT_TYPES
                )
            )
        }

        fun extractBucketCounts(response: SearchResponse): Set<Count> {
            return extractStringTermBucketCounts(
                response = response,
                filterName = ATTACHMENT_TYPE_AGGREGATION_FILTER,
                subAggregationName = ATTACHMENT_TYPE_SUB_AGGREGATION
            )
        }

        private fun aggregateAttachmentTypes(queryBuilder: BoolQueryBuilder?): FilterAggregationBuilder {
            return AggregationBuilders
                .filter(ATTACHMENT_TYPE_AGGREGATION_FILTER, queryBuilder)
                .subAggregation(
                    AggregationBuilders.terms(ATTACHMENT_TYPE_SUB_AGGREGATION).field(VideoDocument.ATTACHMENT_TYPES)
                        .size(2)
                )
        }
    }
}

