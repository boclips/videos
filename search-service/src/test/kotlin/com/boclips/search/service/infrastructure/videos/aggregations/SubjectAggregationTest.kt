package com.boclips.search.service.infrastructure.videos.aggregations

import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.junit.jupiter.api.Test

class SubjectAggregationTest {
    @Test
    fun `filters by`() {
        val filter = boolQuery()
        val aggregateSubjects = SubjectAggregation.aggregateSubjects(filter)

        assertThat(aggregateSubjects?.filter).isEqualTo(filter)
    }

    @Test
    fun `aggregates up to 60 subjects`() {
        val aggregateSubjects = SubjectAggregation.aggregateSubjects(boolQuery())

        assertThat(aggregateSubjects?.subAggregations?.toString())
            .contains("""[{"subject ids":{"terms":{"field":"subjectIds","size":60,""")
    }
}