package com.boclips.videos.service.infrastructure.event.analysis

import com.boclips.videos.service.application.event.PlaybackEvent
import com.boclips.videos.service.infrastructure.event.Event
import com.boclips.videos.service.infrastructure.search.SearchEvent

object GroupEventsByType {

    fun groupByType(events: List<Event<*>>): Pair<List<SearchEvent>, List<PlaybackEvent>> {
        return events.fold(Pair(emptyList(), emptyList())) { (ss, ps), e ->
            when(e.type) {
                "SEARCH" -> Pair(ss+ e as SearchEvent, ps)
                "PLAYBACK" -> Pair(ss, ps+ e as PlaybackEvent)
                else -> Pair(ss, ps)
            }
        }
    }

}

