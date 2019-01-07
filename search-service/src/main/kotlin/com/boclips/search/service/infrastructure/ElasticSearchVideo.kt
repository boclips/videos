package com.boclips.search.service.infrastructure

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ElasticSearchVideo @JsonCreator constructor(
        @JsonProperty(ID) val id: String,
        @JsonProperty(TITLE) val title: String,
        @JsonProperty(DESCRIPTION) val description: String,
        @JsonProperty(CONTENT_PROVIDER) val contentProvider: String,
        @JsonProperty(KEYWORDS) val keywords: List<String>,
        @JsonProperty(TYPE_ID) val typeId: Int
) {
    companion object {
        const val ID = "id"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val CONTENT_PROVIDER = "contentProvider"
        const val KEYWORDS = "keywords"
        const val TYPE_ID = "typeId"
    }
}
