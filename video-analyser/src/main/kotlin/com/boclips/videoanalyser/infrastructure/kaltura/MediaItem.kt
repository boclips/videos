package com.boclips.videoanalyser.infrastructure.kaltura

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class MediaItem(@JsonProperty("referenceId") val referenceId: String?,
                     @JsonProperty("id") val id: String)