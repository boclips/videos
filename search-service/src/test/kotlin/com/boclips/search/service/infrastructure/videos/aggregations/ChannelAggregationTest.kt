package com.boclips.search.service.infrastructure.videos.aggregations

import com.boclips.search.service.infrastructure.videos.VideoFilterCriteria
import com.boclips.search.service.testsupport.VideoQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChannelAggregationTest {

    @Test
    fun `filters by`() {
        val filter = VideoFilterCriteria.allCriteria(VideoQueryFactory.empty().userQuery)
        val aggregateChannels = ChannelAggregation.aggregateChannels(VideoQueryFactory.empty())

        assertThat(aggregateChannels?.filter).isEqualTo(filter)
    }
}
