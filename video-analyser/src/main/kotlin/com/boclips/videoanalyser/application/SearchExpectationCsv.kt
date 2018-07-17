package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.domain.service.SearchExpectation
import com.fasterxml.jackson.annotation.JsonProperty

data class SearchExpectationCsv(@JsonProperty(value = "QUERY") val query: String, @JsonProperty(value = "VIDEO") val video: String) {

    companion object {
        val regex = Regex(".*/video/([a-z0-9]+)")
    }

    fun toSearchExpectation(): SearchExpectation {
        val match = regex.matchEntire(video) ?: throw IllegalStateException("Unexpected URL format: $video")

        val videoId = match.groupValues[1]

        return SearchExpectation(query, videoId)
    }
}
