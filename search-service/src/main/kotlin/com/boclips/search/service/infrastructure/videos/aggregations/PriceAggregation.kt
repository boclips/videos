package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.script.Script
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder

class PriceAggregation {
    companion object {
        private const val PRICE_AGGREGATION_FILTER = "price"
        private const val PRICE_SUB_AGGREGATION = "price ids"

        fun aggregateVideoPrices(organisationId: String): TermsAggregationBuilder {
            return AggregationBuilders
                    .terms(PRICE_AGGREGATION_FILTER)
                    .script(Script(
                            """
                          if (doc['prices.$organisationId'].size() == 0) {
                            if (doc['prices.DEFAULT'].size() == 0) {
                              0
                            } else {
                              doc['prices.DEFAULT']
                            }
                          } else {
                            doc['prices.$organisationId']
                          }
                        """
                    ))
        }

        fun extractBucketCounts(response: SearchResponse): List<Count> {
            return response
                .aggregations.get<ParsedFilter>(PRICE_AGGREGATION_FILTER)
                .aggregations.get<ParsedStringTerms>(PRICE_SUB_AGGREGATION)
                .buckets
                .let { buckets -> parseBuckets(buckets) }
        }
    }
}
