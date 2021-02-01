package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.common.Count
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.script.Script
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder

class PriceAggregation {
    companion object {
        private const val PRICE_AGGREGATION_FILTER = "price"
        private const val AGGREGATE_PRICES_USING_DEFAULT = """
                            if (doc.containsKey('prices.DEFAULT')) {
                              doc['prices.DEFAULT']
                            } else {
                              0
                            }
                            """

        fun aggregateVideoPrices(organisationId: String?): TermsAggregationBuilder {
            return AggregationBuilders
                .terms(PRICE_AGGREGATION_FILTER)
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
        }

        fun extractBucketCounts(response: SearchResponse): Set<Count> {
            return response.aggregations.asList()
                .find { it.name == PRICE_AGGREGATION_FILTER }
                ?.let { parseBuckets((it as ParsedStringTerms).buckets).toSet() }
                ?: emptySet()
        }
    }
}
