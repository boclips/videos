package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import com.boclips.search.service.testsupport.VideoQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChannelAggregationTest {

    @Test
    fun `filters by`() {
        val filter = VideoFilterCriteria.allCriteria(VideoQueryFactory.empty().userQuery)
        val aggregateChannels = ChannelAggregation.aggregateChannels(VideoQueryFactory.empty(), 10)

        assertThat(aggregateChannels?.filter).isEqualTo(filter)
    }

    @Test
    fun `aggregates up to 60 video types`() {
        val aggregateTypes = ChannelAggregation.aggregateChannels(VideoQueryFactory.empty(), 60)

        assertThat(aggregateTypes?.subAggregations?.toString())
            .contains("""[{"channel ids":{"terms":{"field":"contentPartnerId","size":60,""")
    }
}
