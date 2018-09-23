package com.boclips.videos.service.infrastructure.event.analysis

import com.boclips.videos.service.application.event.PlaybackEvent
import java.time.ZonedDateTime

data class Interaction(val timestamp: ZonedDateTime, val description: String, val related: List<Interaction>) {

    companion object {
        fun fromPlaybackEvents(events: List<PlaybackEvent>): List<Interaction> {
            return combineSegments(events).map { events ->
                Interaction(
                        timestamp = events.first().timestamp,
                        description = formatPlaybackDescription(events),
                        related = emptyList()
                )
            }
        }

        fun fromSearchAndPlaybackEvents(events: List<SearchAndPlayback>): List<Interaction> {
            return events.map(this::fromSearchAndPlayback)
        }

        private fun fromSearchAndPlayback(searchAndPlayback: SearchAndPlayback): Interaction {

            val (searchEvent, playbackEvents) = searchAndPlayback

            val playbackInteractions = fromPlaybackEvents(playbackEvents)
            val timestamp = (playbackEvents.map { it.timestamp } + searchEvent.timestamp).max()!!
            return Interaction(
                    timestamp = timestamp,
                    description = "Search for '${searchEvent.data.query}' (${searchEvent.data.resultsReturned} results).",
                    related = playbackInteractions
            )
        }

        private fun combineSegments(events: List<PlaybackEvent>): List<List<PlaybackEvent>> {
            return events.groupBy { VideoIdPlayerId(it.data.videoId, it.data.playerId) }.values.toList()
        }

        private fun formatPlaybackDescription(events: List<PlaybackEvent>): String {
            val totalSeconds = events.map { it.data.segmentEndSeconds - it.data.segmentStartSeconds }.sum()
            val duration = DurationFormatter.formatSeconds(totalSeconds)
            return "Watch $duration of ${events.first().data.videoId}."
        }

        fun sortRecursively(interactions: List<Interaction>): List<Interaction> {
            return interactions
                    .map { it.copy(related = sortRecursively(it.related)) }
                    .sortedBy { it.timestamp }
        }
    }

    data class VideoIdPlayerId(val videoId: String, val playerId: String)
}

