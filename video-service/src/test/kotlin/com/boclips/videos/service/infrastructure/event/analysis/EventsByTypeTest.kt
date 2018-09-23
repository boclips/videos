package com.boclips.videos.service.infrastructure.event.analysis

import com.boclips.videos.service.infrastructure.event.analysis.GroupEventsByType.groupByType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GroupEventsByTypeTest {

    @Test
    fun `groups events by type`() {
        val searchEvent = TestFactories.createSearchEvent()
        val playbackEvent = TestFactories.createPlaybackEvent()

        val (searchEvents, playbackEvents) = groupByType(listOf(searchEvent, playbackEvent))

        assertThat(searchEvents).containsExactly(searchEvent)
        assertThat(playbackEvents).containsExactly(playbackEvent)
    }
}
