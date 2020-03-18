package com.boclips.search.service.infrastructure.common.filters

import com.boclips.search.service.infrastructure.common.HasAgeRange
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.QueryBuilders.boolQuery


fun beWithinAgeRange(filterMin: Int?, filterMax: Int?): BoolQueryBuilder {
    return boolQuery()
        .apply {
            if (filterMin != null) {
                must(QueryBuilders.rangeQuery(HasAgeRange.AGE_RANGE_MIN).apply {
                    gte(filterMin)
                    lt(filterMax)
                })
            }
            if (filterMax != null) {
                must(QueryBuilders.rangeQuery(HasAgeRange.AGE_RANGE_MAX).apply {
                    gt(filterMin)
                    lte(filterMax)
                })
            }

        }
}
