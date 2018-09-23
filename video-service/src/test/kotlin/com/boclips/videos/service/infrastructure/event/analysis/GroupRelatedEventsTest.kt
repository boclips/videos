package com.boclips.videos.service.infrastructure.event.analysis

import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class GroupRelatedEventsTest {

    @Test
    fun `attaches playback events to search when searchId matches`() {

        val searchEvent = TestFactories.createSearchEvent(searchId = "search-id")
        val playbackEvent = TestFactories.createPlaybackEvent(searchId = "search-id")

        val groupes = GroupRelatedEvents.create(listOf(searchEvent), listOf(playbackEvent))

        assertThat(groupes.standalonePlaybacks).isEmpty()
        assertThat(groupes.searches).hasSize(1)
        assertThat(groupes.searches.first().searchEvent).isEqualTo(searchEvent)
        assertThat(groupes.searches.first().playbackEvents).isEqualTo(listOf(playbackEvent))
    }
    @Test
    fun `does not attach playback event to search when searchId is null`() {

        val searchEvent = TestFactories.createSearchEvent(searchId = "search-id")
        val playbackEvent = TestFactories.createPlaybackEvent(searchId = null)

        val groupes = GroupRelatedEvents.create(listOf(searchEvent), listOf(playbackEvent))

        assertThat(groupes.standalonePlaybacks).containsExactly(playbackEvent)
        assertThat(groupes.searches).hasSize(1)
        assertThat(groupes.searches.first().searchEvent).isEqualTo(searchEvent)
        assertThat(groupes.searches.first().playbackEvents).isEmpty()
    }
    @Test
    fun `ignores playback event when searchId exists but does not match any search event`() {

        val searchEvent = TestFactories.createSearchEvent(searchId = "search-id")
        val playbackEvent = TestFactories.createPlaybackEvent(searchId = "search-id-2")

        val groupes = GroupRelatedEvents.create(listOf(searchEvent), listOf(playbackEvent))

        assertThat(groupes.standalonePlaybacks).isEmpty()
        assertThat(groupes.searches).hasSize(1)
        assertThat(groupes.searches.first().searchEvent).isEqualTo(searchEvent)
        assertThat(groupes.searches.first().playbackEvents).isEmpty()
    }
}
