package com.boclips.search.service.infrastructure.common.filters

import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.infrastructure.common.HasAgeRange
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.termsQuery

fun beWithinAgeRanges(ageRanges: List<AgeRange>): BoolQueryBuilder? {
    return boolQuery()
        .queryName(VideoFilterCriteria.AGE_RANGES)
        .apply {
        ageRanges.forEach { ageRange ->
            should(termsQuery(HasAgeRange.AGE_RANGE, ageRange.toRange()))
        }
    }
}
