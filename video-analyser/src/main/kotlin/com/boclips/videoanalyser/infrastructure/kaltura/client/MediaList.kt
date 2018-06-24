package com.boclips.videoanalyser.infrastructure.kaltura.client

import com.boclips.videoanalyser.infrastructure.kaltura.MediaItem
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class MediaList(
        @JsonProperty("objects") val items: List<MediaItem> = emptyList(),
        @JsonProperty("totalCount") val count: Long
)
