package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ContentType
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ChannelDocument @JsonCreator constructor(
    @param:JsonProperty(ID) val id: String,
    @param:JsonProperty(NAME) val name: String,
    @param:JsonProperty(AUTOCOMPLETE_NAME) val autocompleteName: String,
    @param:JsonProperty(TYPES) val types: List<ContentType>,
    @param:JsonProperty(INGEST_TYPE) val ingestType: String?,
    @param:JsonProperty(ELIGIBLE_FOR_STREAM) val eligibleForStream: Boolean,
    @param:JsonProperty(TAXONOMY_VIDEO_LEVEL_TAGGING) val taxonomyVideoLevelTagging: Boolean,
    @param:JsonProperty(TAXONOMY_CATEGORIES) val taxonomyCategories: List<String>?,
    @param:JsonProperty(TAXONOMY_CATEGORIES_WITH_ANCESTORS) val taxonomyCategoriesWithAncestors: List<String>?,
    @param:JsonProperty(IS_YOUTUBE) val isYoutube: Boolean?,
    @param:JsonProperty(IS_PRIVATE) val isPrivate: Boolean = false,
) {
    companion object {
        const val ID = "id"
        const val NAME = "name"
        const val AUTOCOMPLETE_NAME = "autocompleteName"
        const val TYPES = "types"
        const val INGEST_TYPE = "ingestType"
        const val ELIGIBLE_FOR_STREAM = "eligibleForStream"
        const val TAXONOMY_CATEGORIES = "taxonomyCategories"
        const val TAXONOMY_CATEGORIES_WITH_ANCESTORS = "taxonomyCategoriesWithAncestors"
        const val TAXONOMY_VIDEO_LEVEL_TAGGING = "taxonomyVideoLevelTagging"
        const val IS_YOUTUBE = "isYoutube"
        const val IS_PRIVATE = "isPrivate"
    }
}
