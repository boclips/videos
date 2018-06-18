package com.boclips.cleanser.infrastructure.kaltura.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class MediaItem(@JsonProperty("referenceId") val referenceId: String)