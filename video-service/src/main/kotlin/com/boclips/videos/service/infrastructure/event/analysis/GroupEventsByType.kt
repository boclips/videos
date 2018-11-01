package com.boclips.videos.service.infrastructure.event.analysis

import com.boclips.videos.service.infrastructure.event.types.Event
import com.boclips.videos.service.infrastructure.event.types.EventType
import com.boclips.videos.service.infrastructure.event.types.PlaybackEvent
import com.boclips.videos.service.infrastructure.event.types.SearchEvent

object GroupEventsByType {

    fun groupByType(events: List<Event<*>>): Pair<List<SearchEvent>, List<PlaybackEvent>> {
        return events.fold(Pair(emptyList(), emptyList())) { (ss, ps), e ->
            when (e.type) {
                EventType.SEARCH.name -> Pair(ss + e as SearchEvent, ps)
                EventType.PLAYBACK.name -> Pair(ss, ps + e as PlaybackEvent)
                else -> Pair(ss, ps)
            }
        }
    }

}

