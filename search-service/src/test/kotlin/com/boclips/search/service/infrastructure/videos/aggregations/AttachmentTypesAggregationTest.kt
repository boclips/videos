package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.domain.videos.model.VideoQuery
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.junit.jupiter.api.Test

class AttachmentTypesAggregationTest {
    @Test
    fun `filters by`() {
        val filter = boolQuery()
        val aggregateTypes = AttachmentTypeAggregation.aggregateAttachmentTypes(VideoQuery())

        assertThat(aggregateTypes.filter).isEqualTo(filter)
    }

    @Test
    fun `aggregates up to 2 attachment types`() {
        val aggregateTypes = AttachmentTypeAggregation.aggregateAttachmentTypes(VideoQuery())

        assertThat(aggregateTypes.subAggregations?.toString())
            .contains("""[{"attachment type names":{"terms":{"field":"attachmentTypes","size":2,""")
    }
}