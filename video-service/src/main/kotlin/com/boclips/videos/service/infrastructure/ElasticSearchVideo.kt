package com.boclips.videos.service.infrastructure

import com.boclips.videos.service.domain.model.Video
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ElasticSearchVideo @JsonCreator constructor(
        @JsonProperty("id") val id: String,
        @JsonProperty("title") val title: String,
        @JsonProperty("description") val description: String
) {
    fun toVideo(): Video {
        return Video(id = this.id, title = this.title, description = this.description)
    }
}