package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.script.Script
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms

class PriceAggregation {
    companion object {
        private const val PRICE_AGGREGATION_FILTER = "prices"
        private const val PRICE_SUB_AGGREGATION_FILTER = "price values"
        private const val AGGREGATE_PRICES_USING_DEFAULT = """
                            if (doc.containsKey('prices.DEFAULT')) {
                              doc['prices.DEFAULT']
                            } else {
                              0
                            }
                            """

        fun aggregateVideoPrices(videoQuery: VideoQuery, limit: Int): FilterAggregationBuilder {
            return aggregate(
                queryBuilder = VideoFilterCriteria.removeCriteria(
                    VideoFilterCriteria.allCriteria(videoQuery.userQuery),
                    VideoFilterCriteria.VIDEO_PRICES_FILTER
                ),
                organisationId = videoQuery.userQuery.organisationPriceFilter.userOrganisationId,
                limit = limit
            )

        }

        private fun aggregate(queryBuilder: BoolQueryBuilder?, organisationId: String?, limit: Int): FilterAggregationBuilder {
            return AggregationBuilders
                .filter(PRICE_AGGREGATION_FILTER, queryBuilder)
                .subAggregation(
                    AggregationBuilders
                        .terms(PRICE_SUB_AGGREGATION_FILTER)
                        .size(limit)
                        .script(Script(
                            organisationId?.let {
                                """
                                  if (!doc.containsKey('prices.$organisationId') || doc['prices.$organisationId'].size() == 0) {
                                    $AGGREGATE_PRICES_USING_DEFAULT
                                  } else {
                                    doc['prices.$organisationId']
                                  }
                                """
                            } ?: AGGREGATE_PRICES_USING_DEFAULT
                        ))
                )
        }

        fun extractBucketCounts(response: SearchResponse): Set<Count> {
            return if (response.aggregations.asList().any
                { aggregation -> aggregation.name == PRICE_AGGREGATION_FILTER }
            ) {
                response
                    .aggregations.get<ParsedFilter>(PRICE_AGGREGATION_FILTER)
                    .aggregations.find { it.name == PRICE_SUB_AGGREGATION_FILTER }
                    ?.let {
                        parseBuckets((it as ParsedStringTerms).buckets).toSet()
                    } ?: emptySet()
            } else emptySet()

        }
    }
}
