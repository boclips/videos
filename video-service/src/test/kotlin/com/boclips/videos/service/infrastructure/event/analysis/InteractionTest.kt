package com.boclips.videos.service.infrastructure.event.analysis

import com.boclips.videos.service.infrastructure.event.analysis.Interaction.Companion.sortRecursively
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIOException
import org.junit.Test
import java.net.InterfaceAddress
import java.time.ZonedDateTime


class InteractionTest {

    private val now = ZonedDateTime.now()

    @Test
    fun `fromPlaybackEvents combines events with the same player and video id`() {
        val event1 = TestFactories.createPlaybackEvent(playerId = "player-id", videoId = "123", captureTime = now)
        val event2 = TestFactories.createPlaybackEvent(playerId = "player-id", videoId = "123", captureTime = now.minusMinutes(1))

        val interactions = Interaction.fromPlaybackEvents(listOf(event1, event2))

        assertThat(interactions).hasSize(1)
        assertThat(interactions[0]).isEqualTo(Interaction(timestamp = now, description = "Watch 1m 0s of 123.", related = emptyList()))
    }

    @Test
    fun `fromPlaybackEvents does not combine events with different player and video id`() {
        val event1 = TestFactories.createPlaybackEvent(playerId = "player-id", videoId = "123", captureTime = now)
        val event2 = TestFactories.createPlaybackEvent(playerId = "player-id2", videoId = "1234", captureTime = now.minusMinutes(1))

        val interactions = Interaction.fromPlaybackEvents(listOf(event1, event2))

        assertThat(interactions).hasSize(2)
    }

    @Test
    fun `fromSearchAndPlaybackEvents nests playback interactions`() {
        val searchEvent = TestFactories.createSearchEvent(searchId = "search-id", timestamp = now.minusMinutes(1), query = "boston", resultsReturned = 10)
        val playbackEvent = TestFactories.createPlaybackEvent(searchId = "search-id", captureTime = now)

        val interactions = Interaction.fromSearchAndPlaybackEvents(listOf(SearchAndPlayback(searchEvent, listOf(playbackEvent))))

        assertThat(interactions).hasSize(1)
        assertThat(interactions.first().description).isEqualTo("Search for 'boston' (10 results).")
        assertThat(interactions.first().related).hasSize(1)
        assertThat(interactions.first().related.first().description).isEqualTo("Watch 30s of video-id.")
    }

    @Test
    fun `fromSearchAndPlaybackEvents uses latest time as parent interaction timestamp`() {
        val searchEvent = TestFactories.createSearchEvent(searchId = "search-id", timestamp = now.minusMinutes(1))
        val playbackEvent = TestFactories.createPlaybackEvent(searchId = "search-id", captureTime = now)

        val interactions = Interaction.fromSearchAndPlaybackEvents(listOf(SearchAndPlayback(searchEvent, listOf(playbackEvent))))

        assertThat(interactions.first().timestamp).isEqualTo(now)
    }

    @Test
    fun `sortRecursively orders at root level`() {
        val firstInteraction = Interaction(timestamp = now.minusMinutes(2), description = "first", related = emptyList())
        val secondInteraction = Interaction(timestamp = now.minusMinutes(1), description = "second", related = emptyList())
        val thirdInteraction = Interaction(timestamp = now, description = "third", related = emptyList())

        val sorted = sortRecursively(listOf(secondInteraction, firstInteraction, thirdInteraction))

        assertThat(sorted).containsExactly(firstInteraction, secondInteraction, thirdInteraction)
    }

    @Test
    fun `sortRecursively orders nested interactions`() {
        val firstInteraction = Interaction(timestamp = now.minusMinutes(2), description = "first", related = emptyList())
        val secondInteraction = Interaction(timestamp = now.minusMinutes(1), description = "second", related = emptyList())
        val thirdInteraction = Interaction(timestamp = now, description = "third", related = emptyList())

        val related = listOf(secondInteraction, firstInteraction, thirdInteraction)

        val sorted = sortRecursively(listOf(Interaction(timestamp = now, description = "root", related = related)))

        assertThat(sorted[0].related).containsExactly(firstInteraction, secondInteraction, thirdInteraction)
    }


}
