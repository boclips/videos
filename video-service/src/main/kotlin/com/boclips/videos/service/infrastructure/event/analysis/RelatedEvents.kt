package com.boclips.videos.service.infrastructure.event.analysis

import com.boclips.videos.service.infrastructure.event.types.PlaybackEvent
import com.boclips.videos.service.infrastructure.event.types.SearchEvent

data class RelatedEvents(val searches: List<SearchAndPlayback>, val standalonePlaybacks: List<PlaybackEvent>)

data class SearchAndPlayback(val searchEvent: SearchEvent, val playbackEvents: List<PlaybackEvent>)

object GroupRelatedEvents {

    fun create(searchEvents: List<SearchEvent>, playbackEvents: List<PlaybackEvent>): RelatedEvents {

        val (playbacksWithSearchId, playbacksWithoutSearchId) = splitPlaybackEventsBySearchIdPresence(playbackEvents)

        val searches = playbacksWithSearchId.fold(SearchAndPlaybackBySearchId(searchEvents)) { searches, e ->
            searches.add(e)
        }.map.values.toList()

        return RelatedEvents(searches, playbacksWithoutSearchId)
    }

    private fun splitPlaybackEventsBySearchIdPresence(playbackEvents: List<PlaybackEvent>): Pair<List<PlaybackEvent>, List<PlaybackEvent>> {

        return playbackEvents.fold(Pair(emptyList(), emptyList())) { (withSearchId, withoutSearchId), event ->
            if (event.data.searchId == null)
                Pair(withSearchId, withoutSearchId + event)
            else
                Pair(withSearchId + event, withoutSearchId)
        }
    }

    private data class SearchAndPlaybackBySearchId(val map: Map<String, SearchAndPlayback>) {

        constructor(searchEvents: List<SearchEvent>) : this(searchEvents.groupBy({ it.data.searchId }) {
            SearchAndPlayback(
                it,
                emptyList()
            )
        }.mapValues { it.value.first() })

        fun add(playbackEvent: PlaybackEvent): SearchAndPlaybackBySearchId {
            val searchId = playbackEvent.data.searchId!!
            val searchAndPlayback = map[searchId] ?: return this
            return SearchAndPlaybackBySearchId(map.plus(searchId to searchAndPlayback.copy(playbackEvents = searchAndPlayback.playbackEvents + playbackEvent)))
        }
    }
}


