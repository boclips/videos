package com.boclips.search.service.infrastructure

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ElasticSearchVideo @JsonCreator constructor(
        @param:JsonProperty(ID) val id: String,
        @param:JsonProperty(TITLE) val title: String,
        @param:JsonProperty(DESCRIPTION) val description: String,
        @param:JsonProperty(CONTENT_PROVIDER) val contentProvider: String,
        @param:JsonProperty(KEYWORDS) val keywords: List<String>,
        @param:JsonProperty(IS_NEWS)
        @get:JsonProperty(IS_NEWS)
        val isNews: Boolean,
        @param:JsonProperty(IS_EDUCATIONAL)
        @get:JsonProperty(IS_EDUCATIONAL)
        val isEducational: Boolean
) {
    companion object {
        const val ID = "id"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val CONTENT_PROVIDER = "contentProvider"
        const val KEYWORDS = "keywords"
        const val IS_NEWS = "isNews"
        const val IS_EDUCATIONAL = "isEducational"
    }
}
