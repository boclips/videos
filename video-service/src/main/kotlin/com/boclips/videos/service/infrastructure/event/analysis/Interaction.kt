package com.boclips.videos.service.infrastructure.event.analysis

import com.boclips.videos.service.infrastructure.event.types.PlaybackEvent
import com.boclips.videos.service.infrastructure.event.analysis.DurationFormatter.formatSeconds
import java.time.ZonedDateTime
import kotlin.math.max

data class Interaction(val timestamp: ZonedDateTime, val description: String, val related: List<Interaction>) {

    companion object {
        fun fromPlaybackEvents(events: List<PlaybackEvent>): List<Interaction> {

            fun formatDescription(events: List<PlaybackEvent>): String {
                val totalSeconds = events.map { it.data.segmentEndSeconds - it.data.segmentStartSeconds }.sum()
                val videoData = events.first().data
                return "Watch ${formatSeconds(totalSeconds)} of ${videoData.videoId} (duration ${formatSeconds(videoData.videoDurationSeconds)})."
            }

            return events
                    .groupBy { VideoIdPlayerId(it.data.videoId, it.data.playerId) }.values
                    .map { segments ->
                        Interaction(
                                timestamp = segments.first().timestamp,
                                description = formatDescription(segments),
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
            return Interaction(
                    timestamp = searchEvent.timestamp,
                    description = "Search for '${searchEvent.data.query}' (${searchEvent.data.resultsReturned} results).",
                    related = playbackInteractions
            )
        }

        fun sortRecursively(interactions: List<Interaction>): List<Interaction> {

            fun sortKey(timestamp: ZonedDateTime) = timestamp.toEpochSecond()

            fun maxTime(interaction: Interaction): Long {

                val interactionTime = sortKey(interaction.timestamp)
                if (interaction.related.isEmpty()) {
                    return interactionTime
                }
                return max(interactionTime, sortKey(interaction.related.last().timestamp))
            }

            return interactions
                    .map { it.copy(related = sortRecursively(it.related)) }
                    .sortedBy(::maxTime)
        }
    }

    data class VideoIdPlayerId(val videoId: String, val playerId: String)
}

