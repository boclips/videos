package com.boclips.videoanalyser.application.csv

import com.boclips.videoanalyser.domain.model.search.SearchExpectation
import com.fasterxml.jackson.annotation.JsonProperty

data class SearchExpectationCsv(@JsonProperty(value = "QUERY") val query: String, @JsonProperty(value = "VIDEO") val video: String) {

    companion object {
        val regex = Regex("\\s*([0-9]+)\\s*")
    }

    fun toSearchExpectation(): SearchExpectation? {
        if(video.isBlank()) {
            return null
        }
        if(query.isBlank()) {
            throw IllegalStateException("Empty query for video $video")
        }

        val match = regex.matchEntire(video) ?: throw IllegalStateException("Invalid video id: '$video'")
        val videoId = match.groupValues[1]

        return SearchExpectation(query.trim(), videoId)
    }
}
