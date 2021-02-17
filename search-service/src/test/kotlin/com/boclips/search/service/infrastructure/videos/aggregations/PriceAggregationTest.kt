package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import com.boclips.search.service.testsupport.VideoQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PriceAggregationTest {
    @Test
    fun `filters by`() {
        val filter = VideoFilterCriteria.allCriteria(VideoQueryFactory.empty().userQuery)
        val aggregateSubjects = PriceAggregation.aggregateVideoPrices(VideoQueryFactory.empty(), 10)

        assertThat(aggregateSubjects.filter).isEqualTo(filter)
    }

    @Test
    fun `aggregates up to 60 subjects`() {
        val aggregateSubjects = PriceAggregation.aggregateVideoPrices(VideoQueryFactory.empty(), 1000)

        assertThat(aggregateSubjects.subAggregations?.toString())
            .contains("""size":1000,""".trimIndent())
    }
}
