package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.domain.model.Video
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Duration
import java.time.LocalDate

data class ElasticSearchVideo @JsonCreator constructor(
        @JsonProperty("id") val id: String,
        @JsonProperty("title") val title: String,
        @JsonProperty("source") val source: String,
        @JsonProperty("date") val date: String,
        @JsonProperty("duration") val duration: String,
        @JsonProperty("description") val description: String
) {
    fun toVideo(): Video {
        return Video(
                id = this.id,
                title = this.title,
                description = this.description,
                contentProvider = this.source,
                duration = try {
                    this.duration
                            .split(":")
                            .map { it.toLong() }
                            .let { Duration.ofHours(it[0]).plusMinutes(it[1]).plusSeconds(it[2]) }
                } catch (e: Exception) {Duration.ZERO},
                releasedOn = LocalDate.parse(this.date)
        )
    }
}