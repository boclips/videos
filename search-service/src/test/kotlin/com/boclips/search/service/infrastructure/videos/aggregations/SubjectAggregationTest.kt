package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import com.boclips.search.service.testsupport.VideoQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SubjectAggregationTest {
    @Test
    fun `filters by`() {
        val filter = VideoFilterCriteria.allCriteria(VideoQueryFactory.empty().userQuery)
        val aggregateSubjects = SubjectAggregation.aggregateSubjects(VideoQueryFactory.empty())

        assertThat(aggregateSubjects.filter).isEqualTo(filter)
    }

    @Test
    fun `aggregates up to 60 subjects`() {
        val aggregateSubjects = SubjectAggregation.aggregateSubjects(VideoQueryFactory.empty())

        assertThat(aggregateSubjects.subAggregations?.toString())
            .contains("""[{"subject ids":{"terms":{"field":"subjectIds","size":60,""")
    }
}
