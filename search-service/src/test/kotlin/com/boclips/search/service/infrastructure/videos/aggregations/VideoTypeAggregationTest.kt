package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.testsupport.VideoQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.junit.jupiter.api.Test

class VideoTypeAggregationTest {
    @Test
    fun `filters by`() {
        val filter = boolQuery()
        val aggregateTypes = VideoTypeAggregation.aggregateVideoTypes(VideoQueryFactory.empty())

        assertThat(aggregateTypes.filter).isEqualTo(filter)
    }

    @Test
    fun `aggregates up to 2 video types`() {
        val aggregateTypes = VideoTypeAggregation.aggregateVideoTypes(VideoQueryFactory.empty())

        assertThat(aggregateTypes.subAggregations?.toString())
                .contains("""[{"video type names":{"terms":{"field":"types","size":60,""")
    }
}
