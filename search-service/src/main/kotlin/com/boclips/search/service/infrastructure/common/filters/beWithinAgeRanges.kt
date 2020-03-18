package com.boclips.search.service.infrastructure.common.filters

import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.infrastructure.common.HasAgeRange
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.termsQuery

fun beWithinAgeRanges(ageRanges: List<AgeRange>): BoolQueryBuilder? {
    return boolQuery().apply {
        ageRanges.forEach { ageRange ->
            should(termsQuery(HasAgeRange.AGE_RANGE, ageRange.toRange()))
        }
    }
}
