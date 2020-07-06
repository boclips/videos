package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SubjectAggregationTest {
    @Test
    fun `filters by`() {
        val filter = VideoFilterCriteria.allCriteria(VideoQuery())
        val aggregateSubjects = SubjectAggregation.aggregateSubjects(VideoQuery())

        assertThat(aggregateSubjects.filter).isEqualTo(filter)
    }

    @Test
    fun `aggregates up to 60 subjects`() {
        val aggregateSubjects = SubjectAggregation.aggregateSubjects(VideoQuery())

        assertThat(aggregateSubjects.subAggregations?.toString())
            .contains("""[{"subject ids":{"terms":{"field":"subjectIds","size":60,""")
    }
}
