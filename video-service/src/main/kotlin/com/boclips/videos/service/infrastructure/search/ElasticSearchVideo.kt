package com.boclips.videos.service.infrastructure.search

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ElasticSearchVideo @JsonCreator constructor(
        @JsonProperty("id") val id: String,
        @JsonProperty("reference_id") val referenceId: String,
        @JsonProperty("title") val title: String,
        @JsonProperty("source") val source: String,
        @JsonProperty("date") val date: String,
        @JsonProperty("description") val description: String
)