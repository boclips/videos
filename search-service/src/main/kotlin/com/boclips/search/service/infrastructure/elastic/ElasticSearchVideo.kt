package com.boclips.search.service.infrastructure.elastic

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ElasticSearchVideo @JsonCreator constructor(
        @JsonProperty("id") val id: String,
        @JsonProperty("reference_id") val referenceId: String,
        @JsonProperty("title") val title: String,
        @JsonProperty("description") val description: String
)
